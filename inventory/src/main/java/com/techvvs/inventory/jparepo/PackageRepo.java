package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PackageRepo extends JpaRepository<PackageVO, Integer> {


    List<PackageVO> findAll();
    List<PackageVO> findAllByName(String name);
    List<PackageVO> findAllByDescription(String name);
    Optional<PackageVO> findByPackagebarcode(String barcode);

    Page<PackageVO> findAllByCrate(CrateVO crateVO, Pageable pageable);
    Page<PackageVO> findAllByIsprocessed(int isprocessed, Pageable pageable);



}
