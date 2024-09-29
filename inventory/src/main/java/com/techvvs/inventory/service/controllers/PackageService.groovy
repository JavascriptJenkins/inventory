package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.barcode.service.BarcodeService
import com.techvvs.inventory.jparepo.CrateRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.DeliveryRepo
import com.techvvs.inventory.jparepo.PackageRepo
import com.techvvs.inventory.jparepo.PackageTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CrateVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.DeliveryVO
import com.techvvs.inventory.model.PackageTypeVO
import com.techvvs.inventory.model.PackageVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.util.TechvvsAppUtil
import com.techvvs.inventory.viewcontroller.helper.PackageHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.ui.Model

import javax.transaction.Transactional
import java.security.SecureRandom
import java.time.LocalDateTime

@Service
class PackageService {

    @Autowired
    PackageRepo packageRepo

    @Autowired
    ProductRepo productRepo

    @Autowired
    CrateRepo crateRepo

    @Autowired
    DeliveryRepo deliveryRepo

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
    PackageVO deleteProductFromPackage(PackageVO packageVO, String barcode){

        PackageVO packagetoremove = new PackageVO(packageid: 0)
        // we are only removing one product at a time
        for(ProductVO productVO : packageVO.product_package_list){
            if(productVO.barcode == barcode){
                packageVO.product_package_list.remove(productVO)
                packageVO.total = packageVO.total - productVO.price // subtract the price from the cart total

                productVO.quantityremaining = productVO.quantityremaining + 1
                // remove the cart association from the product
                for(PackageVO existingPackage : productVO.package_list){
                    if(existingPackage.packageid == packageVO.packageid){
                        packagetoremove = existingPackage
                    }
                }
                productVO.package_list.remove(packagetoremove)

                productVO.updateTimeStamp = LocalDateTime.now()
                productRepo.save(productVO)
                break
            }
        }

        packageVO.updateTimeStamp = LocalDateTime.now()
        packageVO = packageRepo.save(packageVO)


        return packageVO

    }



    @Transactional
    PackageVO savePackageIfNew(PackageVO packageVO) {
        // Ensure packageVO is not null before any operations
        if (packageVO == null) {
            throw new IllegalArgumentException("PackageVO cannot be null");
        }

        String barcode = packageVO.getBarcode();

        // Check if the package is new and if the product list is empty
        if ((packageVO.getPackageid() == null || packageVO.getPackageid() == 0)
                && (packageVO.getProduct_package_list() == null || packageVO.getProduct_package_list().isEmpty())) {

            // Fetch the PackageTypeVO to ensure it exists
            PackageTypeVO packageTypeVO = packageTypeRepo.findById(packageVO.getPackagetype().getPackagetypeid()).get()

            // Set timestamps and initial processing state
            packageVO.setUpdateTimeStamp(LocalDateTime.now());
            packageVO.setCreateTimeStamp(LocalDateTime.now());
            packageVO.setIsprocessed(0);
            packageVO.setWeight(0) // todo: set weight from product table in database
            packageVO.packagebarcode = barcodeHelper.generateBarcodeData(generateOneDigitNumber(), generateOneDigitNumber(), generateSevenDigitNumber(), generateOneDigitNumber()); // generate barcode....
            packageVO.setPackagetype(packageTypeVO);

            packageVO.packageid = null
            // Save the package
            packageVO = packageRepo.save(packageVO);

            // Rebind the barcode after saving
            packageVO.setBarcode(barcode);
        } else {
            // TODO: Handle case where a cart already exists
        }

        // this will generate and save a sheet of pdf barcodes for the package when it is created
        barcodeService.createBarcodeSheetForSinglePackageUPCA(packageVO)

        return packageVO;
    }


    def generateSevenDigitNumber() {
        return 1000000 + secureRandom.nextInt(9000000)  // Generates a number between 1,000,000 and 9,999,999
    }

    def generateOneDigitNumber() {
        return secureRandom.nextInt(9) + 1  // Generates a number between 1 and 9
    }

