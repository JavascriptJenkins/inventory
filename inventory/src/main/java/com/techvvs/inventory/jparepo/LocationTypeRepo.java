package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.LocationTypeVO;
import com.techvvs.inventory.model.LocationVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationTypeRepo extends JpaRepository<LocationTypeVO, Integer> {


    List<LocationTypeVO> findAll();
    List<LocationTypeVO> findAllByName(String name);
    List<LocationTypeVO> findAllByDescription(String name);



}
