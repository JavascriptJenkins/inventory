package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
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

    @Autowired
    ProductRepo productRepo

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


    BatchVO moveProductToNewBatch(Integer originalBatchId, ProductVO productVO, List products, int targetBatchId) {

        // Fetch the original and target batches
        BatchVO originalBatch = batchRepo.findByBatchid(originalBatchId);


        if (originalBatch == null) {
            throw new IllegalArgumentException("original batch not found.");
        }

        // Using Iterator to safely remove the product while iterating
        Iterator<ProductVO> iterator = originalBatch.getProduct_set().iterator();
        boolean productRemoved = false;

        int targetbatchid = 0
        ProductVO productToRemove = null
        while (iterator.hasNext()) {
            ProductVO product = iterator.next();
            if (product.getProduct_id().equals(productVO.getProduct_id())) {
                iterator.remove();  // Remove the product safely
                productToRemove = product
                targetbatchid = targetBatchId // set it to value that came in from the ui
                productToRemove.batch.batchid = originalBatchId /// need to reset this from the ui
                productRemoved = true;
                break;
            }
        }

        if (!productRemoved) {
            throw new IllegalArgumentException("Product not found in the original batch.");
        }

        productVO = productRepo.save(productToRemove)

        // Save the updated original batch after removal
        originalBatch = batchRepo.save(originalBatch);

        BatchVO targetBatch = batchRepo.findByBatchid(targetbatchid);

        // Update the product reference to the managed batch entity
        productVO.setBatch(targetBatch);
        productRepo.save(productVO); // Save updated product with new batch reference

        // Add the product to the target batch and save
        targetBatch.getProduct_set().add(productVO);
        targetBatch = batchRepo.save(targetBatch);

        products.add(productVO) // add it to a list for displaying after products have been moved on confirmation screen

        return originalBatch
    }



    boolean validateDeleteBatch(BatchVO batchVO){

        // check to make sure that any products in the batch are not in a cart or transaction - and therefore the batch can be deleted

        for(ProductVO productVO : batchVO.product_set){
            if(
                    productVO.cart_list.size() > 0 || productVO.transaction_list.size() > 0
            ){
                return false // if any products are in carts or in a transaction, do not allow the batch to be deleted
            }
        }


        return true
    }


    @Transactional
    boolean deleteBatch(Integer batchid){

        BatchVO batchVO = batchRepo.findById(batchid).get()
        if(batchVO?.batchid == 0 || batchVO?.batchid == null){
            return false
        } else {

            boolean canDelete = validateDeleteBatch(batchVO)

            if(!canDelete){
                return false // return early if the batch cannot be deleted
            }

            try {


                BatchVO existingBatch = batchRepo.findById(batchid).get()

                for(ProductVO existingProduct : existingBatch.product_set){
                    Optional<ProductVO> productVO = productRepo.findById(existingProduct.product_id)

                    if(productVO.present){
                        // delete the product associated with the batch
                        productRepo.delete(productVO.get())
                    }
                }

                // now we have to remove the association to the batchType before deleting the batch
                existingBatch.batch_type_id = null



            } catch (Exception e){
                System.out.println("Caught Exception While Deleting Batch: " + e)
                return false
            } finally{

                batchRepo.deleteById(batchid) // only delete the batch if all products have been deleted and everything else above processed smoothly

            }



            return true
        }
    }




}
