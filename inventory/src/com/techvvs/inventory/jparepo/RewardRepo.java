package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.CustomerVO;
import com.techvvs.inventory.model.RewardVO;
import com.techvvs.inventory.model.TransactionVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardRepo extends JpaRepository<RewardVO, Integer> {

    List<RewardVO> findAllByTransaction(TransactionVO transactionVO);

    List<RewardVO> findAllByCustomer(CustomerVO customerVO);

}
