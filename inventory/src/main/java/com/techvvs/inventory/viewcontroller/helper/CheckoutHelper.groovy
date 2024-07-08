package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.Model

import java.time.LocalDateTime

// helper class to make sure actual checkout controller code stays clean n tidy
@Component
class CheckoutHelper {

    @Autowired
    CustomerRepo customerRepo

    @Autowired
    TransactionRepo transactionRepo

    @Autowired
    ProductRepo productRepo


    // method to get all customers from db
    void getAllCustomers(Model model){

        List<CustomerVO> customers = customerRepo.findAll()
        model.addAttribute("customers", customers)
    }

    TransactionVO validateTransactionVO(TransactionVO transactionVO, Model model){
        if(transactionVO?.customervo?.customerid == null){
            model.addAttribute("errorMessage","Please select a customer")
        }
        if(transactionVO?.barcode == null){
            model.addAttribute("errorMessage","Please enter a barcode")
        }
        return transactionVO
    }

    TransactionVO saveTransactionIfNew(TransactionVO transactionVO){

        if(transactionVO.transactionid == null){
            transactionVO.customervo = customerRepo.findById(transactionVO.customervo.customerid).get()
            transactionVO.isprocessed = 0
            transactionVO.createTimeStamp = LocalDateTime.now()
            transactionVO.updateTimeStamp = LocalDateTime.now()
            transactionVO = transactionRepo.save(transactionVO)
        }


        return transactionVO
    }


    TransactionVO searchForProductByBarcode(TransactionVO transactionVO, Model model){


        Optional<ProductVO> productVO = productRepo.findByBarcode(transactionVO.barcode)


        if(!productVO.empty){

            // if it's the first time adding a product we need to create the set to hold them
            if(transactionVO.product_set == null){
                transactionVO.product_set = new HashSet<ProductVO>()
            }

            transactionVO.product_set.add(productVO.get())

            transactionVO.total += Integer.valueOf(productVO.get().price) // add the product price to the total

            transactionVO.updateTimeStamp = LocalDateTime.now()
            transactionVO = transactionRepo.save(transactionVO) // save the transaction with the new product associated
            model.addAttribute("successMessage","Product: "+productVO.get().name + " added successfully")
        } else {
            model.addAttribute("errorMessage","Product not found")
        }



        return transactionVO
    }






}
