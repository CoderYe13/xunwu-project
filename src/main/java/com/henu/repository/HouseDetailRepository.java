package com.henu.repository;

import com.henu.entity.HouseDetail;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HouseDetailRepository extends CrudRepository<HouseDetail,Long> {
    HouseDetail findByHouseId(Long id);

    List<HouseDetail> findAllByHouseIdIn(List<Long> houseIds);
}
