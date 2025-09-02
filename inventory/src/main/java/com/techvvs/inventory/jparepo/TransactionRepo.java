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
import java.util.Optional;

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
    """)
    Page<TransactionVO> findFilteredTransactions(
            @Param("customerid") Integer customerid,
            @Param("productid") Integer productid,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT t
    FROM TransactionVO t
    JOIN t.product_list p
    WHERE (:customerid IS NULL OR t.customervo.customerid = :customerid)
      AND (:productid IS NULL OR :productid = 0 OR p.product_id = :productid)
      AND t.paid < t.totalwithtax
      AND NOT EXISTS (
        SELECT p2 FROM PaymentVO p2 
        WHERE p2.transaction = t 
        AND p2.createTimeStamp >= :cutoffDate
      )
    """)
    Page<TransactionVO> findUnderpaidTransactions(
            @Param("customerid") Integer customerid,
            @Param("productid") Integer productid,
            @Param("cutoffDate") LocalDateTime cutoffDate,
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
    GROUP BY cast(t.createTimeStamp as date)
    ORDER BY txDate ASC
""")
    List<Object[]> findDailyRevenueByFilters(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("customerid") Integer customerid,
            @Param("productid") Integer productid
    );



    @Query("""
    SELECT cast(t.createTimeStamp as date), SUM(t.paid)
    FROM TransactionVO t
    WHERE t.createTimeStamp BETWEEN :startDate AND :endDate
      AND (:customerid IS NULL OR t.customervo.customerid = :customerid)
    GROUP BY cast(t.createTimeStamp as date)
    ORDER BY cast(t.createTimeStamp as date) ASC
""")
    List<Object[]> findRevenueWithoutJoin(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("customerid") Integer customerid
    );


    @Query("""
    SELECT cast(t.createTimeStamp as date), SUM(t.paid)
    FROM TransactionVO t
    WHERE t.transactionid IN (
        SELECT DISTINCT t2.transactionid
        FROM TransactionVO t2
        JOIN t2.product_list p
        WHERE (:productid IS NULL OR :productid = 0 OR p.product_id = :productid)
    )
    AND t.createTimeStamp BETWEEN :startDate AND :endDate
    AND (:customerid IS NULL OR t.customervo.customerid = :customerid)
    GROUP BY cast(t.createTimeStamp as date)
    ORDER BY cast(t.createTimeStamp as date) ASC
""")
    List<Object[]> findRevenueWithJoin(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("customerid") Integer customerid,
            @Param("productid") Integer productid
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


    Optional<TransactionVO> findByPaypalOrderId(String paypalOrderId);

    // Query to find the most purchased products based on transaction_product table
    @Query("""
        SELECT p, COUNT(t) as purchaseCount
        FROM ProductVO p
        JOIN p.transaction_list t
        GROUP BY p.product_id, p.name, p.barcode, p.price, p.quantity, p.quantityremaining, p.createTimeStamp, p.updateTimeStamp, p.description, p.laborCostPricePerUnit, p.marginPercent, p.notes, p.productnumber, p.salePrice
        ORDER BY purchaseCount DESC
    """)
    List<Object[]> findMostPurchasedProducts();


}
