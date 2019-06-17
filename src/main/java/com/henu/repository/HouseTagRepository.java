package com.henu.repository;

import com.henu.entity.HouseTag;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HouseTagRepository extends CrudRepository<HouseTag,Long> {
    List<HouseTag> findAllByHouseId(Long id);

    HouseTag findByNameAndHouseId(String tag, Long houseId);
}
