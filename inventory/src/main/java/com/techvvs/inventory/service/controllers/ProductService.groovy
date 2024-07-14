package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.LocalDateTime

@Component
class ProductService {

    @Autowired
    ProductRepo productRepo


    void saveProductAssociations(TransactionVO transactionVO) {


        for (ProductVO productVO : transactionVO.product_list) {

            // save the product associations
            //Optional<ProductVO> productVO = productRepo.findByBarcode(cartVO.barcode)


            if (!productVO) {

                // update the product cart list association
                if (productVO.transaction_list == null) {
                    productVO.transaction_list = new ArrayList<>()
                }
                productVO.transaction_list.add(transactionVO)

                productVO.updateTimeStamp = LocalDateTime.now()
                ProductVO savedProduct = productRepo.save(productVO)

            }


        }


    }

    ProductVO saveProduct(ProductVO productVO){
        return productRepo.save(productVO)
    }

    ProductVO findProductByID(ProductVO productVO){
        return productRepo.findById(productVO.product_id).get()
    }

    List<ProductVO> getAllProducts(){
        List<ProductVO> products = productRepo.findAll()
        return products
    }
}