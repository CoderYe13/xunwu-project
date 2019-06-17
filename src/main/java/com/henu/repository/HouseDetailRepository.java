package com.henu.repository;

import com.henu.entity.HouseDetail;
import org.springframework.data.repository.CrudRepository;

public interface HouseDetailRepository extends CrudRepository<HouseDetail,Long> {
    HouseDetail findByHouseId(Long id);
}
