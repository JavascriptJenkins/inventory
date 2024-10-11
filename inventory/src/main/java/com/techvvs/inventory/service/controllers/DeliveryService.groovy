package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.barcode.service.BarcodeService
import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.CrateRepo
import com.techvvs.inventory.jparepo.DeliveryRepo
import com.techvvs.inventory.jparepo.LocationRepo
import com.techvvs.inventory.jparepo.PackageRepo
import com.techvvs.inventory.jparepo.PackageTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.CrateVO
import com.techvvs.inventory.model.DeliveryVO
import com.techvvs.inventory.model.LocationVO
import com.techvvs.inventory.model.PackageVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.util.FormattingUtil
import com.techvvs.inventory.util.TechvvsAppUtil
import com.techvvs.inventory.viewcontroller.helper.CrateHelper
import com.techvvs.inventory.viewcontroller.helper.DeliveryHelper
import com.techvvs.inventory.viewcontroller.helper.PackageHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.ui.Model

import javax.transaction.Transactional
import java.security.SecureRandom
import java.time.LocalDateTime

@Service
class DeliveryService {

    @Autowired
    PackageRepo packageRepo

    @Autowired
    PackageService packageService

    @Autowired
    CrateRepo crateRepo

    @Autowired
    DeliveryRepo deliveryRepo

    @Autowired
    TransactionRepo transactionRepo

    @Autowired
    LocationRepo locationRepo

    @Autowired
    AppConstants appConstants

    @Autowired
    ProductRepo productRepo

    @Autowired
    BarcodeHelper barcodeHelper

    @Autowired
    BarcodeService barcodeService

    @Autowired
    ProductService productService

    @Autowired
    PackageTypeRepo packageTypeRepo

    @Autowired
    TechvvsAppUtil techvvsAppUtil

    @Autowired
    PackageHelper packageHelper

    @Autowired
    DeliveryHelper deliveryHelper

    @Autowired
    CrateHelper crateHelper

    @Autowired
    FormattingUtil formattingUtil

    @Autowired
    Environment environment

    SecureRandom secureRandom = new SecureRandom()

    @Transactional
    void createPackage(){

    }


    @Transactional
    DeliveryVO deletePackageFromDelivery(DeliveryVO deliveryVO, String barcode){

        for(PackageVO packageVO : deliveryVO.package_list){
            if(packageVO.packagebarcode == barcode){
                deliveryVO.package_list.remove(packageVO)

                // subtract the price from total and make sure it doesn't go below 0
                double result = deliveryVO.total - packageVO.total
                deliveryVO.total = result < 0.0 ? 0.0 : deliveryVO.total - packageVO.total

                // remove the delivery association from the package
                packageVO.delivery = null

                packageVO.updateTimeStamp = LocalDateTime.now()
                packageVO.isprocessed = 0 // reset it to not processed
                packageRepo.save(packageVO)
                break
            }
        }

        deliveryVO.updateTimeStamp = LocalDateTime.now()
        deliveryVO = deliveryRepo.save(deliveryVO)


        return deliveryVO

    }

    @Transactional
    DeliveryVO deleteCrateFromDelivery(DeliveryVO deliveryVO, String barcode){

        for(CrateVO crateVO : deliveryVO.crate_list){
            if(crateVO.cratebarcode == barcode){
                deliveryVO.crate_list.remove(crateVO)
                // subtract the price from total and make sure it doesn't go below 0
                double result = deliveryVO.total - crateVO.total
                deliveryVO.total = result < 0.0 ? 0.0 : deliveryVO.total - crateVO.total
                // remove the delivery association from the crate
                crateVO.delivery = null

                crateVO.updateTimeStamp = LocalDateTime.now()
                crateVO.isprocessed = 0 // reset it to not processed
                crateRepo.save(crateVO)
                break
            }
        }

        deliveryVO.updateTimeStamp = LocalDateTime.now()
        deliveryVO = deliveryRepo.save(deliveryVO)


        return deliveryVO

    }



