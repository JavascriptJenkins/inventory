package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.CrateVO;
import com.techvvs.inventory.model.DeliveryVO;
import com.techvvs.inventory.model.PackageVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CrateRepo extends JpaRepository<CrateVO, Integer> {


    List<CrateVO> findAll();
    List<CrateVO> findAllByName(String name);
    List<CrateVO> findAllByDescription(String name);
    Page<CrateVO> findAllByIsprocessed(int isprocessed, Pageable pageable);
    boolean existsByCratebarcode(String barcode);
    Optional<CrateVO> findByCratebarcode(String barcode);
    Page<CrateVO> findAllByDelivery(DeliveryVO deliveryVO, Pageable pageable);
    List<CrateVO> findAllByDelivery(DeliveryVO deliveryVO);



    //Page<TransactionVO> findByCustomervo_customerid(Integer customerid, Pageable pageable);



}
