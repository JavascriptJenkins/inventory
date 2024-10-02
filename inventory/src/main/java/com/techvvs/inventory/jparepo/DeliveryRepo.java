package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.CrateVO;
import com.techvvs.inventory.model.DeliveryVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepo extends JpaRepository<DeliveryVO, Integer> {


    List<DeliveryVO> findAll();
    List<DeliveryVO> findAllByName(String name);
    List<DeliveryVO> findAllByDescription(String name);
    Page<DeliveryVO> findAllByIsprocessed(int isprocessed, Pageable pageable);

    Optional<DeliveryVO> findByDeliveryid(Integer deliveryid);
    //Page<TransactionVO> findByCustomervo_customerid(Integer customerid, Pageable pageable);



}
