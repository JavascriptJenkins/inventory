package com.techvvs.inventory.refdata

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.DiscountRepo
import com.techvvs.inventory.jparepo.LocationRepo
import com.techvvs.inventory.jparepo.LocationTypeRepo
import com.techvvs.inventory.jparepo.LockerRepo
import com.techvvs.inventory.jparepo.PackageTypeRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchTypeVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.DiscountVO
import com.techvvs.inventory.model.LocationTypeVO
import com.techvvs.inventory.model.LocationVO
import com.techvvs.inventory.model.LockerVO
import com.techvvs.inventory.model.PackageTypeVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.qrcode.impl.QrCodeGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

import java.time.LocalDateTime

// The purpose of this class is to load reference data on startup. Things like BatchTypes and other reference data
@Component
class RefDataLoader {

    @Autowired
    BatchTypeRepo batchTypeRepo

    @Autowired
    ProductTypeRepo productTypeRepo

    @Autowired
    CustomerRepo customerRepo

    @Autowired
    LocationTypeRepo locationTypeRepo

    @Autowired
    LockerRepo lockerRepo

    @Autowired
    DiscountRepo discountRepo

    @Autowired
    PackageTypeRepo packageTypeRepo

    @Autowired
    Environment environment

    @Autowired
    AppConstants appConstants

    @Autowired
    QrCodeGenerator qrCodeGenerator


    void loadRefData(){
        String tenant = environment.getProperty("techvvs.tenant")
        loadBatchTypes()
        loadProductTypes(tenant)
        loadCustomers()
        loadPackageTypes()
        loadDiscounts()
        loadLocationTypes()
    }

    void loadBatchTypes(){
        List<BatchTypeVO> list = batchTypeRepo.findAll()
        if(list.size() > 0){
            return // return early to not pollute database with duplicates
        }
        BatchTypeVO batchTypeVO = new BatchTypeVO();
//        batchTypeVO.setBatch_type_id(1)
        batchTypeVO.name = "INDOOR.MIXED"
        batchTypeVO.description = "Mainly indoor but a mixed batch"

        batchTypeVO.setCreateTimeStamp(LocalDateTime.now());
        batchTypeVO.setUpdateTimeStamp(LocalDateTime.now());

        batchTypeRepo.save(batchTypeVO)
        System.out.println("BatchType ref data loaded")
    }

    void loadPackageTypes() {
        List<PackageTypeVO> list = packageTypeRepo.findAll()
        if(list.size() > 0){
            return // return early to not pollute database with duplicates
        }
        // Creating the first PackageTypeVO instance for "large box"
        PackageTypeVO palletTypeVO = new PackageTypeVO();
        palletTypeVO.setName(appConstants.LARGE_BOX);
        palletTypeVO.setDescription("A large box used for shipping");
        palletTypeVO.setCreateTimeStamp(LocalDateTime.now());
        palletTypeVO.setUpdateTimeStamp(LocalDateTime.now());
        packageTypeRepo.save(palletTypeVO);

        // Creating the second PackageTypeVO instance for "small box"
        PackageTypeVO smallBoxTypeVO = new PackageTypeVO();
        smallBoxTypeVO.setName(appConstants.SMALL_BOX);
        smallBoxTypeVO.setDescription("A small box used for shipping");
        smallBoxTypeVO.setCreateTimeStamp(LocalDateTime.now());
        smallBoxTypeVO.setUpdateTimeStamp(LocalDateTime.now());
        packageTypeRepo.save(smallBoxTypeVO);

        System.out.println("PackageType reference data loaded");
    }

