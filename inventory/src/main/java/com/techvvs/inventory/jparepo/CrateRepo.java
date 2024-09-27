package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.CrateVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrateRepo extends JpaRepository<CrateVO, Integer> {


    List<CrateVO> findAll();
    List<CrateVO> findAllByName(String name);
    List<CrateVO> findAllByDescription(String name);
    Page<CrateVO> findAllByIsprocessed(int isprocessed, Pageable pageable);

    //Page<TransactionVO> findByCustomervo_customerid(Integer customerid, Pageable pageable);



}
