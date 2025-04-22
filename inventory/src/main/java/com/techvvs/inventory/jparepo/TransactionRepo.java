package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.CustomerVO;
import com.techvvs.inventory.model.TransactionVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepo extends JpaRepository<TransactionVO, Integer> {

    List<TransactionVO> findAll();


    List<TransactionVO> findAllByCustomervo(CustomerVO customerVO);

    Page<TransactionVO> findByCustomervo_customerid(Integer customerid, Pageable pageable);

    @Query("""
    SELECT DISTINCT t
    FROM TransactionVO t
    JOIN t.product_list p
    WHERE (:customerid IS NULL OR t.customervo.customerid = :customerid)
      AND (:productid IS NULL OR :productid = 0 OR p.product_id = :productid)
      AND (:batchid IS NULL OR :batchid = 0 OR p.batch.batchid = :batchid)
    """)
    Page<TransactionVO> findFilteredTransactions(
            @Param("customerid") Integer customerid,
            @Param("productid") Integer productid,
            @Param("batchid") Integer batchid,
            Pageable pageable
    );


    // this query drives the graphing behavior.  will get the daily amount that was actually paid
    @Query("""
    SELECT cast(t.createTimeStamp as date) as txDate, SUM(t.paid)
    FROM TransactionVO t
    JOIN t.product_list p
    WHERE t.createTimeStamp BETWEEN :startDate AND :endDate
      AND (:customerid IS NULL OR t.customervo.customerid = :customerid)
      AND (:productid IS NULL OR :productid = 0 OR p.product_id = :productid)
      AND (:batchid IS NULL OR :batchid = 0 OR p.batch.batchid = :batchid)
    GROUP BY cast(t.createTimeStamp as date)
    ORDER BY txDate ASC
""")
    List<Object[]> findDailyRevenueByFilters(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("customerid") Integer customerid,
            @Param("productid") Integer productid,
            @Param("batchid") Integer batchid
    );





    // this is a great query that finds a list of unpaid transactions that a product is in based on batchid and productid
//    SELECT DISTINCT t.transactionid
//    FROM transaction t
//    JOIN TRANSACTION_PRODUCT tp ON t.transactionid = tp.transactionid
//    JOIN BATCH_PRODUCT_SET bps ON tp.productid = bps.PRODUCT_SET_PRODUCT_ID
//    WHERE t.PAID < t.TOTALWITHTAX
//    AND tp.productid = 16
//    AND bps.BATCHVO_BATCHID = 13;


    @Query(value = "SELECT DISTINCT t.* " +
            "FROM transaction t " +
            "JOIN TRANSACTION_PRODUCT tp ON t.transactionid = tp.transactionid " +
            "JOIN BATCH_PRODUCT_SET bps ON tp.productid = bps.PRODUCT_SET_PRODUCT_ID " +
            "WHERE t.PAID < t.TOTALWITHTAX " +
            "AND tp.productid = :productId " +
            "AND bps.BATCHVO_BATCHID = :batchid",
            countQuery = "SELECT COUNT(DISTINCT t.transactionid) " +
                    "FROM transaction t " +
                    "JOIN TRANSACTION_PRODUCT tp ON t.transactionid = tp.transactionid " +
                    "JOIN BATCH_PRODUCT_SET bps ON tp.productid = bps.PRODUCT_SET_PRODUCT_ID " +
                    "WHERE t.PAID < t.TOTALWITHTAX " +
                    "AND tp.productid = :productId " +
                    "AND bps.BATCHVO_BATCHID = :batchid",
            nativeQuery = true)
    Page<TransactionVO> findDistinctTransactionsByProductIdAndBatchId(@Param("productId") int productId,
                                                                      @Param("batchid") int batchid,
                                                                      Pageable pageable);



}
