package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.BatchTypeVO;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BatchTypeRepo extends JpaRepository<BatchTypeVO, Integer> {

    @Transactional
    void deleteByName(String name);

    List<BatchTypeVO> findAll();
    List<BatchTypeVO> findAllByName(String name);
    Optional<BatchTypeVO> findByName(String name);
    @Query("SELECT b FROM BatchTypeVO b WHERE b.batch_type_id = :batchTypeId")
    Optional<BatchTypeVO> findByBatchTypeId(@Param("batchTypeId") Integer batchTypeId);
    List<BatchTypeVO> findAllByDescription(String name);

}
