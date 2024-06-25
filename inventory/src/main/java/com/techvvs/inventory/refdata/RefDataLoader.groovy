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
//        batchTypeVO.setBatch_type_id(1)
        batchTypeVO.name = "INDOOR.MIXED"
        batchTypeVO.description = "Mainly indoor but a mixed batch"

        batchTypeVO.setCreateTimeStamp(LocalDateTime.now());
        batchTypeVO.setUpdateTimeStamp(LocalDateTime.now());

        batchTypeRepo.save(batchTypeVO)
        System.out.println("BatchType ref data loaded")
    }

    void loadProductTypes(){

        ProductTypeVO productTypeVO = new ProductTypeVO();
     //   productTypeVO.producttypeid = 2
        productTypeVO.name = "INDOOR.UNIT"
        productTypeVO.description = "Indoor Unit"

        productTypeVO.setCreateTimeStamp(LocalDateTime.now());
        productTypeVO.setUpdateTimeStamp(LocalDateTime.now());

        productTypeRepo.save(productTypeVO)


        ProductTypeVO productTypeVO2 = new ProductTypeVO();
     //   productTypeVO2.producttypeid = 3
        productTypeVO2.name = "CART.2G.DISPO"
        productTypeVO2.description = "2 gram disposable"

        productTypeVO2.setCreateTimeStamp(LocalDateTime.now());
        productTypeVO2.setUpdateTimeStamp(LocalDateTime.now());

        productTypeRepo.save(productTypeVO2)


        ProductTypeVO productTypeVO3 = new ProductTypeVO();
      //  productTypeVO3.producttypeid = 4
        productTypeVO3.name = "CART.0.5G.ROSIN"
        productTypeVO3.description = ".5 g rosin cart"

        productTypeVO3.setCreateTimeStamp(LocalDateTime.now());
        productTypeVO3.setUpdateTimeStamp(LocalDateTime.now());

        productTypeRepo.save(productTypeVO3)


        ProductTypeVO productTypeVO4 = new ProductTypeVO();
       // productTypeVO4.producttypeid = 5
        productTypeVO4.name = "CART.1G.RESIN"
        productTypeVO4.description = "1G RESIN CART DISPO"

        productTypeVO4.setCreateTimeStamp(LocalDateTime.now());
        productTypeVO4.setUpdateTimeStamp(LocalDateTime.now());

        productTypeRepo.save(productTypeVO4)


        ProductTypeVO productTypeVO5 = new ProductTypeVO();
       // productTypeVO5.producttypeid = 6
        productTypeVO5.name = "BOOMER.UNIT"
        productTypeVO5.description = "UNIT OF BOOMS"

        productTypeVO5.setCreateTimeStamp(LocalDateTime.now());
        productTypeVO5.setUpdateTimeStamp(LocalDateTime.now());

        productTypeRepo.save(productTypeVO5)

        System.out.println("ProductType ref data loaded")
    }



}