    void loadProductTypes(String tenant){

        List<ProductTypeVO> list = productTypeRepo.findAll()
        if(list.size() > 0){
            return // return early to not pollute database with duplicates
        }

        System.out.println("Loading product types for tenant: " + tenant)
        switch (tenant){
            case appConstants.TENANT_HIGHLAND:
                // Load default product types
                saveProductType("INDOOR", "Indoor Unit");
                saveProductType("DEP", "light deps");
                saveProductType("OUTS", "outdoor");
                saveProductType("LOWS", "move em like its hot");
                saveProductType("PRODUCT.CART", "cartridges");
                saveProductType("PRODUCT.JOINTPACK", "joints in packs");
                saveProductType("PRODUCT.CRUMBLE.BADDER", "crumble badder");
                saveProductType("PRODUCT.TERPSAUCE", "terp sauce");
                break;

            case appConstants.TENANT_TEST1:
                // Load default product types
                saveProductType("INDOOR", "Indoor Unit");
                saveProductType("DEP", "light deps");
                saveProductType("OUTS", "outdoor");
                saveProductType("LOWS", "move em like its hot");
                saveProductType("PRODUCT.CART", "cartridges");
                saveProductType("PRODUCT.JOINTPACK", "joints in packs");
                saveProductType("PRODUCT.CRUMBLE.BADDER", "crumble badder");
                saveProductType("PRODUCT.TERPSAUCE", "terp sauce");
                break;

            case appConstants.TENANT_TEST:
                // Load default product types
                saveProductType("INDOOR", "Indoor Unit");
                saveProductType("DEP", "light deps");
                saveProductType("OUTS", "outdoor");
                saveProductType("LOWS", "move em like its hot");
                saveProductType("PRODUCT.CART", "cartridges");
                saveProductType("PRODUCT.JOINTPACK", "joints in packs");
                saveProductType("PRODUCT.CRUMBLE.BADDER", "crumble badder");
                saveProductType("PRODUCT.TERPSAUCE", "terp sauce");
                break;

            default:
                // Load default product types
                saveProductType("INDOOR", "Indoor Unit");
                saveProductType("DEP", "light deps");
                saveProductType("OUTS", "outdoor");
                saveProductType("LOWS", "move em like its hot");
                saveProductType("PRODUCT.CART", "cartridges");
                saveProductType("PRODUCT.JOINTPACK", "joints in packs");
                saveProductType("PRODUCT.CRUMBLE.BADDER", "crumble badder");
                saveProductType("PRODUCT.TERPSAUCE", "terp sauce");
        }



        System.out.println("ProductType ref data loaded")
    }

    void loadCustomers(){

//        List<CustomerVO> list = customerRepo.findAll()
//        if(list.size() > 0){
//            return // return early to not pollute database with duplicates
//        }

//        CustomerVO customerVO = new CustomerVO();
//        customerVO.name = "John Doe"
//        customerVO.email = "mrchihuahua@techvvs.io"
//        customerVO.phone = "6127673388"
//        customerVO.address = "6969 420 ave"
//        customerVO.address2 = "apartment 9"
//        customerVO.notes = "likes to touch your butt"
//        customerVO.setCreateTimeStamp(LocalDateTime.now());
//        customerVO.setUpdateTimeStamp(LocalDateTime.now());
//        customerRepo.save(customerVO)
//
//        CustomerVO customerVO2 = new CustomerVO();
//        customerVO2.name = "Dildino Baggins"
//        customerVO2.email = "bagboi@techvvs.io"
//        customerVO2.phone = "7872228888"
//        customerVO2.address = "69 425 ave"
//        customerVO2.address2 = "apartment 8"
//        customerVO2.notes = "long hair doesnt care"
//        customerVO2.setCreateTimeStamp(LocalDateTime.now());
//        customerVO2.setUpdateTimeStamp(LocalDateTime.now());
//        customerRepo.save(customerVO2)

        System.out.println("Customer ref data NOT BEING loaded")
    }


    void loadDiscounts() {

        List<DiscountVO> discountVOS = discountRepo.findAll()
        if(discountVOS.size() > 0){
            return // return early to not pollute database with duplicates
        }
//        for (int i = 1; i <= 100; i++) {
//            DiscountVO discountVO = new DiscountVO();
//            discountVO.name = i + "% off";
//            discountVO.description = i + "% discount";
//            discountVO.discountamount = 0.00; // Keep discount amount constant for percentage discounts
//            discountVO.discountpercentage = i;
//
//            discountVO.setCreateTimeStamp(LocalDateTime.now());
//            discountVO.setUpdateTimeStamp(LocalDateTime.now());
//            discountRepo.save(discountVO);
//        }
//
//        System.out.println("Percentage discount options loaded");
//
//        for (int i = 1; i <= 10; i++) {
//            DiscountVO discountVO = new DiscountVO();
//            discountVO.name = "${i * 100} off";
//            discountVO.description = "${i * 100} dollar discount";
//            discountVO.discountamount = i * 100.00;
//            discountVO.discountpercentage = 0;
//
//            discountVO.setCreateTimeStamp(LocalDateTime.now());
//            discountVO.setUpdateTimeStamp(LocalDateTime.now());
//            discountRepo.save(discountVO);
//        }

        System.out.println("Discount ref data NOT BEING loaded");

    }


