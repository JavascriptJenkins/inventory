package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.DeliveryVO;
import com.techvvs.inventory.model.LocationVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepo extends JpaRepository<LocationVO, Integer> {


    List<LocationVO> findAll();
    List<LocationVO> findAllByName(String name);
    List<LocationVO> findAllByDescription(String name);



}
