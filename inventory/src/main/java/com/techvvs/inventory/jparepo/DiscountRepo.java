package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.DiscountVO;
import com.techvvs.inventory.model.ReturnVO;
import com.techvvs.inventory.model.TransactionVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscountRepo extends JpaRepository<DiscountVO, Integer> {

    List<DiscountVO> findAllByTransaction(TransactionVO transactionVO);

}
