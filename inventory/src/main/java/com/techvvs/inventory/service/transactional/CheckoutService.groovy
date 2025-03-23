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
            int index,
            Totals totals

    ) {

        double total = 0.00

        // todo: i think we need to do null checking here to make sure we only run this if producttype is present
        // Update the transaction total with the applied discount for producttype discounts
        if(transactionVO.discount_list[index].producttype != null){
            total = Math.max(0, formattingUtil.calculateTotalWithDiscountAmountPerUnitByProductType(
                    transactionVO.discount_list[index].discountamount,
                    transactionVO.discount_list[index].producttype,
                    transactionVO.product_list
            ));
            totals.listOfDiscountsToApplyToTotal.add(total) // add the total calculated from above to the running tally here
        }



        // Update the transaction total with the applied discount for product discounts
        if(transactionVO.discount_list[index].product != null){
            total = Math.max(0, formattingUtil.calculateTotalWithDiscountAmountPerUnitByProduct(
                    transactionVO.discount_list[index].discountamount,
                    transactionVO.discount_list[index].product,
                    transactionVO.discount_list[index].quantity,
                    transactionVO.product_list
            ));
            // add the discount for this producttype for subtraction from the originaltotal after this loop processes
            totals.listOfDiscountsToApplyToTotal.add(total)
        }


        double totalDiscountAmount = 0.00

        // Calculate the total discount amount based on the product list and discount parameters
        if(transactionVO.discount_list[index].producttype != null) {
            totalDiscountAmount = Math.max(0, calculateTotalDiscountAmount(
                    transactionVO.product_list,
                    transactionVO.discount_list[index].producttype,
                    transactionVO.discount_list[index].discountamount
            ));
        }

        if(transactionVO.discount_list[index].product != null) {
            totalDiscountAmount = Math.max(0, calculateTotalDiscountAmountByProduct(
                    transactionVO.product_list,
                    transactionVO.discount_list[index].product,
                    transactionVO.discount_list[index].discountamount,
                    transactionVO.discount_list[index].quantity
            ));
        }

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
            // handle producttype discounts
            if(discountVO.producttype != null &&
                    discountVO.producttype.producttypeid == productToRemove.producttypeid.producttypeid && discountVO.isactive == 1) {
                // if we find a match while removing the product from the transaction, apply the per unit discount back to the transaction total and totalwithtax fields
                 return Math.max(0,discountVO.discountamount)

            }
        }
        return 0.00
    }

    @Transactional
    double calculateTotalsForRemovingExistingProductFromTransactionForProductDiscount(
            TransactionVO transactionVO,
            ProductVO productToRemove
    ) {
        // first check to see if any matches in the active discount list that match to the producttype being removed
        for(DiscountVO discountVO : transactionVO.discount_list) {
            // handle producttype discounts
            if(discountVO.product != null &&
                    discountVO.product.product_id == productToRemove.product_id && discountVO.isactive == 1) {
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


    @Transactional
    TransactionVO calculateTotalsForRemovingExistingDiscountByProduct(
            TransactionVO transactionVO,
            double currenttransactionamount,
            DiscountVO removedDiscount
    ) {

        transactionVO.total = Math.max(0, formattingUtil.calculateTotalWithRemovedDiscountAmountPerUnitByProduct(
                currenttransactionamount,
                removedDiscount.discountamount,
                removedDiscount.product,
                transactionVO.product_list,
                removedDiscount.quantity // passing this in to make sure we remove the exact amount of that product
        ));
        transactionVO.total < 0 ? 0 : transactionVO.total // make sure it doesnt go below 0 for some reason...


        // calculate the totalDiscountAmount based on quantityofunitsofsameproducttype * discountamount
        double totalDiscountAmount = Math.max(0, calculateTotalDiscountAmountByProduct(
                transactionVO.product_list,
                removedDiscount.product,
                removedDiscount.discountamount,
                removedDiscount.quantity
        ));

        // here set the new transaction totalwithtax field based on the new total we just calculated
        // NOTE: this is the same method being called for Product and ProductType discounts
        transactionVO.totalwithtax = Math.max(0, formattingUtil.calculateTotalWithTaxBasedOnTotalDiscountAmountForDiscountRemoval(
                currenttransactionamount,
                0.0,
                totalDiscountAmount
        ));

        return transactionVO

    }


    @Transactional
    double calculateTotalDiscountAmountByProduct(
            List<ProductVO> product_list,
            ProductVO productInScope,
            Double perunitdiscount,
            int quantity
    ){
        // calulcate the total discount to apply
        Double totaldiscounttoapply = 0.00
        int counter1 = 0
        for(ProductVO productVO : product_list){
            // check every product in the list, if it matches the producttype then increment the discount
            if(productVO.product_id == productInScope.product_id && counter1 < quantity){
                Math.max(0,totaldiscounttoapply += perunitdiscount)
                counter1 ++
            }
        }
        return totaldiscounttoapply
    }



}
