package com.techvvs.inventory.dao

import com.techvvs.inventory.jparepo.AttributeRepo
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.AttributeVO
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
    AttributeRepo attributeRepo

    @Autowired
    ProductTypeRepo productTypeRepo;

    ProductVO updateProduct(ProductVO productVO, String[] attributeIds) {
        ProductVO existing
        try {
            existing = productRepo.findById(productVO.product_id).orElseThrow {
                new RuntimeException("Product not found with ID: ${productVO.product_id}")
            }

            ProductVO.copyUpdatableFields(productVO, existing)
            existing.setUpdateTimeStamp(LocalDateTime.now())

        } catch (Exception ex) {
            println("Caught Error: ${ex.message}")
            throw ex
        } finally {
            existing = productRepo.save(existing)

            if(attributeIds == null){

                existing.attribute_list.each { it.product_attribute_list.remove(existing) } // remove the product association from each attribute

                existing.attribute_list = new ArrayList<>(1) // clear out the existing attribute list
                existing = productRepo.save(existing)
            } else {
                List<Integer> ids = attributeIds.collect { it as Integer }
                List<AttributeVO> attributes = attributeRepo.findAllById(ids)

                // clear out existing associations for each attribute -> product relationship on this specific product
                existing.attribute_list.each { attribute ->
                    attribute.product_attribute_list.removeIf { it.product_id == existing.product_id }
                }

                // after we clear them out above on the
                existing.attribute_list = null // clear out the existing attribute list
                existing.attribute_list = attributes // assign the attributes from the ui


                // now we add the product onto each attribute that came in from the form
                attributes.each {
                    it.product_attribute_list.add(existing)
                }

                existing = productRepo.save(existing) // final save of existing product
            }


        }

        return existing
    }




}
