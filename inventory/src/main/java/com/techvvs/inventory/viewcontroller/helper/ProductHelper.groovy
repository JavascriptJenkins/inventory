package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.ProductVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.Model

@Component
class ProductHelper {

    @Autowired
    ProductRepo productRepo




    ProductVO loadProduct(int productid){
        Optional<ProductVO> productVO = productRepo.findById(productid)
        return productVO.get()
    }





}