    @Transactional
    DeliveryVO saveDeliveryIfNew(DeliveryVO deliveryVO) {
        // Ensure packageVO is not null before any operations
        if (deliveryVO == null) {
            throw new IllegalArgumentException("DeliveryVO cannot be null");
        }

        String barcode = deliveryVO.getBarcode();

        // Check if the package is new
        if ((deliveryVO.deliveryid == null || deliveryVO.deliveryid == 0)) {

//            // Fetch the PackageTypeVO to ensure it exists
//            PackageTypeVO packageTypeVO = packageTypeRepo.findById(packageVO.getPackagetype().getPackagetypeid()).get()

            // Set timestamps and initial processing state
            deliveryVO.setUpdateTimeStamp(LocalDateTime.now());
            deliveryVO.setCreateTimeStamp(LocalDateTime.now());
            deliveryVO.setIsprocessed(0);
            deliveryVO.setStatus(0);
            deliveryVO.total = calculateDeliveryTotal(deliveryVO)
           // deliveryVO.setWeight(0) // todo: set weight from product table in database
            deliveryVO.deliverybarcode = barcodeHelper.generateBarcodeData(generateOneDigitNumber(), generateOneDigitNumber(), generateSevenDigitNumber(), generateOneDigitNumber()); // generate barcode....
//            packageVO.setPackagetype(packageTypeVO);

            deliveryVO.deliveryid = null
            // Save the package
            deliveryVO = deliveryRepo.save(deliveryVO);

            // Rebind the barcode after saving
            deliveryVO.setBarcode(barcode);
            // this will generate and save a sheet of pdf barcodes for the package when it is created
            // barcodeService.createBarcodeSheetForSinglePackageUPCA(packageVO)
            // this will generate and save a sheet of pdf barcodes for the package when it is created
            barcodeService.createBarcodeSheetForSingleDeliveryUPCA(deliveryVO)
        } else {
            // TODO: Handle case where a cart already exists
        }



        return deliveryVO;
    }

    double calculateDeliveryTotal(DeliveryVO deliveryVO) {

        // todo: get this from a database table instead ....
        Double taxpercentage = environment.getProperty("tax.percentage", Double.class)


        // first add up totals of all packages and crates
        double cratetotal = deliveryVO.crate_list.stream().mapToDouble({ CrateVO crate -> crate.getTotal() }).sum()
        double packagetotal = deliveryVO.package_list.stream().mapToDouble({ PackageVO pkg -> pkg.getTotal() }).sum()
        double grandtotal = cratetotal + packagetotal
        double discount = 0.0;


        return formattingUtil.calculateTotalWithTax(grandtotal, taxpercentage, discount)
    }

    def generateSevenDigitNumber() {
        return 1000000 + secureRandom.nextInt(9000000)  // Generates a number between 1,000,000 and 9,999,999
    }

    def generateOneDigitNumber() {
        return secureRandom.nextInt(9) + 1  // Generates a number between 1 and 9
    }

    // add product to cart and then update the cart and product associations
    @Transactional
    DeliveryVO searchForPackageByBarcodeAndDelivery(DeliveryVO deliveryVO, Model model, Optional<Integer> page, Optional<Integer> size

    ){


        // validate the barcode and add the last digit here
        int checksum =barcodeHelper.calculateUPCAChecksum(deliveryVO.barcode)

        String barcode = deliveryVO.barcode + String.valueOf(checksum)

        // NOTE: reference the packageservice for a method that handles quantity mode when scanning
        packageService.savePackageDeliveryAssociations(barcode, deliveryVO, model, 1)




        return deliveryVO
    }


    @Transactional
    DeliveryVO searchForCrateByBarcodeAndDelivery(DeliveryVO deliveryVO, Model model, Optional<Integer> page, Optional<Integer> size

    ){


        // validate the barcode and add the last digit here
        int checksum =barcodeHelper.calculateUPCAChecksum(deliveryVO.barcode)

        String barcode = deliveryVO.barcode + String.valueOf(checksum)

        // NOTE: reference the packageservice for a method that handles quantity mode when scanning
        packageService.saveCrateDeliveryAssociations(barcode, deliveryVO, model, 1)




        return deliveryVO
    }
    // todo: NOTE - these requirements are written in the context of a pallet being delivered.  Will give user ability to create a "PACKAGE DELIVERY" in different user flow

    // todo: need to make the concept of a "Pallet" so we can have a "Pallet Building" page
    // todo: instead of creating a "Transaction", we are going to ask the user if they want to add the package to a pallet, or if they want to keep making packages.
    // todo: then, when they are done adding packages to the system and adding them to a pallet, user can navigate to the pallet building page.
    // todo: on the pallet building page, user can see all the packages they have added to the pallet.
    // todo: on the pallet building page, user can delete packages from the pallet.
    // todo: on the pallet building page, user can print barcodes and qr tags for the packages in the pallet.
    // todo: on the pallet building page, user will be required to add a CUSTOMER to the pallet.  (will add CUSTOMER to PALLET and also to PACKAGE)
    // todo: on the pallet building page, user will be required to add a DESTINATION LOCATION to the pallet.   (will add DESTINATION LOCATION to PALLET and also to PACKAGE)
    // todo: on the pallet building page, user can decide they are done building the pallet, and THEN they can submit the pallet, which will create a TRANSACTION and a DELIVERY

