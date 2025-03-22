package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.BatchVO;
import com.techvvs.inventory.model.ProductTypeVO;
import com.techvvs.inventory.model.ProductVO;
import com.techvvs.inventory.model.TransactionVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepo extends JpaRepository<ProductVO, Integer> {

    @Query("SELECT p FROM ProductVO p ORDER BY p.name ASC")
    List<ProductVO> findAllSortedByName();

    List<ProductVO> findAll();

    // or descending by timestamp
    List<ProductVO> findAllByOrderByCreateTimeStampDescNameAsc();
    List<ProductVO> findAllByProductnumber(Integer productnumber);
    List<ProductVO> findAllByName(String name);
    Page<ProductVO> findAllByProducttypeidAndBatch(ProductTypeVO producttypeid, BatchVO batchVO, Pageable pageable);

    Page<ProductVO> findAllByNameContainingAndBatch(String searchTerm, BatchVO batchVO, Pageable pageable);


    // case insensitive search that will work with postgresql and also h2 database
    @Query("SELECT p FROM ProductVO p " +
            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "AND p.quantityremaining > 0")
    Page<ProductVO> searchProductsIgnoreCase(
            @Param("searchTerm") String searchTerm,
            Pageable pageable);


    List<ProductVO> findAllByDescription(String desc);

    Page<ProductVO> findAllByBatch(BatchVO batchVO, Pageable pageable);

    @Query("SELECT p FROM ProductVO p WHERE p.batch = :batchVO AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<ProductVO> searchByBatchAndName(@Param("batchVO") BatchVO batchVO, @Param("name") String name, Pageable pageable);

//    Page<ProductVO> findAllByTransaction(TransactionVO transactionVO, Pageable pageable);

    Optional<ProductVO> findByBarcode(String barcode);

    boolean existsByProductnumber(int productnumber);
    boolean existsByBarcode(String barcode);

    @Query(value = "SELECT SUM(p.quantityremaining) FROM product p " +
            "JOIN BATCH_PRODUCT_SET bps ON p.product_id = bps.PRODUCT_SET_PRODUCT_ID " +
            "WHERE bps.BATCHVO_BATCHID = :batchid", nativeQuery = true)
    int selectCountOfProductsRemainingInBatch(@Param("batchid") int batchid);


    // This finds a list of pageable productVOs that are in a batchid and not paid for
    @Query(value = "SELECT p.* " +
            "FROM product p " +
            "JOIN TRANSACTION_PRODUCT tp ON p.product_id = tp.productid " +
            "JOIN TRANSACTION t ON tp.transactionid = t.transactionid " +
            "JOIN BATCH_PRODUCT_SET bps ON tp.productid = bps.PRODUCT_SET_PRODUCT_ID " +
            "WHERE t.PAID < t.TOTALWITHTAX " +
            "AND bps.BATCHVO_BATCHID = :batchid",
            countQuery = "SELECT COUNT(p.product_id) " +
                    "FROM product p " +
                    "JOIN TRANSACTION_PRODUCT tp ON p.product_id = tp.productid " +
                    "JOIN TRANSACTION t ON tp.transactionid = t.transactionid " +
                    "JOIN BATCH_PRODUCT_SET bps ON tp.productid = bps.PRODUCT_SET_PRODUCT_ID " +
                    "WHERE t.PAID < t.TOTALWITHTAX " +
                    "AND bps.BATCHVO_BATCHID = :batchid",
            nativeQuery = true)
    Page<ProductVO> findUnpaidProductsByBatchId(@Param("batchid") int batchid, Pageable pageable);





//    @Query("SELECT * FROM Product p JOIN Batch b ON p.product_id = b.product_id WHERE o.customer.name = :name")
//    Page<ProductVO> findByBatch(BatchVO batch, Pageable pageable);


}
