package com.techvvs.inventory.refdata

import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchTypeVO
import com.techvvs.inventory.model.ProductTypeVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.LocalDateTime

// The purpose of this class is to load reference data on startup. Things like BatchTypes and other reference data
@Component
class RefDataLoader {

    @Autowired
    BatchTypeRepo batchTypeRepo

    @Autowired
    ProductTypeRepo productTypeRepo


    void loadRefData(){
        loadBatchTypes()
        loadProductTypes()
    }

    void loadBatchTypes(){
        BatchTypeVO batchTypeVO = new BatchTypeVO();
        batchTypeVO.setBatch_type_id(1) // we are going to manually set primary key on ref data
        batchTypeVO.name = "INDOOR.MIXED"
        batchTypeVO.description = "Mainly indoor but a mixed batch"

        batchTypeVO.setCreateTimeStamp(LocalDateTime.now());
        batchTypeVO.setUpdateTimeStamp(LocalDateTime.now());

        batchTypeRepo.save(batchTypeVO)
        System.out.println("BatchType ref data loaded")
    }

    void loadProductTypes(){

        ProductTypeVO productTypeVO = new ProductTypeVO();
        productTypeVO.producttypeid = 2
        productTypeVO.name = "INDOOR.UNIT"
        productTypeVO.description = "Indoor Unit"

        productTypeVO.setCreateTimeStamp(LocalDateTime.now());
        productTypeVO.setUpdateTimeStamp(LocalDateTime.now());

        productTypeRepo.save(productTypeVO)


        System.out.println("ProductType ref data loaded")
    }



}
