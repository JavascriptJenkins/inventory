package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.ProductVO;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductVO, Integer> {

    List<ProductVO> findAll();

}