    void loadLocationTypes(){

        if(locationTypeRepo.findByName(appConstants.B2B_DISTRO).present){
            // nothing
        } else {
            LocationTypeVO locationType1 = new LocationTypeVO();
            locationType1.setName(appConstants.B2B_DISTRO);
            locationType1.setDescription("biz to biz distribution");
            locationType1.setCreateTimeStamp(LocalDateTime.now());
            locationType1.setUpdateTimeStamp(LocalDateTime.now());
            locationTypeRepo.save(locationType1);
        }

        if(locationTypeRepo.findByName(appConstants.B2C_RETAIL).present){
            // nothing
        } else {
            LocationTypeVO locationType2 = new LocationTypeVO();
            locationType2.setName("B2C.RETAIL");
            locationType2.setDescription("biz to customer retail");
            locationType2.setCreateTimeStamp(LocalDateTime.now());
            locationType2.setUpdateTimeStamp(LocalDateTime.now());
            locationTypeRepo.save(locationType2);
        }

        if(locationTypeRepo.findByName(appConstants.ADHOC_CUSTOMER_DELIVERY).present){
            // nothing
        } else {
            LocationTypeVO locationType3 = new LocationTypeVO();
            locationType3.setName("ADHOC.CUSTOMER.DELIVERY");
            locationType3.setDescription("adhoc order from a customer for delivery fulfillment");
            locationType3.setCreateTimeStamp(LocalDateTime.now());
            locationType3.setUpdateTimeStamp(LocalDateTime.now());
            locationTypeRepo.save(locationType3);
        }

        if(locationTypeRepo.findByName(appConstants.ADHOC_CUSTOMER_PICKUP).present){
            // nothing
        } else {
            LocationTypeVO locationType4 = new LocationTypeVO();
            locationType4.setName("ADHOC.CUSTOMER.PICKUP");
            locationType4.setDescription("adhoc order from a customer for curbside pickup fulfillment");
            locationType4.setCreateTimeStamp(LocalDateTime.now());
            locationType4.setUpdateTimeStamp(LocalDateTime.now());
            locationTypeRepo.save(locationType4);
        }


        System.out.println("Location Type ref data loaded");



    }


    void loadDefaultDeliveryLockers(){


        if(lockerRepo.findByName("locker-1").present){
            // don't do anything, we already have default lockers loaded
            System.out.println("20 Default Lockers NOT Loaded in Ref Data.  Locker-1 already exists. ");
        } else {
            int amtOfLockersToCreate = 20
            for(amtOfLockersToCreate; amtOfLockersToCreate > 0; amtOfLockersToCreate--){
                LockerVO savedlocker = lockerRepo.save(new LockerVO(
                        name: "locker-" + amtOfLockersToCreate,
                        description: "locker-" + amtOfLockersToCreate,
                        notes: "default locker for delivering products.",
                        createTimeStamp: LocalDateTime.now(),
                        updateTimeStamp: LocalDateTime.now()))

                savedlocker.setUpdateTimeStamp(LocalDateTime.now())
                savedlocker.lockerqrlink = qrCodeGenerator.buildQrLinkForLockerItem(String.valueOf(savedlocker.lockerid))
                lockerRepo.save(savedlocker)
            }
            System.out.println("20 Default Lockers Loaded in Ref Data");
        }

    }

    // Helper method to create and save product types
    private void saveProductType(String name, String description) {


        if(productTypeRepo.existsByName(name)){
            System.out.println("SKIPPING: " + name + " already exists")

        } else {
            ProductTypeVO productTypeVO = new ProductTypeVO();
            productTypeVO.name = name;
            productTypeVO.description = description;
            productTypeVO.setCreateTimeStamp(LocalDateTime.now());
            productTypeVO.setUpdateTimeStamp(LocalDateTime.now());

            productTypeRepo.save(productTypeVO);
        }

    }



}
