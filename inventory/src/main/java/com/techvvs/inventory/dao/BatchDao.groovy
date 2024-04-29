package com.techvvs.inventory.dao

import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.LocalDateTime


// orchestration logic for data access layer
@Component
class BatchDao {

    @Autowired
    BatchRepo batchRepo;


    BatchVO updateBatch(BatchVO batchVO){

//        try{
//            BatchVO existing = batchRepo.findAllByBatchnumber(batchVO.batchnumber).get(0)
//
//
//            // update this first to be safe
//            existing.batch_type_id = batchVO.batch_type_id
//            batchVO = batchRepo.save(batchVO) // todo: this is broken probably
//
//
//            // update this second
//            batchVO.product_set = existing.product_set
//            batchVO.setUpdateTimeStamp(LocalDateTime.now());
//
//
//
//
//
//        } catch(Exception ex){
//            System.out.println("Caught Error: "+ex.getCause())
//        } finally {
//            batchVO = batchRepo.save(batchVO)
//        }

        BatchVO existing
        try{
            existing = batchRepo.findById(batchVO.batchid).get()
            batchVO.product_set = existing.product_set // product set will always be the same here
            existing = batchVO
            existing.setUpdateTimeStamp(LocalDateTime.now());
        } catch(Exception ex) {
            System.out.println("Caught Error: " + ex.getCause())
        } finally {
            batchVO = batchRepo.save(existing)
        }





        // first go get the updated list of product objects associated with this batch

        // then assign the product list from database to the instance of BatchVO from the ui

        // update the batch lastupdated field

        // save the batch from ui with existing productlist from database

        // return the result from the database




        return batchVO
    }




}
