package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.PackageRepo
import com.techvvs.inventory.jparepo.PackageTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CustomerVO
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
import java.time.LocalDateTime

@Service
class PackageService {

    @Autowired
    PackageRepo packageRepo

    @Autowired
    ProductRepo productRepo

    @Autowired
    BarcodeHelper barcodeHelper

    @Autowired
    ProductService productService

    @Autowired
    PackageTypeRepo packageTypeRepo

    @Autowired
    TechvvsAppUtil techvvsAppUtil

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
            packageVO.setPackagetype(packageTypeVO);

            packageVO.packageid = null
            // Save the package
            packageVO = packageRepo.save(packageVO);

            // Rebind the barcode after saving
            packageVO.setBarcode(barcode);
        } else {
            // TODO: Handle case where a cart already exists
        }

        return packageVO;
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






}
