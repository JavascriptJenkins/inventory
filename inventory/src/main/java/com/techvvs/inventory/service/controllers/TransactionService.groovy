package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.util.FormattingUtil
import com.techvvs.inventory.util.TechvvsAppUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.LocalDateTime


// abstraction for transactions
@Component
class TransactionService {

    @Autowired
    TransactionRepo transactionRepo

    @Autowired
    CartRepo cartRepo

    @Autowired
    ProductService productService

    @Autowired TechvvsAppUtil techvvsAppUtil

    @Autowired
    FormattingUtil formattingUtil

    @Autowired
    AppConstants appConstants


    //todo: make sure the customer is fully hydrated on the way in here
    TransactionVO processCartGenerateNewTransaction(CartVO cartVO) {

        ArrayList<ProductVO> newlist = cartVO.product_cart_list

        TransactionVO newtransaction = new TransactionVO(

                product_list: newlist,
                cart: cartVO,
                updateTimeStamp: LocalDateTime.now(),
                createTimeStamp: LocalDateTime.now(),
                customervo: cartVO.customer,
                total: cartVO.total,
//                totalwithtax: formattingUtil.calculateTotalWithTax(cartVO.total, appConstants.DEFAULT_TAX_PERCENTAGE),
                totalwithtax: cartVO.total,
                paid: 0.00,
                taxpercentage: techvvsAppUtil.dev1 ? 0 : 0, // we are not going to set a tax percentage here in non dev environments
                isprocessed: 0

        )

        newtransaction = transactionRepo.save(newtransaction)

        // only save the cart after transaction is created


        productService.saveProductAssociations(newtransaction)



        // save the cart with processed=1
        cartVO.isprocessed = 1
        cartVO.updateTimeStamp = LocalDateTime.now()
        cartVO = cartRepo.save(newtransaction.cart)


        // now we have to go update the quantityremaining on the product

        for(ProductVO productVO : newtransaction.product_list){

            ProductVO existingproduct = productService.findProductByID(productVO)

            existingproduct.quantityremaining = productVO.quantityremaining - 1
            existingproduct.updateTimeStamp = LocalDateTime.now()
            productVO = productService.saveProduct(productVO)

        }



            return newtransaction

    }

    TransactionVO getExistingTransaction(Integer transactionid){
        return transactionRepo.findById(transactionid).get()
    }


    List<ProductVO> getAggregatedProductList(TransactionVO transactionVO){
        Set<String> seen = new HashSet<>()
        List<ProductVO> originallist = transactionVO.product_list
        List<ProductVO> newlist = new ArrayList<>()

        for(ProductVO productVO : originallist){

            if(seen.contains(productVO.barcode)){
                continue
            }
            seen.add(productVO.barcode)
            newlist.add(productVO)
        }

        return newlist


    }

    List<ProductVO> getAggregatedCartProductList(CartVO cartVO){
        Set<String> seen = new HashSet<>()
        List<ProductVO> originallist = cartVO.product_cart_list
        List<ProductVO> newlist = new ArrayList<>()

        for(ProductVO productVO : originallist){

            if(seen.contains(productVO.barcode)){
                continue
            }
            seen.add(productVO.barcode)
            newlist.add(productVO)
        }

        return newlist


    }




}
