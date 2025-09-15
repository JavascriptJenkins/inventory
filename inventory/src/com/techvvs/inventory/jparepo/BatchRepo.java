package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.BatchVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface BatchRepo extends JpaRepository<BatchVO, Integer> {

    @Transactional
    void deleteByName(String name);

    List<BatchVO> findAll();
    List<BatchVO> findAllByOrderByCreateTimeStampDescNameAsc();
    List<BatchVO> findAllByBatchnumber(Integer batchnumber);
    BatchVO findByBatchid(Integer batchid);
    BatchVO findByName(String name);
    boolean existsByBatchnumber(int batchnumber);
    List<BatchVO> findAllByName(String name);
    List<BatchVO> findAllByDescription(String name);
    
    // Filtering method following TransactionRepo pattern
    @Query("""
    SELECT DISTINCT b
    FROM BatchVO b
    LEFT JOIN b.product_set p
    WHERE (:vendorid IS NULL OR p.vendorvo.vendorid = :vendorid)
      AND (:productTypeId IS NULL OR p.producttypeid.producttypeid = :productTypeId)
      AND (:batchTypeId IS NULL OR b.batch_type_id.batch_type_id = :batchTypeId)
    """)
    Page<BatchVO> findFilteredBatches(
            @Param("vendorid") Integer vendorid,
            @Param("productTypeId") Integer productTypeId,
            @Param("batchTypeId") Integer batchTypeId,
            Pageable pageable
    );

}
