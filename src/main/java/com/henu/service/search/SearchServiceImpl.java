package com.henu.service.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.henu.entity.House;
import com.henu.entity.HouseDetail;
import com.henu.entity.HouseTag;
import com.henu.repository.HouseDetailRepository;
import com.henu.repository.HouseRepository;
import com.henu.repository.HouseTagRepository;
import com.henu.service.ServiceMultiResult;
import com.henu.service.ServiceResult;
import com.henu.web.form.RentSearch;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements ISearchService {
    private final static Logger logger = LoggerFactory.getLogger(ISearchService.class);

    private static final String INDEX_NAME = "xunwu";
    private static final String INDEX_TYPE = "house";
    private static final String INDEX_TOPIC = "house_build";

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private HouseDetailRepository houseDetailRepository;
    @Autowired
    private HouseTagRepository houseTagRepository;

    @Autowired
    private TransportClient esClient;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = INDEX_TOPIC)
    private void handleMessage(String content) {
        try {
            HouseIndexMessage message = objectMapper.readValue(content, HouseIndexMessage.class);

            switch (message.getOperation()) {
                case HouseIndexMessage.INDEX:
                    this.createOrUpdateIndex(message);
                    break;
                case HouseIndexMessage.REMOVE:
                    this.removeIndex(message);
                    break;
                default:
                    logger.warn("Not support message content " + content);
                    break;
            }
        } catch (IOException e) {
            logger.error("Cannot parse json for " + content, e);
        }
    }

    private void createOrUpdateIndex(HouseIndexMessage message) {
        Long houseId = message.getHouseId();
        House house = houseRepository.findById(houseId).get();
        if (house == null) {
            logger.error("Index house{}dose not exist!", houseId);
            this.index(houseId, message.getRetry() + 1);
            return;
        }
        HouseIndexTemplate indexTemplate = new HouseIndexTemplate();
        modelMapper.map(house, indexTemplate);

        HouseDetail detail = houseDetailRepository.findByHouseId(houseId);
        if (detail == null) {
            //TODO异常情况
        }
        modelMapper.map(detail, indexTemplate);
        List<HouseTag> tags = houseTagRepository.findAllByHouseId(houseId);
        if (tags != null && !tags.isEmpty()) {
            List<String> tagStrings = new ArrayList<>();
            tags.forEach(houseTag -> {
                tagStrings.add(houseTag.getName());
                indexTemplate.setTags(tagStrings);
            });
        }
        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(INDEX_NAME).setTypes(INDEX_TYPE)
                .setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId));

        logger.debug(requestBuilder.toString());
        SearchResponse searchResponse = requestBuilder.get();

        boolean success;
        long totalHit = searchResponse.getHits().getTotalHits();
        if (totalHit == 0) {
            success = create(indexTemplate);
        } else if (totalHit == 1) {
            String esId = searchResponse.getHits().getAt(0).getId();
            success = update(esId, indexTemplate);
        } else {
            success = deleteAndCreate(totalHit, indexTemplate);
        }
        if (success) {
            logger.debug("Index success with house " + houseId);
        }
    }

    private void removeIndex(HouseIndexMessage message) {
        Long houseId = message.getHouseId();
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(esClient)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId))
                .source(INDEX_NAME);

        logger.debug("Delete by query for house: " + builder);
        BulkByScrollResponse response = builder.get();
        long deleted = response.getDeleted();
        logger.debug("Delete total " + deleted);
        if (deleted <=0) {
            this.remove(houseId, message.getRetry());
        }
    }


    @Override
    public void index(Long houseId) {
        this.index(houseId, 0);
    }

    private void index(Long houseId, int retry) {
        if (retry > HouseIndexMessage.MAX_RETRY) {
            logger.error("Retry index times over 3 for house: " + houseId + "Please check it!");
            return;
        }
        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.INDEX, retry);

        try {
            kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            logger.error("Json encode error for " + message);
        }
    }


    private boolean create(HouseIndexTemplate indexTemplate) {
        try {
            IndexResponse response = this.esClient.prepareIndex(INDEX_NAME, INDEX_TYPE)
                    .setSource(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON).get();
            logger.debug("Create index with house: " + indexTemplate.getHouseId());
            if (response.status() == RestStatus.CREATED) {
                return true;
            } else {
                return false;
            }
        } catch (JsonProcessingException e) {
            logger.error("Error to index house " + indexTemplate.getHouseId());
            return false;
        }
    }

    private boolean update(String esId, HouseIndexTemplate indexTemplate) {
        try {
            UpdateResponse response = this.esClient.prepareUpdate(INDEX_NAME, INDEX_TYPE, esId)
                    .setDoc(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON).get();
            logger.debug("Update index with house: " + indexTemplate.getHouseId());
            if (response.status() == RestStatus.OK) {
                return true;
            } else {
                return false;
            }
        } catch (JsonProcessingException e) {
            logger.error("Error to index house " + indexTemplate.getHouseId());
            return false;
        }
    }

    private boolean deleteAndCreate(long totalHit, HouseIndexTemplate indexTemplate) {
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(esClient)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, indexTemplate.getHouseId()))
                .source(INDEX_NAME);

        logger.debug("Delete by query for house: " + builder);
        BulkByScrollResponse response = builder.get();
        long deleted = response.getDeleted();
        if (deleted != totalHit) {
            logger.warn("Need delete {},but {} was deleted!", totalHit, deleted);
            return false;
        } else {
            return create(indexTemplate);
        }
    }

    @Override
    public void remove(Long houseId) {
//        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
//                .newRequestBuilder(esClient)
//                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId))
//                .source(INDEX_NAME);
//
//        logger.debug("Delete by query for house: " + builder);
//        BulkByScrollResponse response = builder.get();
//        long deleted = response.getDeleted();
//        logger.debug("Delete total "+deleted);

        this.remove(houseId, 0);
    }

    public void remove(Long houseId, int retry) {
        if (retry > HouseIndexMessage.MAX_RETRY) {
            logger.error("Retry remove times over 3 for house: " + houseId + "Please check it!");
            return;
        }
        HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.REMOVE, retry);
        try {
            this.kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            logger.error("Cannot encode json for" + message,e);
        }
    }

    @Override
    public ServiceMultiResult<Long> query(RentSearch rentSearch) {
        return null;
    }

    @Override
    public ServiceResult<List<String>> suggest(String prefix) {
        return null;
    }
}
