package com.henu.repository;

import java.util.List;
import java.util.Optional;

import com.henu.entity.Subway;
import org.springframework.data.repository.CrudRepository;


/**
 * Created by 瓦力.
 */
public interface SubwayRepository extends CrudRepository<Subway, Long>{
    List<Subway> findAllByCityEnName(String cityEnName);

    Optional<Subway> findById(Long subwayId);
}
