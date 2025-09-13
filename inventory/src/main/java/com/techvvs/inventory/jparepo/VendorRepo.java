package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.VendorVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorRepo extends JpaRepository<VendorVO, Integer> {

    List<VendorVO> findAllByOrderByNameAsc();

}
