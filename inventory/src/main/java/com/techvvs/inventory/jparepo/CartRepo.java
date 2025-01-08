package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface CartRepo extends JpaRepository<CartVO, Integer> {


    List<CartVO> findAll();
    Page<CartVO> findAllByIsprocessed(int isprocessed, Pageable pageable);
    Optional<CartVO> findByMenuAndCustomer(MenuVO menuVO, CustomerVO customerVO);
    List<CartVO> findAllByMenuAndCustomer(MenuVO menuVO, CustomerVO customerVO);



}
