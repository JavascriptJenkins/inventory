package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.barcode.service.BarcodeService
import com.techvvs.inventory.jparepo.CrateRepo
import com.techvvs.inventory.jparepo.DeliveryRepo
import com.techvvs.inventory.jparepo.PackageRepo
import com.techvvs.inventory.jparepo.PackageTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.CrateVO
import com.techvvs.inventory.model.DeliveryVO
import com.techvvs.inventory.model.PackageVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.util.TechvvsAppUtil
import org.springframework.beans.factory.annotation.Autowired
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

    SecureRandom secureRandom = new SecureRandom()

    @Transactional
    void createPackage(){

    }


    @Transactional
    CrateVO deletePackageFromCrate(CrateVO crateVO, String barcode){

        for(PackageVO packageVO : crateVO.package_list){
            if(packageVO.packagebarcode == barcode){
                crateVO.package_list.remove(packageVO)
//                crateVO.total = crateVO.total - packageVO.price // subtract the price from the cart total

                // remove the crate association from the package
                packageVO.crate = null

                packageVO.updateTimeStamp = LocalDateTime.now()
                packageVO.isprocessed = 0 // reset it to not processed
                packageRepo.save(packageVO)
                break
            }
        }

        crateVO.updateTimeStamp = LocalDateTime.now()
        crateVO = crateRepo.save(crateVO)


        return crateVO

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
           // deliveryVO.setWeight(0) // todo: set weight from product table in database
            deliveryVO.deliverybarcode = barcodeHelper.generateBarcodeData(generateOneDigitNumber(), generateOneDigitNumber(), generateSevenDigitNumber(), generateOneDigitNumber()); // generate barcode....
//            packageVO.setPackagetype(packageTypeVO);

            deliveryVO.deliveryid = null
            // Save the package
            deliveryVO = deliveryRepo.save(deliveryVO);

            // Rebind the barcode after saving
            deliveryVO.setBarcode(barcode);
        } else {
            // TODO: Handle case where a cart already exists
        }

        // this will generate and save a sheet of pdf barcodes for the package when it is created
        // barcodeService.createBarcodeSheetForSinglePackageUPCA(packageVO)
        // this will generate and save a sheet of pdf barcodes for the package when it is created
        barcodeService.createBarcodeSheetForSingleDeliveryUPCA(deliveryVO)


        return deliveryVO;
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






}