package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.model.BatchTypeVO
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.transaction.Transactional
import java.time.LocalDateTime

@Component
class BatchService {

    @Autowired
    BatchRepo batchRepo

    @Autowired
    BatchTypeRepo batchTypeRepo

    @Autowired
    BatchControllerHelper batchControllerHelper

    // todo: do a lookup to make sure batchnumber is unique
    @Transactional
    BatchVO createBatchRecord(String nameOfBatch, Integer batchType){


        // todo: have the user choose or enter a batchtype somehow

        Optional<BatchTypeVO> batchTypeVO = batchTypeRepo.findByBatchTypeId(batchType);

        // set the batch data
        BatchVO batchVO = new BatchVO();
        batchVO.setName(nameOfBatch);
        batchVO.setDescription("default batch description"); // insert date imported into here?
        batchVO.setBatchid(0);
        batchVO.setBatchnumber(Integer.valueOf(batchControllerHelper.generateBatchNumber())); // we are doing a unique check here
        batchVO.setBatch_type_id(batchTypeVO.get()); // assume we always have the ref data loaded

        batchVO.setCreateTimeStamp(LocalDateTime.now());
        batchVO.setUpdateTimeStamp(LocalDateTime.now());

        // first save the batch then we will add products to it and save it again
        BatchVO result = batchRepo.save(batchVO); //

        return result;

    }


    BatchVO addProductToBatch(BatchVO batchVO, ProductVO result){
        batchVO = batchRepo.findByBatchid(batchVO.getBatchid());
        batchVO.setUpdateTimeStamp(LocalDateTime.now());
        // this means a valid batch was found
        batchVO.getProduct_set().add(result); // add the product from database to the product set
        batchVO = batchRepo.save(batchVO);
        return batchVO;


    }





}
