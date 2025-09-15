package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.PackageTypeVO;
import com.techvvs.inventory.model.ProductTypeVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PackageTypeRepo extends JpaRepository<PackageTypeVO, Integer> {

    List<PackageTypeVO> findAll();
    Optional<PackageTypeVO> findByName(String name);



}
