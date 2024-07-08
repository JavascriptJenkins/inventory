package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.BatchVO;
import com.techvvs.inventory.model.ProductTypeVO;
import com.techvvs.inventory.model.ProductVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepo extends JpaRepository<ProductVO, Integer> {

    List<ProductVO> findAll();
    List<ProductVO> findAllByProductnumber(Integer productnumber);
    List<ProductVO> findAllByName(String name);
    Page<ProductVO> findAllByProducttypeidAndBatch(ProductTypeVO producttypeid, BatchVO batchVO, Pageable pageable);

    Page<ProductVO> findAllByNameContainingAndBatch(String searchTerm, BatchVO batchVO, Pageable pageable);

    List<ProductVO> findAllByDescription(String desc);

    Page<ProductVO> findAllByBatch(BatchVO batchVO, Pageable pageable);

    Optional<ProductVO> findByBarcode(String barcode);

//    @Query("SELECT * FROM Product p JOIN Batch b ON p.product_id = b.product_id WHERE o.customer.name = :name")
//    Page<ProductVO> findByBatch(BatchVO batch, Pageable pageable);


}
