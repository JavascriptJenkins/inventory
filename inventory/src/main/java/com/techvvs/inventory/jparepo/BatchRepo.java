package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.BatchVO;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface BatchRepo extends JpaRepository<BatchVO, Integer> {

    @Transactional
    void deleteByName(String name);

    List<BatchVO> findAll();
    List<BatchVO> findAllByBatchnumber(Integer batchnumber);
    BatchVO findByBatchid(Integer batchid);
    BatchVO findByName(String name);
    List<BatchVO> findAllByName(String name);
    List<BatchVO> findAllByDescription(String name);

}
