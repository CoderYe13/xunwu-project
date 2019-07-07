package com.henu.service.house;

import com.henu.base.HouseSort;
import com.henu.base.HouseStatus;
import com.henu.base.LoginUserUtil;
import com.henu.base.RentValueBlock;
import com.henu.entity.*;
import com.henu.repository.*;
import com.henu.service.IHouseService;
import com.henu.service.ServiceMultiResult;
import com.henu.service.ServiceResult;
import com.henu.service.search.ISearchService;
import com.henu.web.dto.HouseDTO;
import com.henu.web.dto.HouseDetailDTO;
import com.henu.web.dto.HousePictureDTO;
import com.henu.web.form.DatatableSearch;
import com.henu.web.form.HouseForm;
import com.henu.web.form.PhotoForm;
import com.henu.web.form.RentSearch;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.*;

@Service
public class HouseServiceImpl implements IHouseService {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private HouseRepository houseRepository;
    @Autowired
    private SubwayRepository subwayRepository;
    @Autowired
    private HousePictureRepository housePictureRepository;
    @Autowired
    private HouseTagRepository houseTagRepository;
    @Autowired
    private SubwayStationRepository subwayStationRepository;
    @Autowired
    private HouseDetailRepository houseDetailRepository;

    @Autowired
    private ISearchService searchService;

    @Override
    public ServiceResult<HouseDTO> save(HouseForm houseForm) {
        HouseDetail detail = new HouseDetail();
        ServiceResult<HouseDTO> subwayValidtionResult = wrapperDetailInfo(detail, houseForm);

        if (subwayValidtionResult != null) {
            return subwayValidtionResult;
        }
        House house = new House();
        modelMapper.map(houseForm, house);//将houseForm的数据map到house中对应的字段

        Date now = new Date();
        house.setCreateTime(now);
        house.setLastUpdateTime(now);
        house.setAdminId(LoginUserUtil.getLoginUserId());
        houseRepository.save(house);

        detail.setHouseId(house.getId());
        detail = houseDetailRepository.save(detail);

        List<HousePicture> pictures = generatePicture(houseForm, house.getId());
        Iterable<HousePicture> housePictures = housePictureRepository.saveAll(pictures);

        HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
        HouseDetailDTO houseDetailDTO = modelMapper.map(detail, HouseDetailDTO.class);

        houseDTO.setHouseDetail(houseDetailDTO);
        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        housePictures.forEach(housePicture -> pictureDTOS.add(
                modelMapper.map(housePicture, HousePictureDTO.class)
        ));
        houseDTO.setPictures(pictureDTOS);
        houseDTO.setCover("www.henuer.cn" + houseDTO.getCover());

        List<String> tags = houseForm.getTags();
        if (tags != null || !tags.isEmpty()) {
            List<HouseTag> houseTags = new ArrayList<>();
            for (String tag : tags) {
                houseTags.add(new HouseTag(house.getId(), tag));
            }
            houseTagRepository.saveAll(houseTags);
            houseDTO.setTags(tags);
        }

        return new ServiceResult<HouseDTO>(true, null, houseDTO);
    }

    private List<HousePicture> generatePicture(HouseForm form, Long houseId) {
        List<HousePicture> pictures = new ArrayList<>();
        if (form.getPhotos() == null || form.getPhotos().isEmpty()) {
            return pictures;
        }
        for (PhotoForm photoForm : form.getPhotos()) {
            HousePicture picture = new HousePicture();
            picture.setHouseId(houseId);
            picture.setCdnPrefix("www.henuer.cn");
            picture.setPath(photoForm.getPath());
            picture.setWidth(photoForm.getWidth());
            picture.setHeight(photoForm.getHeight());
        }
        return pictures;
    }

