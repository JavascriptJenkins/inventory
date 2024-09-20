package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.PackageRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.PackageVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.util.FormattingUtil
import com.techvvs.inventory.util.TechvvsAppUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

import javax.transaction.Transactional
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

    @Autowired
    Environment environment

    @Autowired
    PackageRepo packageRepo


    @Transactional
    TransactionVO processCartGenerateNewTransaction(CartVO cartVO) {

        Double taxpercentage = environment.getProperty("tax.percentage", Double.class)

        ArrayList<ProductVO> newlist = cartVO.product_cart_list

        TransactionVO newtransaction = new TransactionVO(

                product_list: newlist,
                cart: cartVO,
                updateTimeStamp: LocalDateTime.now(),
                createTimeStamp: LocalDateTime.now(),
                customervo: cartVO.customer,
                total: cartVO.total,
                totalwithtax: formattingUtil.calculateTotalWithTax(cartVO.total, taxpercentage),
//                totalwithtax: cartVO.total,
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


        // quantityremaining is updated when the cart is saved... this method is useless for now but will
        // be useful if we need to do anything to the product after the transaction is saved

        for(ProductVO productVO : newtransaction.product_list){

            ProductVO existingproduct = productService.findProductByID(productVO)

           // existingproduct.quantityremaining = productVO.quantityremaining - 1
            existingproduct.updateTimeStamp = LocalDateTime.now()
            productVO = productService.saveProduct(productVO)

        }



            return newtransaction

    }


    // todo: i don't think i need this, keep it just in case we need a method that adds a list of packages to a transaction....
    @Transactional
    TransactionVO processPackageGenerateNewTransaction(PackageVO packageVO) {

        int textpercentage = environment.getProperty("tax.percentage", Integer.class, 0)

        ArrayList<Package> newlist = packageVO.product_package_list

        TransactionVO newtransaction = new TransactionVO(

                product_list: newlist,
                package: packageVO,
                updateTimeStamp: LocalDateTime.now(),
                createTimeStamp: LocalDateTime.now(),
                customervo: packageVO.customer,
                total: packageVO.total,
                totalwithtax: formattingUtil.calculateTotalWithTax(packageVO.total, textpercentage),
//                totalwithtax: packageVO.total,
                paid: 0.00,
                taxpercentage: techvvsAppUtil.dev1 ? 0 : 0, // we are not going to set a tax percentage here in non dev environments
                isprocessed: 0

        )

        newtransaction = transactionRepo.save(newtransaction)

        // only save the package after transaction is created


        productService.saveProductAssociations(newtransaction)



        // save the package with processed=1
        packageVO.isprocessed = 1
        packageVO.updateTimeStamp = LocalDateTime.now()
        packageVO = packageRepo.save(newtransaction.package)


        // quantityremaining is updated when the cart is saved... this method is useless for now but will
        // be useful if we need to do anything to the product after the transaction is saved

        for(ProductVO productVO : newtransaction.product_list){

            ProductVO existingproduct = productService.findProductByID(productVO)

            // existingproduct.quantityremaining = productVO.quantityremaining - 1
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

    List<ProductVO> getAggregatedPackageProductList(PackageVO packageVO){
        Set<String> seen = new HashSet<>()
        List<ProductVO> originallist = packageVO.product_package_list
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
