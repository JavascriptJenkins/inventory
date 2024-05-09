package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.ProductTypeVO;
import com.techvvs.inventory.model.ProductVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepo extends JpaRepository<ProductVO, Integer> {

    List<ProductVO> findAll();
    List<ProductVO> findAllByProductnumber(Integer productnumber);
    List<ProductVO> findAllByName(String name);
    Page<ProductVO> findAllByProducttypeid(ProductTypeVO producttypeid, Pageable pageable);
    Page<ProductVO> findAllByNameContaining(String searchTerm, Pageable pageable);
    List<ProductVO> findAllByDescription(String desc);

}
