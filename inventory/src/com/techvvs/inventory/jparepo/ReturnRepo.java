package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.PaymentVO;
import com.techvvs.inventory.model.ReturnVO;
import com.techvvs.inventory.model.TransactionVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReturnRepo extends JpaRepository<ReturnVO, Integer> {

    List<ReturnVO> findAllByTransaction(TransactionVO transactionVO);

}
