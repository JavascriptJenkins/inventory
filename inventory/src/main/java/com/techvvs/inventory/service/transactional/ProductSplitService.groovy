package com.techvvs.inventory.service.transactional

import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.service.controllers.ProductService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.transaction.Transactional
import java.time.LocalDateTime


@Component
class ProductSplitService {


    @Autowired
    ProductService productService

    @Autowired
    BarcodeHelper barcodeHelper



    // todo: make sure this cannot be called if the product calling it has a quantity of 0
    /* accepts a single product, outputs a list of products that was split from the original */
    @Transactional
    ProductVO splitProduct(int productid, Double newprice, int split) {

        // subtract 1 from existing product quantity
        ProductVO existingproduct = productService.findProductByID(new ProductVO(product_id: productid))
        existingproduct.quantity = Math.max(0, existingproduct.quantity - 1)
        existingproduct = productService.saveProduct(existingproduct) // save the updated product quantity


        // create a new product with 8TH, QUAD, OUNCE, QP, HALF, etc - use existing product name with this appended to end of name
        String splitlabel = productService.matchSplitAmountWithLabel(split) // get string value for amount we are splitting by
        int splitmultiplier = productService.calculateSplitMultiplier(split)

        ProductVO productVO = new ProductVO()
        productVO.name = existingproduct.name + " " + splitlabel
        productVO.vendor = existingproduct.vendor
        productVO.price = newprice
        productVO.quantity = splitmultiplier
        productVO.vendorquantity = existingproduct.vendorquantity
        productVO.quantityremaining = splitmultiplier
        productVO.bagcolor = "normal"
        productVO.barcode = productService.generateBarcodeForSplitProduct(existingproduct.batch.batchnumber)
        productVO.cost = Math.round(Math.max(0, existingproduct.price / splitmultiplier) * 100.0) / 100.0; // this rounds to nearest 2 decimals
        productVO.crate = 0
        productVO.crateposition = "00000"
        productVO.description = "Split from "+existingproduct.name+" with split of "+splitlabel
        productVO.productnumber = 2 // todo: see if this actually has to be unique.... should remove this whole column from product table honestly

        // metadata
        productVO.createTimeStamp = LocalDateTime.now()
        productVO.updateTimeStamp = LocalDateTime.now()

        // assign product type and batch
        productVO.producttypeid = productService.assignProductType(existingproduct.producttypeid, splitlabel)
        productVO.batch = existingproduct.batch

        productVO = productService.saveProduct(productVO) // save the new product!

        // todo: copy all the photos and other files from the original product ???


    }




}