    private ServiceResult<HouseDTO> wrapperDetailInfo(HouseDetail houseDetail, HouseForm houseForm) {
        Subway subway = subwayRepository.findById(houseForm.getSubwayLineId()).get();
        if (subway == null) {
            return new ServiceResult<>(false, "Not valid subway line.");
        }
        SubwayStation subwayStation = subwayStationRepository.findById(houseForm.getSubwayStationId()).get();
        if (subwayStation == null || subway.getId() != subwayStation.getSubwayId()) {
            return new ServiceResult<>(false, "Not valid subwayStation.");

        }
        houseDetail.setSubwayLineId(subway.getId());
        houseDetail.setSubwayLineName(subway.getName());

        houseDetail.setSubwayStationId(subwayStation.getId());
        houseDetail.setSubwayStationName(subwayStation.getName());

        houseDetail.setDescription(houseForm.getDescription());
        houseDetail.setDetailAddress(houseForm.getDetailAddress());
        houseDetail.setLayoutDesc(houseForm.getLayoutDesc());
        houseDetail.setRentWay(houseForm.getRentWay());
        houseDetail.setRoundService(houseForm.getRoundService());
        houseDetail.setTraffic(houseForm.getTraffic());
        return null;
    }

    @Override
    public ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody) {
        List<HouseDTO> houseDTOS = new ArrayList<>();

        Sort sort = new Sort(Sort.Direction.fromString(searchBody.getDirection()), searchBody.getOrderBy());
        int page = searchBody.getStart() / searchBody.getLength();
        Pageable pageable = new PageRequest(page, searchBody.getLength(), sort);

        Specification<House> specification = (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("adminId"), LoginUserUtil.getLoginUserId());
            predicate = cb.and(predicate, cb.notEqual(root.get("status"), HouseStatus.DELETED.getValue()));

            if (searchBody.getCity() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("cityEnName"), searchBody.getCity()));
            }
            if (searchBody.getStatus() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), searchBody.getStatus()));
            }
            if (searchBody.getCreateTimeMin() != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createTime"), searchBody.getCreateTimeMin()));
            }
            if (searchBody.getCreateTimeMax() != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createTime"), searchBody.getCreateTimeMax()));
            }
            if (searchBody.getTitle() != null) {
                predicate = cb.and(predicate, cb.like(root.get("title"), "%" + searchBody.getTitle() + "%"));
            }

            return predicate;
        };

        Page<House> houses = houseRepository.findAll(specification, pageable);

        //Iterable<House>houses=houseRepository.findAll();
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover("www.henuer.cn" + house.getCover());
            houseDTOS.add(houseDTO);
        });
        //return new ServiceMultiResult<>(houseDTOS.size(),houseDTOS);

        return new ServiceMultiResult<>(houses.getTotalElements(), houseDTOS);
    }

    @Override
    public ServiceResult<HouseDTO> findCompletaOne(Long id) {
        House house = houseRepository.findById(id).get();//不同的方式获取一个对象
        if (house == null) {
            return ServiceResult.notFound();
        }
        HouseDetail detail = houseDetailRepository.findByHouseId(id);
        List<HousePicture> pictures = housePictureRepository.findAllByHouseId(id);

        HouseDetailDTO detailDTO = modelMapper.map(detail, HouseDetailDTO.class);

        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        for (HousePicture picture : pictures) {
            HousePictureDTO pictureDTO = modelMapper.map(picture, HousePictureDTO.class);
            pictureDTOS.add(pictureDTO);
        }

        List<HouseTag> tags = houseTagRepository.findAllByHouseId(id);
        List<String> tagList = new ArrayList<>();
        for (HouseTag tag : tags) {
            tagList.add(tag.getName());
        }

        HouseDTO result = modelMapper.map(house, HouseDTO.class);
        result.setHouseDetail(detailDTO);
        result.setPictures(pictureDTOS);
        result.setTags(tagList);

        return ServiceResult.of(result);
    }

    @Override
    @Transactional
    public ServiceResult update(HouseForm houseForm) {
        House house = this.houseRepository.findById(houseForm.getId()).get();
        if (house == null) {
            return ServiceResult.notFound();
        }

        HouseDetail detail = this.houseDetailRepository.findByHouseId(house.getId());
        if (detail == null) {
            return ServiceResult.notFound();
        }
        ServiceResult wrapperResult = wrapperDetailInfo(detail, houseForm);
        if (wrapperResult != null) {
            return wrapperResult;
        }
        houseDetailRepository.save(detail);
//        List<HousePicture> pictures=generatePicture(houseForm,houseForm.getId());
//        housePictureRepository.saveAll(pictures);
//
//        if (houseForm.getCover()==null){
//            houseForm.setCover(house.getCover());
//        }
        modelMapper.map(houseForm, house);
        house.setLastUpdateTime(new Date());
        houseRepository.save(house);

        //构建es索引
        if (house.getStatus() == HouseStatus.PASSES.getValue()) {
            searchService.index(house.getId());
        }
        return ServiceResult.success();
    }

    @Override
    public ServiceResult removePhoto(Long id) {
        return null;
    }

    @Override
    @Transactional
    public ServiceResult updateCover(Long coverId, Long targetId) {
        return null;
    }

    /*
    *        List<String> tags = houseForm.getTags();
            if (tags != null || !tags.isEmpty()) {
                List<HouseTag> houseTags = new ArrayList<>();
                for (String tag : tags) {
                    houseTags.add(new HouseTag(house.getId(), tag));
                }
                houseTagRepository.saveAll(houseTags);
                houseDTO.setTags(tags);
            }

     */
    @Override
    @Transactional
    public ServiceResult addTag(Long houseId, String tag) {
        House house = houseRepository.findById(houseId).get();
        if (house == null || tag == null) {
            return ServiceResult.notFound();
        }
        HouseTag houseTag = new HouseTag(houseId, tag);
        houseTagRepository.save(houseTag);
        return ServiceResult.success();
    }

    @Override
    public ServiceResult removeTag(Long houseId, String tag) {
        House house = houseRepository.findById(houseId).get();
        if (house == null || tag == null) {
            return ServiceResult.notFound();
        }

        HouseTag houseTag = houseTagRepository.findByNameAndHouseId(tag, houseId);
        if (houseTag == null) {
            return new ServiceResult(false, "标签不存在");
        }

        houseTagRepository.deleteById(houseTag.getId());
        return ServiceResult.success();
    }

    @Override
    @Transactional
    public ServiceResult updateStatus(Long id, int status) {
        House house = houseRepository.findById(id).get();
        if (house == null) {
            return ServiceResult.notFound();
        }
        if (house.getStatus() == status) {
            return new ServiceResult(false, "状态没有发生变化");
        }
        if (house.getStatus() == HouseStatus.RENTED.getValue()) {
            return new ServiceResult(false, "已出租的房屋不允许修改");
        }
        if (house.getStatus() == HouseStatus.DELETED.getValue()) {
            return new ServiceResult(false, "已删除的资源不允许修改");
        }
        houseRepository.updateStatus(id, status);

        //上架更新索引，其他情况删除索引
        if (status == HouseStatus.PASSES.getValue()) {
            searchService.index(id);
        } else {
            searchService.remove(id);
        }
        return ServiceResult.success();
    }

    /**
     * 房源搜索
     *
     * @param rentSearch
     * @return
     */
    @Override
    public ServiceMultiResult<HouseDTO> query(RentSearch rentSearch) {
        //使用es进行查询
        if (rentSearch.getKeywords() != null && !rentSearch.getKeywords().isEmpty()) {
            ServiceMultiResult<Long> serviceResult = searchService.query(rentSearch);
            if (serviceResult.getTotal() == 0) {
                return new ServiceMultiResult<>(0, new ArrayList<>());
            }
            return new ServiceMultiResult<>(serviceResult.getTotal(),
                    wrapperHouseResult(serviceResult.getResult()));
        }
        //如果es中数据为空，直接从数据库中查询
        return simpleQuery(rentSearch);
    }

    //按照houseId转换出house对象
    private List<HouseDTO> wrapperHouseResult(List<Long> houseIds) {
        List<HouseDTO> result = new ArrayList<>();

        Map<Long, HouseDTO> idToHouseMap = new HashMap<>();

        Iterable<House> houses = houseRepository.findAllById(houseIds);
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover("www.henu.cn");
            idToHouseMap.put(house.getId(), houseDTO);
        });
        wrapperHouseList(houseIds, idToHouseMap);

        //矫正顺序
        for (Long houseId : houseIds) {
            result.add(idToHouseMap.get(houseId));
        }
        return result;
    }


    //从数据库中查询
    private ServiceMultiResult<HouseDTO> simpleQuery(RentSearch rentSearch) {

        //Sort sort = new Sort(Sort.Direction.DESC, "lastUpdateTime");
        Sort sort = HouseSort.generateSort(rentSearch.getOrderBy(), rentSearch.getOrderDirection());
        int page = rentSearch.getStart() / rentSearch.getSize();
        Pageable pageable = new PageRequest(page, rentSearch.getSize(), sort);
        Specification<House> specification = ((root, criteriaQuery, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.equal(root.get("status"), HouseStatus.PASSES.getValue());
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("cityEnName"), rentSearch.getCityEnName()));

            if (HouseSort.DISTANCE_TO_SUBWAY_KEY.equals(rentSearch.getOrderBy())) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.gt(root.get(HouseSort.DISTANCE_TO_SUBWAY_KEY), -1));
            }

            if (rentSearch.getPriceBlock() != null&&rentSearch.getPriceBlock().length()>1) {
                String[] prices = rentSearch.getPriceBlock().split("\\-");
                System.out.println(prices[0]+"  "+prices[1]);
                if (prices.length==2) {
                    if (prices[0].equals("*")) {
                        predicate = criteriaBuilder.and(predicate, criteriaBuilder.le(root.get("price"), Integer.valueOf(prices[1])));
                    } else {
                        if (prices[1].equals("*")) {
                            predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("price"), Integer.valueOf(prices[0])));
                        } else {
                            predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("price"), Integer.valueOf(prices[0])),criteriaBuilder.lessThanOrEqualTo(root.get("price"), Integer.valueOf(prices[1])));
                        }
                    }
                }
            }

            return predicate;
        });
        System.out.println("开始");
        Page<House> houses = houseRepository.findAll(specification, pageable);
        System.out.println("结束");
        List<HouseDTO> houseDTOS = new ArrayList<>();

        List<Long> houseIds = new ArrayList<>();

        Map<Long, HouseDTO> idToHouseMap = new HashMap<>();
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover("www.henuer.cn" + house.getCover());
            houseDTOS.add(houseDTO);
            houseIds.add(house.getId());
            idToHouseMap.put(house.getId(), houseDTO);
        });
        wrapperHouseList(houseIds, idToHouseMap);
        return new ServiceMultiResult<>(houses.getTotalElements(), houseDTOS);
    }

    /**
     * 渲染详细信息及标签
     *
     * @param houseIds
     * @param idToHouseMap
     */
    private void wrapperHouseList(List<Long> houseIds, Map<Long, HouseDTO> idToHouseMap) {
        List<HouseDetail> details = houseDetailRepository.findAllByHouseIdIn(houseIds);
        details.forEach(houseDetail -> {
            HouseDTO houseDTO = idToHouseMap.get(houseDetail.getHouseId());
            HouseDetailDTO detailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);
            houseDTO.setHouseDetail(detailDTO);
        });
        List<HouseTag> houseTags = houseTagRepository.findAllByHouseIdIn(houseIds);
        houseTags.forEach(houseTag -> {
            HouseDTO house = idToHouseMap.get(houseTag.getHouseId());
            house.getTags().add(houseTag.getName());
        });
    }
}
