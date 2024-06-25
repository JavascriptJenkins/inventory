package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.ProductTypeVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductTypeRepo extends JpaRepository<ProductTypeVO, Integer> {

    List<ProductTypeVO> findAll();
    Optional<ProductTypeVO> findByName(String name);



}
