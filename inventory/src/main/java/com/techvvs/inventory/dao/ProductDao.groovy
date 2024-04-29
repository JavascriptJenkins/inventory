package com.techvvs.inventory.dao

import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.LocalDateTime


// orchestration logic for data access layer
@Component
class ProductDao {

    @Autowired
    ProductRepo productRepo;

    @Autowired
    ProductTypeRepo productTypeRepo;

    ProductVO updateProduct(ProductVO productVO){
        ProductVO existing
        try{
//
            existing = productRepo.findById(productVO.product_id).get()
            existing = productVO
            existing.setUpdateTimeStamp(LocalDateTime.now());
        } catch(Exception ex) {
            System.out.println("Caught Error: " + ex.getCause())
        } finally {
            productVO = productRepo.save(existing)
        }

        return productVO
    }




}