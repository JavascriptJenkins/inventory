package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.BatchTypeVO;
import com.techvvs.inventory.model.CustomerVO;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface CustomerRepo extends JpaRepository<CustomerVO, Integer> {

    @Transactional
    void deleteByName(String name);

    List<CustomerVO> findAll();
    Optional<CustomerVO> findByCustomerid(Integer customerid);
    Optional<CustomerVO> findByMembershipnumber(String membershipnumber);

}
