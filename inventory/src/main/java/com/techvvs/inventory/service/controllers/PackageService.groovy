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
    PackageService packageService

    @Autowired
    PackageHelper packageHelper

    @Autowired
    CustomerRepo customerRepo

    @Autowired
    PackageTypeRepo packageTypeRepo

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

        Optional<ProductVO> productVO = productRepo.findByBarcode(barcode)

        // todo: on second time thru we need to fully hydrate the customer and product_set before saving

        if(!productVO.empty){

            // update the product cart list association
            if(productVO.get().package_list == null){
                productVO.get().package_list = new ArrayList<>()
            }
            productVO.get().package_list.add(packageVO)

            productVO.get().updateTimeStamp = LocalDateTime.now()
            // when product is added to the cart, decrease the quantity remaining.
            productVO.get().quantityremaining = productVO.get().quantityremaining == 0 ? 0 : productVO.get().quantityremaining - 1
            ProductVO savedProduct = productRepo.save(productVO.get())

            if(packageVO.total == null){
                packageVO.total = 0.00
            }

            /* Cart code below */
            packageVO.total += Double.valueOf(productVO.get().price) // add the product price to the total

            // handle quantity here (have to iterate thru all product cert list and update the quantity)

            // if it's the first time adding a product we need to create the set to hold them
            if(packageVO.product_package_list == null){
                packageVO.product_package_list = new ArrayList<ProductVO>()
            }

            packageVO = refreshProductPackageList(packageVO)

            // now save the cart side of the many to many
            packageVO.product_package_list.add(savedProduct)
            packageVO.updateTimeStamp = LocalDateTime.now()
            packageVO = packageRepo.save(packageVO)
            model.addAttribute("successMessage","Package: "+productVO.get().name + " added successfully")
        } else {
            // need to bind the selected customer here otherwise the dropdown wont work
            packageVO.packagetype = packageTypeRepo.findById(packageVO.packagetype.packagetypeid).get()
            model.addAttribute("errorMessage","Product not found")
        }



        return packageVO
    }


    @Transactional
    PackageVO refreshProductPackageList(PackageVO packageVO){

        if(packageVO.packageid == 0){
            return packageVO
        }

        packageVO.product_package_list = packageRepo.findById(packageVO.packageid).get().product_package_list
        return packageVO

    }




}
