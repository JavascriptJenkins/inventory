package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.CustomerVO;
import com.techvvs.inventory.model.TransactionVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface TransactionRepo extends JpaRepository<TransactionVO, Integer> {

    List<TransactionVO> findAll();


//    List<TransactionVO> findAllByCustomer(CustomerVO customerVO);

}
