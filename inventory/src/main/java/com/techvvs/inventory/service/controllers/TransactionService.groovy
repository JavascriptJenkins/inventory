package com.techvvs.inventory.service.controllers

import com.fasterxml.jackson.annotation.JsonProperty
import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.DeliveryRepo
import com.techvvs.inventory.jparepo.DiscountRepo
import com.techvvs.inventory.jparepo.LocationRepo
import com.techvvs.inventory.jparepo.LocationTypeRepo
import com.techvvs.inventory.jparepo.PackageRepo
import com.techvvs.inventory.jparepo.PackageTypeRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.DeliveryVO
import com.techvvs.inventory.model.DiscountVO
import com.techvvs.inventory.model.LocationTypeVO
import com.techvvs.inventory.model.LocationVO
import com.techvvs.inventory.model.PackageTypeVO
import com.techvvs.inventory.model.PackageVO
import com.techvvs.inventory.model.PaymentVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.ReturnVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.model.nonpersist.Totals
import com.techvvs.inventory.printers.PrinterService
import com.techvvs.inventory.qrcode.impl.QrCodeGenerator
import com.techvvs.inventory.service.transactional.CheckoutService
import com.techvvs.inventory.util.FormattingUtil
import com.techvvs.inventory.util.TechvvsAppUtil
import com.techvvs.inventory.util.TwilioTextUtil
import com.techvvs.inventory.viewcontroller.helper.TransactionHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.security.core.parameters.P
import org.springframework.stereotype.Component

