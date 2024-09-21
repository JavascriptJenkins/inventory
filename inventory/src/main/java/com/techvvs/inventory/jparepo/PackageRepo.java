package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.CartVO;
import com.techvvs.inventory.model.PackageVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PackageRepo extends JpaRepository<PackageVO, Integer> {


    List<PackageVO> findAll();
    List<PackageVO> findAllByName(String name);
    List<PackageVO> findAllByDescription(String name);
    Optional<PackageVO> findByPackagebarcode(String barcode);


}
