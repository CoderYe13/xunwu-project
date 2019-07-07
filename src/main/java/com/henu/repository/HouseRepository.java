package com.henu.repository;

import com.henu.entity.House;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HouseRepository extends PagingAndSortingRepository<House, Long>
        , JpaSpecificationExecutor<House> {
    @Modifying
    @Query("update House as house set house.status = :status where house.id = :id")
    void updateStatus(@Param(value = "id") Long id, @Param(value = "status") int status);



}
