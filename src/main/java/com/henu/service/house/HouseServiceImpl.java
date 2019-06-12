package com.henu.service.house;

import com.henu.base.LoginUserUtil;
import com.henu.entity.*;
import com.henu.repository.*;
import com.henu.service.IHouseService;
import com.henu.service.ServiceResult;
import com.henu.web.dto.HouseDTO;
import com.henu.web.dto.HouseDetailDTO;
import com.henu.web.dto.HousePictureDTO;
import com.henu.web.form.HouseForm;
import com.henu.web.form.PhotoForm;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        detail=houseDetailRepository.save(detail);

        List<HousePicture> pictures=generatePicture(houseForm,house.getId());
       Iterable<HousePicture> housePictures= housePictureRepository.saveAll(pictures);

       HouseDTO houseDTO=modelMapper.map(house,HouseDTO.class);
        HouseDetailDTO houseDetailDTO=modelMapper.map(detail,HouseDetailDTO.class);

        houseDTO.setHouseDetail(houseDetailDTO);
        List<HousePictureDTO> pictureDTOS=new ArrayList<>();
        housePictures.forEach(housePicture ->pictureDTOS.add(
                modelMapper.map(housePicture,HousePictureDTO.class)
        ) );
        houseDTO.setPictures(pictureDTOS);
        houseDTO.setCover("www.henuer.cn"+houseDTO.getCover());

        List<String> tags=houseForm.getTags();
        if(tags!=null||!tags.isEmpty()){
            List<HouseTag> houseTags=new ArrayList<>();
            for(String tag: tags){
                houseTags.add(new HouseTag(house.getId(),tag));
            }
            houseTagRepository.saveAll(houseTags);
            houseDTO.setTags(tags);
        }

        return new ServiceResult<HouseDTO>(true,null,houseDTO);
    }

    private List<HousePicture> generatePicture(HouseForm form,Long houseId){
        List<HousePicture> pictures=new ArrayList<>();
        if(form.getPhotos()==null|| form.getPhotos().isEmpty()){
            return pictures;
        }
        for(PhotoForm photoForm:form.getPhotos()){
            HousePicture picture=new HousePicture();
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
        if (subwayStation==null||subway.getId()!=subwayStation.getSubwayId()){
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
}