import javax.persistence.EntityNotFoundException
import javax.transaction.Transactional
import java.security.SecureRandom
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
    CheckoutService checkoutService

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

    @Autowired
    QrCodeGenerator qrCodeGenerator

    @Autowired
    DeliveryRepo deliveryRepo

    @Autowired
    LocationRepo locationRepo

    @Autowired
    LocationTypeRepo locationTypeRepo

    @Autowired
    PackageTypeRepo packageTypeRepo

    @Autowired
    CustomerRepo customerRepo

    @Autowired
    TwilioTextUtil twilioTextUtil

    @Autowired
    TransactionHelper transactionHelper

    @Transactional
    TransactionVO processCartGenerateNewTransaction(CartVO cartVO) {

        Double taxpercentage = environment.getProperty("tax.percentage", Double.class)

        double originalprice = cartService.calculateTotalPriceOfProductList(cartVO.product_cart_list)

        double totalwithtax = 0.00

        totalwithtax = formattingUtil.calculateTotalWithTax(originalprice, taxpercentage, 0.00)

        ArrayList<ProductVO> newlist = cartVO.product_cart_list

        TransactionVO newtransaction = new TransactionVO(

                product_list: newlist,
                cart: cartVO,
                updateTimeStamp: LocalDateTime.now(),
                createTimeStamp: LocalDateTime.now(),
                customervo: cartVO.customer,
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


    // todo: holy crap this method is ridiculous.  god willing inshallah it shall be refactored
    @Transactional
    TransactionVO processCartGenerateNewTransactionForDelivery(CartVO cartVO, int locationid, String deliverynotes, LocationVO locationVO, type) {

        Double taxpercentage = environment.getProperty("tax.percentage", Double.class)
        System.out.println("BUGFIX DEBUG: 5")
        double originalprice = cartService.calculateTotalPriceOfProductList(cartVO.product_cart_list)

        double totalwithtax = 0.00

        totalwithtax = formattingUtil.calculateTotalWithTax(originalprice, taxpercentage, 0.00)
        System.out.println("BUGFIX DEBUG: 6")
        ArrayList<ProductVO> newlist = cartVO.product_cart_list

        TransactionVO newtransaction = new TransactionVO(

                product_list: newlist,
                cart: cartVO,
                updateTimeStamp: LocalDateTime.now(),
                createTimeStamp: LocalDateTime.now(),
                customervo: cartVO.customer,
                total: cartVO.total,
                originalprice: originalprice,
                totalwithtax: totalwithtax,
//                totalwithtax: cartVO.total,
                paid: 0.00,
                taxpercentage: techvvsAppUtil.dev1 ? 0 : 0, // we are not going to set a tax percentage here in non dev environments
                isprocessed: 0

        )

        newtransaction = transactionRepo.save(newtransaction)
        System.out.println("BUGFIX DEBUG: 7")
        // only save the cart after transaction is created
        productService.saveProductAssociations(newtransaction)

        // save the cart with processed=1
        cartVO.isprocessed = 1
        cartVO.updateTimeStamp = LocalDateTime.now()
        cartVO = cartRepo.save(newtransaction.cart)

        System.out.println("BUGFIX DEBUG: 8")
        // create a delivery instance for the transaction so it will show up in the delivery que
        DeliveryVO deliveryVO = new DeliveryVO(
                transaction: newtransaction,
                name: cartVO.customer.name+" | Order " + " | " +newtransaction.transactionid,
                description: "typical order description",
                deliverybarcode: productService.generateBarcodeForSplitProduct(generateEightDigitNumber()),
                deliveryqrlink: "", // this contains the deliveryid, has to be set after the delivery is created
                location: setLocation(deliverynotes, locationid, locationVO, type, cartVO.customer),
                notes: deliverynotes,
                iscanceled: 0,
                isprocessed: 0,
                ispickup: "delivery".equals(type) ? 0 : 1,
                status: appConstants.DELIVERY_STATUS_CREATED,
                total: newtransaction.total,
                updateTimeStamp: LocalDateTime.now(),
                createTimeStamp: LocalDateTime.now()

        )
        deliveryVO = deliveryRepo.save(deliveryVO)
        System.out.println("BUGFIX DEBUG: 9")

        // now set the delivery qr link on the delivery object
        deliveryVO.deliveryqrlink = qrCodeGenerator.buildQrLinkForDeliveryItem(String.valueOf(deliveryVO.deliveryid))
        deliveryVO.package_list = createNewPackage(newtransaction, deliveryVO)
        System.out.println("BUGFIX DEBUG: 10")
        deliveryVO = deliveryRepo.save(deliveryVO)

        // todo: should we text the user the delivery qr link upon creation?   will just display in work que ui for now

        newtransaction.delivery = deliveryVO // binding this here just so the upstream methods will have it and we don't have to do another fetch to the database

        return newtransaction

    }

    LocationVO setLocation(String deliverynotes, int locationid, LocationVO locationVO, String type, CustomerVO customerVO) {
        if(locationid > 0) {
            // this means user chose a location on the dropdown and we grab that from the database
            return locationRepo.findById(locationid).get()
        } else {
            CustomerVO detachedVO = customerVO
            LocationTypeVO locationTypeVO = null
            if("delivery".equals(type)) {
                locationTypeVO = locationTypeRepo.findByName(appConstants.ADHOC_CUSTOMER_DELIVERY).get()

            } else if("pickup".equals(type)) {
                locationTypeVO = locationTypeRepo.findByName(appConstants.ADHOC_CUSTOMER_PICKUP).get()
            }

            LocationVO newlocation = locationRepo.save(createLocationBasedOnOrderType(type, locationTypeVO, locationVO, deliverynotes))

            // save the locationid relationship to the customer
            detachedVO.locationlist.add(newlocation)

            customerRepo.save(detachedVO)
            return newlocation
        }
    }

    LocationVO createLocationBasedOnOrderType(String type, LocationTypeVO locationTypeVO, LocationVO locationVO, String deliverynotes){
        switch (type) {
            case "delivery":
                return new LocationVO(
                        name: deliverynotes ? deliverynotes.substring(0, Math.min(25, deliverynotes.length())) : "", // we are setting the location name to first 25 characters of the delivery notes
                        description: deliverynotes,
                        locationtype: locationTypeVO,
                        notes: deliverynotes,
                        address1: locationVO.address1,
                        address2: locationVO.address2 != null ? locationVO.address2 : "",
                        city: locationVO.city,
                        state: locationVO.state,
                        zipcode: locationVO.zipcode,
                        updateTimeStamp: LocalDateTime.now(),
                        createTimeStamp: LocalDateTime.now()
                )
            case "pickup":
                return new LocationVO(
                        name: "Default Pickup Location", // we are setting the location name to first 25 characters of the delivery notes
                        description: "Default Notes For Pickup Location",
                        locationtype: locationTypeVO,
                        notes: deliverynotes,
                        address1: "default address 1",
                        address2: "default address 2",
                        city: "default city",
                        state: "default state",
                        zipcode: "default zip",
                        updateTimeStamp: LocalDateTime.now(),
                        createTimeStamp: LocalDateTime.now()
                )
            default:
                return new LocationVO()
        }

    }

    List<PackageVO>  createNewPackage(TransactionVO transactionVO, DeliveryVO deliveryVO){
        PackageTypeVO packageTypeVO = packageTypeRepo.findByName(appConstants.SMALL_BOX).get()
        List<ProductVO> detachedList = new ArrayList<>(transactionVO.product_list);

        PackageVO packageVO = packageRepo.save(new PackageVO(
                name: transactionVO.customervo.name + " | Package | " + transactionVO.transactionid,
                description: "this package is going out for delivery or pickup in a package locker. ",
                packagebarcode: productService.generateBarcodeForSplitProduct(generateEightDigitNumber()), // we are not going to use this in this customer fulfillment delivery flow.  no reason to track multiple barcodes rn. in the future we might use this for tracking code
                packagetype: packageTypeVO,
                customer: transactionVO.customervo,
                transaction: transactionVO,
                weight: 0.00,
                bagcolor: "normal",
                delivery: deliveryVO,
                product_package_list: detachedList,
                total: transactionVO.total,
                isprocessed: 0,
                updateTimeStamp: LocalDateTime.now(),
                createTimeStamp: LocalDateTime.now(),
        ))

        List<PackageVO> newlist = new ArrayList<PackageVO>()
        newlist.add(packageVO)

        return newlist
    }

//    int generateSixDigitNumber() {
//        // Initialize SecureRandom
//        SecureRandom secureRandom = new SecureRandom()
//        // Generate a random number between 100000 and 999999 (inclusive)
//        return Integer.valueOf(100000 + secureRandom.nextInt(900000))
//    }

    int generateEightDigitNumber() {
        // Initialize SecureRandom
        SecureRandom secureRandom = new SecureRandom();

        // Generate a random number between 10000000 and 99999999 (inclusive)
        return 10000000 + secureRandom.nextInt(90000000);
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
    TransactionVO removeDiscountFromTransactionReCalcTotals(
            TransactionVO transactionVO,
            DiscountVO removedDiscount,
            double originaltransactionamount,
            double currenttransactionamount

    ) {
        transactionVO = checkoutService.calculateTotalsForRemovingExistingDiscount(transactionVO, currenttransactionamount, removedDiscount)

        return transactionVO
    }

    // apply discount to each discount instance
    @Transactional
    TransactionVO applyDiscountToTransaction(
            TransactionVO transactionVO,
            int index,
            Totals totals
    ) {

        transactionVO = checkoutService.calculateTotalsForAddingNewDiscount(transactionVO,  index, totals)

        return transactionVO;
    }




    @Transactional
    public TransactionVO executeApplyDiscountToTransaction(
            TransactionVO transactionVO,
            String transactionid,
            ProductTypeVO producttypevo
    ) {

        TransactionVO existingTransaction = transactionRepo.findById(Integer.valueOf(transactionid)).orElseThrow({ new EntityNotFoundException("Transaction not found: " + transactionid) });

        // Remove existing discounts of the same product type and credit back the discount to original total
        // before continueing with applying the new discount
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

            return transactionVO;
        } else {
            return existingTransaction;
        }


    }

    private DiscountVO createDiscount(
            DiscountVO incomingDiscount,
            TransactionVO transaction,
            ProductTypeVO producttypevo
    ) {

        // count the quantity of the product we are applying discount for
        int quantity = 0
        ProductVO productVO1 = new ProductVO(product_id:0)
        for(ProductVO productVO : transaction.product_list){
            if(productVO.producttypeid.producttypeid == producttypevo.producttypeid){
                quantity++
                productVO1 = productVO
            }
        }

        DiscountVO newDiscount = new DiscountVO();
        newDiscount.setDiscountamount(incomingDiscount.getDiscountamount());
        newDiscount.setProducttype(producttypevo);
        newDiscount.setName("ProductType: " + producttypevo.name)
        newDiscount.setDescription("Discount applied based on product type");
        newDiscount.setIsactive(1);
        newDiscount.setQuantity(quantity)
        newDiscount.setProduct(productVO1)
        newDiscount.setTransaction(transaction);
        newDiscount.setCreateTimeStamp(LocalDateTime.now());
        newDiscount.setUpdateTimeStamp(LocalDateTime.now());

        return newDiscount;
    }

    // NOTE: this is where we actually apply the product and producttype discounts so they can co exist
    private TransactionVO applyAllDiscountsToTransaction(
             TransactionVO transaction
    ) {


        Totals totals = new Totals()
        for (int i = 0; i < transaction.getDiscount_list().size(); i++) {
            if (transaction.getDiscount_list().get(i).getIsactive() == 1) {
                transaction = applyDiscountToTransaction(transaction, i, totals);
            }
        }

        double totaldiscounttosubstractfromtotal = 0.00
        double totaldiscounttosubstractfromtotalwithtax = 0.00
        double totalreturnedproductvalue = 0.00

        totals = applyReturnProductValuesToTotals(transaction, totals) // account for the returned products
        totals = applyExistingPaymentsToTotals(transaction, totals) // account for the existing payments


        // we should be updating the total and total withtax here, after whole loop runs
        for(double discountamount: totals.listOfDiscountsToApplyToTotal){
            totaldiscounttosubstractfromtotal += discountamount
        }

        for(double discountamount: totals.listOfDiscountsToApplyToTotalWithTax){
            totaldiscounttosubstractfromtotalwithtax += discountamount
        }

        for(double returnedproductvalue: totals.listOfReturnProductValuesToApply){
            totalreturnedproductvalue += returnedproductvalue
        }


        transaction.total = transaction.originalprice - (totaldiscounttosubstractfromtotal + totalreturnedproductvalue)
        transaction.totalwithtax = transaction.originalprice - (totaldiscounttosubstractfromtotal + totalreturnedproductvalue)

        return transaction;
    }

    Totals applyReturnProductValuesToTotals(TransactionVO transaction, Totals totals) {
        // now, we need to update these 2 totals again if we have any discounts to apply
        // check if there are any objects in the return table and subtract from original price
        // if we don't do this, discounts will be applied incorrectly on transactions with returns
        if(transaction.return_list.size() > 0){
            for(ReturnVO returnVO : transaction.return_list){
                totals.listOfReturnProductValuesToApply.add(returnVO?.product?.price)
            }
        }

        return totals
    }

    Totals applyExistingPaymentsToTotals(TransactionVO transaction, Totals totals) {
        // now, we need to update these 2 totals again if we have any discounts to apply
        // check if there are any objects in the return table and subtract from original price
        // if we don't do this, discounts will be applied incorrectly on transactions with returns
        if(transaction.payment_list.size() > 0){
            for(PaymentVO payment : transaction.payment_list){
                totals.listOfExistingPaymentsToApply.add(payment.amountpaid)
            }
        }
        return totals
    }


    @Transactional
    TransactionVO checkForExistingDiscountOfSameProducttypeAndCreditBackToTransactionTotals(
            TransactionVO transactionVO,
            String transactionid,
            Integer producttypeid
    ) {
        List<DiscountVO> discountsCopy = new ArrayList<>(transactionVO.getDiscount_list());

        for (DiscountVO existingOldDiscount : discountsCopy) {
            if (existingOldDiscount.getIsactive() == 1 &&
                    existingOldDiscount.product == null && // don't roll back any discounts that are linked to a product
                    existingOldDiscount.getProducttype().getProducttypeid().equals(producttypeid)
                    && existingOldDiscount.getIsactive() == 1) {

                // Deactivate the matching discount
                existingOldDiscount.setIsactive(0);
                existingOldDiscount.setUpdateTimeStamp(LocalDateTime.now());
                discountRepo.save(existingOldDiscount);

            }
        }

        return transactionVO;
    }


    @Transactional
    public TransactionVO executeApplyDiscountToTransactionByProduct(
            TransactionVO transactionVO,
            String transactionid,
            ProductVO productVO,
            Integer quantityinscope
    ) {

        TransactionVO existingTransaction = transactionRepo.findById(Integer.valueOf(transactionid)).orElseThrow({ new EntityNotFoundException("Transaction not found: " + transactionid) });

        // Remove existing discounts of the same product and credit back the discount to original total
        // before continueing with applying the new discount
        // NOTE: do not care about quantity here, because we are simply removing any old product discount of matching product_id
        existingTransaction = checkForExistingDiscountOfSameProductAndCreditBackToTransactionTotalsByProduct(
                existingTransaction,
                transactionid,
                Integer.valueOf(productVO.product_id)
        );

        if (transactionVO.getDiscount().getDiscountamount() > 0) {

            // Prepare new discount
            DiscountVO newDiscount = createDiscountByProduct(transactionVO.getDiscount(), existingTransaction, productVO, quantityinscope);

            // Save the new discount
            DiscountVO savedDiscount = discountRepo.save(newDiscount);

            // Add the new discount to the transaction's discount list
            savedDiscount.getTransaction().getDiscount_list().add(savedDiscount);

            // Apply discounts to the transaction
            // NOTE: this is handling ALL discounts including product type discounts and product discounts...
            savedDiscount.setTransaction(applyAllDiscountsToTransaction(savedDiscount.getTransaction()));

            // Save the updated transaction
            transactionVO = transactionRepo.save(savedDiscount.getTransaction());
        }

        return transactionVO;
    }


    @Transactional
    TransactionVO checkForExistingDiscountOfSameProductAndCreditBackToTransactionTotalsByProduct(
            TransactionVO transactionVO,
            String transactionid,
            Integer productid
    ) {
        List<DiscountVO> discountsCopy = new ArrayList<>(transactionVO.getDiscount_list());

        for (DiscountVO existingOldDiscount : discountsCopy) {
            if (existingOldDiscount.getIsactive() == 1 &&
                    existingOldDiscount.getProduct().getProduct_id().equals(productid)) {

                // Deactivate the matching discount
                existingOldDiscount.setIsactive(0);
                existingOldDiscount.setUpdateTimeStamp(LocalDateTime.now());
                discountRepo.save(existingOldDiscount);

//                // Re-fetch the transaction to ensure the latest state is loaded
//                transactionVO = transactionRepo.findById(Integer.valueOf(transactionid)).orElseThrow({ new EntityNotFoundException("Transaction not found: " + transactionid) });
//
//                // Recalculate totals by crediting back the removed discount
//                transactionVO = removeDiscountFromTransactionReCalcTotalsByProduct(transactionVO, existingOldDiscount, transactionVO.total);
            }
        }

        return transactionVO;
    }

    @Transactional
    TransactionVO removeDiscountFromTransactionReCalcTotalsByProduct(
            TransactionVO transactionVO,
            DiscountVO removedDiscount,
            double currenttransactionamount

    ) {
        transactionVO = checkoutService.calculateTotalsForRemovingExistingDiscountByProduct(transactionVO, currenttransactionamount, removedDiscount)

        return transactionVO
    }

    @Transactional
    private DiscountVO createDiscountByProduct(
            DiscountVO incomingDiscount,
            TransactionVO transaction,
            ProductVO productVO,
            int quantity
    ) {


        DiscountVO newDiscount = new DiscountVO();
        newDiscount.setDiscountamount(incomingDiscount.getDiscountamount());
//        newDiscount.setProducttype(producttypevo);
        newDiscount.setProduct(productVO);
        newDiscount.setQuantity(quantity);
        newDiscount.setName("Product: " + productVO.name)
        newDiscount.setDescription("Discount applied based on a product");
        newDiscount.setIsactive(1);
        newDiscount.setTransaction(transaction);
        newDiscount.setCreateTimeStamp(LocalDateTime.now());
        newDiscount.setUpdateTimeStamp(LocalDateTime.now());

        return newDiscount;
    }



    @Transactional
    void checkToSeeIfDiscountQuantityIsMoreThanProductQuantityInTransaction(TransactionVO transactionVO, DiscountVO discountVO){

        // if discount is null it means there is no latest discount
        if(discountVO != null){

            int productcount = 0
            for(ProductVO productVO : transactionVO.getProduct_list()){
                if(productVO.product_id == discountVO.product.product_id){
                    productcount ++ //
                }
            }

            if(discountVO.quantity > productcount){
                System.out.print("INFO: Quantity of discount is more than quantity of product in transaction: " + discountVO.quantity + " > " + productcount)
                discountVO = discountRepo.findById(discountVO.discountid).get() // update in scope for hibernate
                discountVO.setQuantity(productcount)
                discountVO = discountRepo.save(discountVO)
            }
        }


    }

    @Transactional
    TransactionVO calculateTotal(TransactionVO transactionVO){

        // find value of all discounts on the transaction
        Double finaltotal = 0.00
        Double orignalprice = transactionVO.getOriginalprice()

        Double valueOfAllReturnedProducts = 0.00
        // first calculate the value of any returned products
        for(ReturnVO returnVO : transactionVO.return_list) {
            valueOfAllReturnedProducts += returnVO.product.price // add up the non discounted price of all returns on the transaction
        }

        finaltotal = orignalprice - valueOfAllReturnedProducts

        finaltotal = (finaltotal - transactionVO.paid)

        // it's only possible to get customer credit if you've submitted a payment
        if (finaltotal < 0 && transactionVO.paid > 0) {

            // This means we have a negative balance → customer should get credit back
            transactionVO.customercredit = Math.abs(finaltotal)
        }


        // we need to account for any discounts that were applied to the products in the transaction
        // here we add up all the discounts applied to the products in the transaction.


        // we only apply the current discounts on the transaction to the final total AFTER we have calculated customer credit.
        // Customer Credit DOES NOT GET TO BE CALCULATED including current discounts
        Double totaldiscountfromallproducts = transactionHelper.calculateAllDiscountsThatHaveBeenAppliedToTransaction(transactionVO)

//        if(finaltotal < 0){
//            finaltotal = (finaltotal - totaldiscountfromallproducts)
//        }
        finaltotal = (finaltotal - totaldiscountfromallproducts)


        double total = Math.max(0,finaltotal)


        transactionVO.total = total
        transactionVO.totalwithtax = total
        transactionVO.updateTimeStamp = LocalDateTime.now()
        transactionVO = transactionRepo.save(transactionVO)
        return transactionVO
    }


}
