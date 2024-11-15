package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.DiscountRepo
import com.techvvs.inventory.jparepo.PackageRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.DiscountVO
import com.techvvs.inventory.model.PackageVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.printers.PrinterService
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
    ProductTypeRepo productTypeRepo

    @Autowired
    DiscountRepo discountRepo

    @Autowired
    PrinterService printerService

    @Autowired
    AppConstants appConstants

    @Autowired
    Environment environment

    @Autowired
    PackageRepo packageRepo

    @Autowired
    CartService cartService

    @Autowired
    DiscountService discountService


    @Transactional
    TransactionVO processCartGenerateNewTransaction(CartVO cartVO) {

        Double taxpercentage = environment.getProperty("tax.percentage", Double.class)

        // calculate a percentage discount amount
        double discountPercentage = formattingUtil.calculateTotalDiscountPercentage(cartVO)

        double originalprice = cartService.calculateTotalPriceOfProductList(cartVO.product_cart_list)

        double totalwithtax = 0.00
        if(discountPercentage == 0 && cartVO.discount != null){
            totalwithtax = formattingUtil.calculateTotalWithTaxUsingDiscountAmount(originalprice, taxpercentage, cartVO.discount.discountamount)
        } else if(discountPercentage > 0){
            totalwithtax = formattingUtil.calculateTotalWithTax(originalprice, taxpercentage, discountPercentage)
        } else if(cartVO.discount == null){
            totalwithtax = formattingUtil.calculateTotalWithTax(originalprice, taxpercentage, 0.00)
        }

        ArrayList<ProductVO> newlist = cartVO.product_cart_list

        TransactionVO newtransaction = new TransactionVO(

                product_list: newlist,
                cart: cartVO,
                updateTimeStamp: LocalDateTime.now(),
                createTimeStamp: LocalDateTime.now(),
                customervo: cartVO.customer,
                discount: cartVO.discount,
                total: cartVO.total,
                originalprice: originalprice,
                totalwithtax: totalwithtax,
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
                totalwithtax: formattingUtil.calculateTotalWithTax(packageVO.total, textpercentage, 0.0),
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


    // remove discount and credit back to the original total and totalwithtax to each discount instance
    TransactionVO removeDiscountFromTransactionReCalcTotals(TransactionVO transactionVO, DiscountVO removedDiscount) {

        transactionVO.total = formattingUtil.calculateTotalWithRemovedDiscountAmountPerUnitByProductType(
                transactionVO.total,
                removedDiscount.discountamount,
                removedDiscount.producttype,
                transactionVO.product_list
        )
        transactionVO.total < 0 ? 0 : transactionVO.total // make sure it doesnt go below 0 for some reason...


        // calculate the totalDiscountAmount based on quantityofunitsofsameproducttype * discountamount
        double totalDiscountAmount = calculateTotalDiscountAmount(
                transactionVO.product_list,
                removedDiscount.producttype,
                removedDiscount.discountamount
        )

        // here set the new transaction totalwithtax field based on the new total we just calculated
        transactionVO.totalwithtax = formattingUtil.calculateTotalWithTaxBasedOnTotalDiscountAmountForDiscountRemoval(
                transactionVO.total,
                0.0,
                totalDiscountAmount)

        return transactionVO
    }

    // apply discount to each discount instance
    TransactionVO applyDiscountToTransaction(TransactionVO transactionVO, int index) {

        transactionVO.total = formattingUtil.calculateTotalWithDiscountAmountPerUnitByProductType(
                transactionVO.total,
                transactionVO.discount_list[index].discountamount,
                transactionVO.discount_list[index].producttype,
                transactionVO.product_list
        )
        transactionVO.total < 0 ? 0 : transactionVO.total // make sure it doesnt go below 0 for some reason...

        double originaldiscountedtotal = transactionVO.total
        // calculate the totalDiscountAmount based on quantityofunitsofsameproducttype * discountamount
        double totalDiscountAmount = calculateTotalDiscountAmount(
                transactionVO.product_list,
                transactionVO.discount_list[index].producttype,
                transactionVO.discount_list[index].discountamount
        )

        // here set the new transaction totalwithtax field based on the new total we just calculated
        transactionVO.totalwithtax = formattingUtil.calculateTotalWithTaxBasedOnTotalDiscountAmount(
                originaldiscountedtotal, // need to pass this variable in because the total was modified by the calculateTotalWithDiscountAmountPerUnitByProductType method above first
                0.0,
                totalDiscountAmount)

        return transactionVO
    }

    double calculateTotalDiscountAmount(
            List<ProductVO> product_list,
            ProductTypeVO productTypeVO,
            Double perunitdiscount
    ){
        // calulcate the total discount to apply
        Double totaldiscounttoapply = 0.00
        for(ProductVO productVO : product_list){
            // check every product in the list, if it matches the producttype then increment the discount
            if(productVO.producttypeid.producttypeid == productTypeVO.producttypeid){
                totaldiscounttoapply += perunitdiscount
            }
        }
        return totaldiscounttoapply
    }




    @Transactional
    TransactionVO executeApplyDiscountToTransaction(TransactionVO transactionVO, String transactionid, Optional<String> producttypeid){
        // todo: move this to a service class

        TransactionVO existingTransaction = transactionRepo.findById(Integer.valueOf(transactionid)).get()
        //before we do anything, remove any old discounts that match incoming producttypeid
        existingTransaction = checkForExistingDiscountOfSameProducttypeAndCreditBackToTransactionTotals(
                existingTransaction,
                transactionid,
                Integer.valueOf(producttypeid.get())
        )

        // grab the discount in scope
        DiscountVO newdiscount = new DiscountVO()
        DiscountVO saveddiscount = new DiscountVO()
        if(transactionVO.discount.discountamount > 0 && producttypeid.isPresent()){
            newdiscount = transactionVO.discount
            newdiscount.setProducttype(productTypeRepo.findById(Integer.valueOf(producttypeid.get())).get())
            newdiscount.name = "Transaction Discount"
            newdiscount.description = "discount applied based on producttype"
            newdiscount.isactive = 1 // set incoming new discount to active
            newdiscount.transaction = existingTransaction
            newdiscount.createTimeStamp = LocalDateTime.now()
            newdiscount.updateTimeStamp = LocalDateTime.now()
            saveddiscount = discountRepo.save(newdiscount) // save the new discount tied to transaction


            // todo: run all the logic on the numbers of the discount on the transaction.
            // todo: make a method that cycles through all the discounts in the list of transaction and then applies them.
            saveddiscount.transaction.discount_list.add(saveddiscount) // add the discount to list of discounts on transaction

            // insert method here to apply all discount logic
            // now that new discount has been added to the list, calculate the new total based on all discounts
            for(int i =0; i < saveddiscount.transaction.discount_list.size(); i++){
                saveddiscount.transaction = applyDiscountToTransaction(saveddiscount.transaction, i)
            }

           // saveddiscount = discountRepo.save(newdiscount)
            transactionVO = transactionRepo.save(saveddiscount.transaction) // update the transaction
        }

        return transactionVO

    }

    TransactionVO checkForExistingDiscountOfSameProducttypeAndCreditBackToTransactionTotals(
            TransactionVO transactionVO,
            String transactionid,
            Integer producttypeid
    ){
        // find and remove any existing discounts for the same producttype that is coming in as a new discount
        // Only one "activediscount" discount per producttype is allowed, so we set any existing discounts
        // for the transaction to inactive here before we process the new total.
        // when setting discount to inactive, we also have to add the removed discount back to the total and totalwithtax
        // before further processing
        for(DiscountVO existingolddiscount : transactionVO.discount_list){
            if(existingolddiscount.producttype.producttypeid == producttypeid && existingolddiscount.isactive == 1){
                // this means we found a matching discount of same producttype that already exists
                existingolddiscount.isactive = 0 // set to inactive
                existingolddiscount.updateTimeStamp = LocalDateTime.now()
                discountRepo.save(existingolddiscount) // save the discount as inactive

                // update the transaction tied to the new discount (required to do this only if we found an existing discount above)
                transactionVO = transactionRepo.findById(Integer.valueOf(transactionid)).get()

                //recalculate the total and totalwithtax by adding the credit back from old discount of same producttypeid that is being removed
                transactionVO = removeDiscountFromTransactionReCalcTotals(transactionVO, existingolddiscount)

                return transactionVO
            }
            return transactionVO
        }
        return transactionVO
    }


}
