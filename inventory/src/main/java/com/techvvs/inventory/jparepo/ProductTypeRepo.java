package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.ProductTypeVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductTypeRepo extends JpaRepository<ProductTypeVO, Integer> {

    List<ProductTypeVO> findAll();
    ProductTypeVO findByName(String name);



}
