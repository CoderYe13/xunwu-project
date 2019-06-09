package com.henu.repository;

import java.util.List;
import java.util.Optional;

import com.henu.entity.SubwayStation;
import org.springframework.data.repository.CrudRepository;


/**
 * Created by 瓦力.
 */
public interface SubwayStationRepository extends CrudRepository<SubwayStation, Long> {
    List<SubwayStation> findAllBySubwayId(Long subwayId);

    Optional<SubwayStation> findById(Long stationId);
}
