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

import javax.persistence.EntityNotFoundException
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
    @Transactional
    TransactionVO removeDiscountFromTransactionReCalcTotals(TransactionVO transactionVO, DiscountVO removedDiscount) {
        double originaldiscountedtotal = transactionVO.total
        transactionVO.total = Math.max(0, formattingUtil.calculateTotalWithRemovedDiscountAmountPerUnitByProductType(
                transactionVO.total,
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
                originaldiscountedtotal,
                0.0,
                totalDiscountAmount
        ));


        return transactionVO
    }

    // apply discount to each discount instance
    @Transactional
    TransactionVO applyDiscountToTransaction(TransactionVO transactionVO, int index) {

        // Store the original discounted total
        double originalDiscountedTotal = transactionVO.total;

        // Update the transaction total with the applied discount, clamping the value to 0
        transactionVO.total = Math.max(0, formattingUtil.calculateTotalWithDiscountAmountPerUnitByProductType(
                transactionVO.total,
                transactionVO.discount_list[index].discountamount,
                transactionVO.discount_list[index].producttype,
                transactionVO.product_list
        ));

        // Redundant line removed: transactionVO.total < 0 ? 0 : transactionVO.total
        // Math.max already ensures it doesn't go below 0.

        // Calculate the total discount amount based on the product list and discount parameters
        double totalDiscountAmount = Math.max(0, calculateTotalDiscountAmount(
                transactionVO.product_list,
                transactionVO.discount_list[index].producttype,
                transactionVO.discount_list[index].discountamount
        ));

        // Update the totalWithTax field based on the recalculated totals, clamping it to 0
        transactionVO.totalwithtax = Math.max(0, formattingUtil.calculateTotalWithTaxBasedOnTotalDiscountAmount(
                originalDiscountedTotal,
                0.0,
                totalDiscountAmount
        ));

        return transactionVO;
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
    public TransactionVO executeApplyDiscountToTransaction(TransactionVO transactionVO, String transactionid, ProductTypeVO producttypevo) {
        // Move business logic to service layer
        TransactionVO existingTransaction = transactionRepo.findById(Integer.valueOf(transactionid)).orElseThrow({ new EntityNotFoundException("Transaction not found: " + transactionid) });

        // Remove existing discounts of the same product type
        existingTransaction = checkForExistingDiscountOfSameProducttypeAndCreditBackToTransactionTotals(
                existingTransaction,
                transactionid,
                Integer.valueOf(producttypevo.producttypeid)
        );

        if (transactionVO.getDiscount().getDiscountamount() > 0) {

            // Prepare new discount
            DiscountVO newDiscount = createDiscount(transactionVO.getDiscount(), existingTransaction, producttypevo);

            // Save the new discount
            DiscountVO savedDiscount = discountRepo.save(newDiscount);

            // Add the new discount to the transaction's discount list
            savedDiscount.getTransaction().getDiscount_list().add(savedDiscount);

            // Apply discounts to the transaction
            savedDiscount.setTransaction(applyAllDiscountsToTransaction(savedDiscount.getTransaction()));

            // Save the updated transaction
            transactionVO = transactionRepo.save(savedDiscount.getTransaction());
        }

        return transactionVO;
    }

    private DiscountVO createDiscount(
            DiscountVO incomingDiscount,
            TransactionVO transaction,
            ProductTypeVO producttypevo
    ) {


        DiscountVO newDiscount = new DiscountVO();
        newDiscount.setDiscountamount(incomingDiscount.getDiscountamount());
        newDiscount.setProducttype(producttypevo);
        newDiscount.setName("Transaction Discount");
        newDiscount.setDescription("Discount applied based on product type");
        newDiscount.setIsactive(1);
        newDiscount.setTransaction(transaction);
        newDiscount.setCreateTimeStamp(LocalDateTime.now());
        newDiscount.setUpdateTimeStamp(LocalDateTime.now());

        return newDiscount;
    }

    private TransactionVO applyAllDiscountsToTransaction(TransactionVO transaction) {
        for (int i = 0; i < transaction.getDiscount_list().size(); i++) {
            if (transaction.getDiscount_list().get(i).getIsactive() == 1) {
                transaction = applyDiscountToTransaction(transaction, i);
            }
        }
        return transaction;
    }


    @Transactional
    TransactionVO checkForExistingDiscountOfSameProducttypeAndCreditBackToTransactionTotals(
            TransactionVO transactionVO,
            String transactionid,
            Integer producttypeid
    ) {
        List<DiscountVO> discountsCopy = new ArrayList<>(transactionVO.getDiscount_list());

        for (DiscountVO existingOldDiscount : discountsCopy) {
            if (existingOldDiscount.getProducttype().getProducttypeid().equals(producttypeid)
                    && existingOldDiscount.getIsactive() == 1) {

                // Deactivate the matching discount
                existingOldDiscount.setIsactive(0);
                existingOldDiscount.setUpdateTimeStamp(LocalDateTime.now());
                discountRepo.save(existingOldDiscount);

                // Re-fetch the transaction to ensure the latest state is loaded
                transactionVO = transactionRepo.findById(Integer.valueOf(transactionid)).orElseThrow({ new EntityNotFoundException("Transaction not found: " + transactionid) });

                // Recalculate totals by crediting back the removed discount
                transactionVO = removeDiscountFromTransactionReCalcTotals(transactionVO, existingOldDiscount);
            }
        }

        return transactionVO;
    }


}
