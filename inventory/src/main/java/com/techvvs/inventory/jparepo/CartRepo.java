package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.BatchVO;
import com.techvvs.inventory.model.CartVO;
import com.techvvs.inventory.model.PackageVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface CartRepo extends JpaRepository<CartVO, Integer> {


    List<CartVO> findAll();
    Page<CartVO> findAllByIsprocessed(int isprocessed, Pageable pageable);



}
