package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.AttributeVO;
import com.techvvs.inventory.model.VendorVO;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.management.Attribute;


public interface AttributeRepo extends JpaRepository<AttributeVO, Integer> {

}
