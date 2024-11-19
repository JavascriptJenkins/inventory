package com.techvvs.inventory.service.transactional

import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.DiscountVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.model.nonpersist.Totals
import com.techvvs.inventory.util.FormattingUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.transaction.Transactional

@Service
class CheckoutService {

    @Autowired
    FormattingUtil formattingUtil

    @Autowired
    TransactionRepo transactionRepo


    @Transactional
    TransactionVO calculateTotalsForAddingNewDiscount(
            TransactionVO transactionVO,
            double originaltransactiontotal,
            int index,
            Totals totals

    ) {

        // todo: this needs to keep a running tally of all the discounts that are applied as they go thru the loop
        // Update the transaction total with the applied discount, clamping the value to 0
        //transactionVO.total = Math.max(0, formattingUtil.calculateTotalWithDiscountAmountPerUnitByProductType(
        double total = Math.max(0, formattingUtil.calculateTotalWithDiscountAmountPerUnitByProductType(
                originaltransactiontotal, // not using this delete it
                transactionVO.discount_list[index].discountamount,
                transactionVO.discount_list[index].producttype,
                transactionVO.product_list
        ));

        // add the discount for this producttype for subtraction from the originaltotal after this loop processes
        totals.listOfDiscountsToApplyToTotal.add(total)

        // Redundant line removed: transactionVO.total < 0 ? 0 : transactionVO.total
        // Math.max already ensures it doesn't go below 0.

        // Calculate the total discount amount based on the product list and discount parameters
        double totalDiscountAmount = Math.max(0, calculateTotalDiscountAmount(
                transactionVO.product_list,
                transactionVO.discount_list[index].producttype,
                transactionVO.discount_list[index].discountamount
        ));

        // Update the totalWithTax field based on the recalculated totals, clamping it to 0
        double totalWithTax = Math.max(0, formattingUtil.calculateTotalWithTaxBasedOnTotalDiscountAmount(
                0.0,
                totalDiscountAmount
        ));

        // add the discount for this producttype for subtraction from the originaltotal after this loop processes
        totals.listOfDiscountsToApplyToTotalWithTax.add(totalWithTax)


        return transactionVO


    }


    @Transactional
    double calculateTotalsForRemovingExistingProductFromTransaction(
            TransactionVO transactionVO,
            ProductVO productToRemove
    ) {
        // first check to see if any matches in the active discount list that match to the producttype being removed
        for(DiscountVO discountVO : transactionVO.discount_list) {
            if(discountVO.producttype.producttypeid == productToRemove.producttypeid.producttypeid && discountVO.isactive == 1) {
                // if we find a match while removing the product from the transaction, apply the per unit discount back to the transaction total and totalwithtax fields
                 return Math.max(0,discountVO.discountamount)

            }
        }
        return 0.00
    }

    @Transactional
    TransactionVO calculateTotalsForRemovingExistingDiscount(
            TransactionVO transactionVO,
            double currenttransactionamount,
            DiscountVO removedDiscount
    ) {
        transactionVO.total = Math.max(0, formattingUtil.calculateTotalWithRemovedDiscountAmountPerUnitByProductType(
                currenttransactionamount,
                removedDiscount.discountamount,
                removedDiscount.producttype,
                transactionVO.product_list
        ));
        transactionVO.total < 0 ? 0 : transactionVO.total // make sure it doesnt go below 0 for some reason...


        // calculate the totalDiscountAmount based on quantityofunitsofsameproducttype * discountamount
        double totalDiscountAmount = Math.max(0, calculateTotalDiscountAmount(
                transactionVO.product_list,
                removedDiscount.producttype,
                removedDiscount.discountamount
        ));

        // here set the new transaction totalwithtax field based on the new total we just calculated
        transactionVO.totalwithtax = Math.max(0, formattingUtil.calculateTotalWithTaxBasedOnTotalDiscountAmountForDiscountRemoval(
                currenttransactionamount,
                0.0,
                totalDiscountAmount
        ));

        return transactionVO

    }


    @Transactional
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
                Math.max(0,totaldiscounttoapply += perunitdiscount)
            }
        }
        return totaldiscounttoapply
    }




}