    // add product to cart and then update the cart and product associations
    @Transactional
    PackageVO searchForProductByBarcodeAndPackage(PackageVO packageVO, Model model, Optional<Integer> page, Optional<Integer> size

    ){


        // validate the barcode and add the last digit here
        int checksum =barcodeHelper.calculateUPCAChecksum(packageVO.barcode)

        String barcode = packageVO.barcode + String.valueOf(checksum)

        if(packageVO.quantityselected == 0){
            productService.saveProductPackageAssociations(barcode, packageVO, model, 1)
        } else {
            int j = 0;
            // run the product save once for every quantity selected
            for (int i = 0; i < packageVO.quantityselected; i++) {
                j++
                productService.saveProductPackageAssociations(barcode, packageVO, model, j)
            }
        }



        return packageVO
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

    @Transactional
    void savePackageCrateAssociations(String barcode, CrateVO crateVO, Model model, int counter) {
        Optional<PackageVO> packageVO = packageRepo.findByPackagebarcode(barcode)


        if(!packageVO.empty){

            packageVO.get().crate = crateVO // add the crate association to the package

            packageVO.get().updateTimeStamp = LocalDateTime.now()
            // when product is added to the cart, decrease the quantity remaining.
            packageVO.get().isprocessed = 1 // set this to processed which means it is now in a crate
            PackageVO savedPackage = packageRepo.save(packageVO.get())

            if(crateVO.total == null){
                crateVO.total = 0.00
            }

            /* Fill this out later if needed */
//            crateVO.total += Double.valueOf(packageVO.get().price) // add the product price to the total

            // handle quantity here (have to iterate thru all product cert list and update the quantity)

            // if it's the first time adding a product we need to create the set to hold them
            if(crateVO.package_list == null){
                crateVO.package_list = new ArrayList<PackageVO>()
            }

            crateVO = refreshPackageCrateList(crateVO)

            // now save the cart side of the many to many
            crateVO.package_list.add(savedPackage)
            crateVO.updateTimeStamp = LocalDateTime.now()
            crateVO = crateRepo.save(crateVO)
            model.addAttribute("successMessage","Package: "+packageVO.get().name + " added successfully. Quantity: "+counter)
        } else {
            // need to bind the selected customer here otherwise the dropdown wont work
//            crateVO.packagetype = packageTypeRepo.findById(crateVO.packagetype.packagetypeid).get()
            model.addAttribute("errorMessage","Package not found")
        }


    }

    @Transactional
    void savePackageDeliveryAssociations(String barcode, DeliveryVO deliveryVO, Model model, int counter) {
        Optional<PackageVO> packageVO = packageRepo.findByPackagebarcode(barcode)


        if(!packageVO.empty){

            packageVO.get().delivery = deliveryVO // add the delivery association to the package

            packageVO.get().updateTimeStamp = LocalDateTime.now()
            // when product is added to the cart, decrease the quantity remaining.

            // set this to processed which means it is now in a delivery (can either be in a crate or in a delivery)
            packageVO.get().isprocessed = 1
            PackageVO savedPackage = packageRepo.save(packageVO.get())

            if(deliveryVO.total == null){
                deliveryVO.total = 0.00
            }

            /* Fill this out later if needed */
//            deliveryVO.total += Double.valueOf(packageVO.get().price) // add the product price to the total

            // handle quantity here (have to iterate thru all product cert list and update the quantity)

            // if it's the first time adding a product we need to create the set to hold them
            if(deliveryVO.package_list == null){
                deliveryVO.package_list = new ArrayList<PackageVO>()
            }

            deliveryVO = refreshPackageDeliveryList(deliveryVO)

            // now save the cart side of the many to many
            deliveryVO.package_list.add(savedPackage)
            deliveryVO.updateTimeStamp = LocalDateTime.now()
            deliveryVO = deliveryRepo.save(deliveryVO)
            model.addAttribute("successMessage","Package: "+packageVO.get().name + " added successfully. Quantity: "+counter)
        } else {
            // need to bind the selected customer here otherwise the dropdown wont work
//            deliveryVO.packagetype = packageTypeRepo.findById(deliveryVO.packagetype.packagetypeid).get()
            model.addAttribute("errorMessage","Package not found")
        }


    }


    @Transactional
    void saveCrateDeliveryAssociations(String barcode, DeliveryVO deliveryVO, Model model, int counter) {
        Optional<CrateVO> crateVO = crateRepo.findByCratebarcode(barcode)


        if(!crateVO.empty){

            crateVO.get().delivery = deliveryVO // add the delivery association to the package

            crateVO.get().updateTimeStamp = LocalDateTime.now()
            // when product is added to the cart, decrease the quantity remaining.

            // set this to processed which means it is now in a delivery (can either be in a crate or in a delivery)
            crateVO.get().isprocessed = 1
            CrateVO savedCrate = crateRepo.save(crateVO.get())

            if(deliveryVO.total == null){
                deliveryVO.total = 0.00
            }

            /* Fill this out later if needed */
//            deliveryVO.total += Double.valueOf(packageVO.get().price) // add the product price to the total

            // handle quantity here (have to iterate thru all product cert list and update the quantity)

            // if it's the first time adding a product we need to create the set to hold them
            if(deliveryVO.crate_list == null){
                deliveryVO.crate_list = new ArrayList<CrateVO>()
            }

            deliveryVO = refreshCrateDeliveryList(deliveryVO)

            // now save the cart side of the many to many
            deliveryVO.crate_list.add(savedCrate)
            deliveryVO.updateTimeStamp = LocalDateTime.now()
            deliveryVO = deliveryRepo.save(deliveryVO)
            model.addAttribute("successMessage","Crate: "+crateVO.get().name + " added successfully. Quantity: "+counter)
        } else {
            // need to bind the selected customer here otherwise the dropdown wont work
//            deliveryVO.cratetype = crateTypeRepo.findById(deliveryVO.cratetype.cratetypeid).get()
            model.addAttribute("errorMessage","Crate not found")
        }


    }

    @Transactional
    DeliveryVO refreshCrateDeliveryList(DeliveryVO deliveryVO){

        if(deliveryVO.deliveryid == 0){
            return deliveryVO
        }

        deliveryVO.crate_list = deliveryRepo.findById(deliveryVO.deliveryid).get().crate_list
        return deliveryVO

    }

    @Transactional
    DeliveryVO refreshPackageDeliveryList(DeliveryVO deliveryVO){

        if(deliveryVO.deliveryid == 0){
            return deliveryVO
        }

        deliveryVO.package_list = deliveryRepo.findById(deliveryVO.deliveryid).get().package_list
        return deliveryVO

    }
    @Transactional
    CrateVO refreshPackageCrateList(CrateVO crateVO){

        if(crateVO.crateid == 0){
            return crateVO
        }

        crateVO.package_list = crateRepo.findById(crateVO.crateid).get().package_list
        return crateVO

    }





}