    // todo: Then user can navigate to the "Pallet Review" page, where they will be able to monitor the DELIVERY status of the pallet
    @Transactional
    TransactionVO processPackageGenerateNewTransaction(PackageVO packageVO) {

        ArrayList<ProductVO> newlist = packageVO.product_package_list

        TransactionVO newtransaction = new TransactionVO(

                product_list: newlist,
                updateTimeStamp: LocalDateTime.now(),
                createTimeStamp: LocalDateTime.now(),
                customervo: packageVO?.customer,
                total: packageVO.total,
//                totalwithtax: formattingUtil.calculateTotalWithTax(cartVO.total, appConstants.DEFAULT_TAX_PERCENTAGE),
                totalwithtax: packageVO.total,
                paid: 0.00,
                taxpercentage: techvvsAppUtil.dev1 ? 0 : 0, // we are not going to set a tax percentage here in non dev environments
                isprocessed: 0,
                ispackagetype: 1

        )

        newtransaction = transactionRepo.save(newtransaction)

        // only save the cart after transaction is created


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


    void hadlePackageId(
            Optional<String> packageid,
            String deliveryid,
            Optional<Integer> deliverypage,
            Optional<Integer> deliverysize,
                        Model model
    ){
        model = packageHelper.loadPackage(packageid.get(), model)
        // check to see if this package is already in a delivery
        PackageVO packageVO = (PackageVO) model.getAttribute("package")
        if(packageVO.delivery != null){
            // load the associated delivery for the package if it exists
            model = deliveryHelper.loadDelivery(String.valueOf(packageVO.delivery.deliveryid), model, deliverypage, deliverysize)
        } else {
            model = deliveryHelper.loadDelivery(deliveryid, model, deliverypage, deliverysize)
        }
        DeliveryVO deliveryVO = (DeliveryVO) model.getAttribute("delivery")
        deliveryVO.packageinscope = packageVO // if it's first time navigating from package create page, add the package to the packageinscope

    }


    void hadleCrateId(
            Optional<String> crateid,
            String deliveryid,
            Optional<Integer> cratepage,
            Optional<Integer> cratesize,
            Optional<Integer> deliverypage,
            Optional<Integer> deliverysize,
            Model model
    ){
        model = crateHelper.loadCrate(crateid.get(), model, cratepage, cratesize)
        // check to see if this package is already in a delivery
        CrateVO crateVO = (CrateVO) model.getAttribute("crate")
        if(crateVO.delivery != null){
            // load the associated delivery for the crate if it exists
            model = deliveryHelper.loadDelivery(String.valueOf(crateVO.delivery.deliveryid), model, deliverypage, deliverysize)
        } else {
            model = deliveryHelper.loadDelivery(deliveryid, model, deliverypage, deliverysize)
        }
        DeliveryVO deliveryVO = (DeliveryVO) model.getAttribute("delivery")
        deliveryVO.crateinscope = crateVO // if it's first time navigating from crate create page, add the crate to the crateinscope

    }


    @Transactional
    void createNewDeliveryFromTransaction(TransactionVO transactionVO){

        //hydrate location based on locationid passed in
        LocationVO existinglocation = locationRepo.findById(transactionVO.delivery.location.locationid).get()
        TransactionVO existingTransaction = transactionRepo.findById(transactionVO.getTransactionid()).get()


        // create a new delivery using the
        DeliveryVO deliverytoSave = new DeliveryVO(
                transaction: existingTransaction,
                status: appConstants.DELIVERY_STATUS_CREATED,
                name: "delivery_"+transactionVO.getTransactionid(),
                isprocessed: 0,
                iscanceled: 0,
                description: "delivery created for location: "+existinglocation.name+" from transaction: "+transactionVO.getTransactionid(),
                location: existinglocation,
                deliverybarcode: barcodeHelper.generateBarcodeData(generateOneDigitNumber(), generateOneDigitNumber(), generateSevenDigitNumber(), generateOneDigitNumber()),
                total: transactionVO.totalwithtax,
                updateTimeStamp: LocalDateTime.now(),
                createTimeStamp: LocalDateTime.now()
        )

        DeliveryVO savedDelivery = deliveryRepo.save(deliverytoSave)
        existingTransaction.delivery = savedDelivery

        // save the transaction with the new delivery that was just created
        transactionRepo.save(existingTransaction)

        barcodeService.createBarcodeSheetForSingleDeliveryUPCA(savedDelivery) // save a barcode sheet created from this delivery

    }



}
