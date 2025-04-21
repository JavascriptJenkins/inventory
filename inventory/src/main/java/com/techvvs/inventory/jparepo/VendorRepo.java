package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.VendorVO;
import org.springframework.data.jpa.repository.JpaRepository;


public interface VendorRepo extends JpaRepository<VendorVO, Integer> {

}
