package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.CustomerVO;
import com.techvvs.inventory.model.PaymentVO;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface PaymentRepo extends JpaRepository<PaymentVO, Integer> {

    List<PaymentVO> findAll();

}
