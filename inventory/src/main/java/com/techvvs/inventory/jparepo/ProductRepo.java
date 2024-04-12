package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.BatchVO;
import com.techvvs.inventory.model.ProductVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepo extends JpaRepository<ProductVO, Integer> {

    List<ProductVO> findAll();
    List<ProductVO> findAllByProductnumber(Integer productnumber);
    List<ProductVO> findAllByName(String name);
    List<ProductVO> findAllByDescription(String desc);

}
