package com.henu.service.house;

import com.henu.base.LoginUserUtil;
import com.henu.entity.House;
import com.henu.repository.HouseRepository;
import com.henu.service.IHouseService;
import com.henu.service.ServiceResult;
import com.henu.web.dto.HouseDTO;
import com.henu.web.form.HouseForm;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class HouseServiceImpl implements IHouseService {
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private HouseRepository houseRepository;
    @Override
    public ServiceResult<HouseDTO> save(HouseForm houseForm) {
        House house=new House();
        modelMapper.map(houseForm,house);//将houseForm的数据map到house中对应的字段

        Date now=new Date();
        house.setCreateTime(now);
        house.setLastUpdateTime(now);
        house.setAdminId(LoginUserUtil.getLoginUserId());
        houseRepository.save(house);
        return null;
    }
}
