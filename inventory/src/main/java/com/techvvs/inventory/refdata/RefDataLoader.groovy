package com.techvvs.inventory.refdata

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.DiscountRepo
import com.techvvs.inventory.jparepo.LocationRepo
import com.techvvs.inventory.jparepo.LocationTypeRepo
import com.techvvs.inventory.jparepo.PackageTypeRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchTypeVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.DiscountVO
import com.techvvs.inventory.model.LocationTypeVO
import com.techvvs.inventory.model.LocationVO
import com.techvvs.inventory.model.PackageTypeVO
import com.techvvs.inventory.model.ProductTypeVO
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
    DiscountRepo discountRepo

    @Autowired
    PackageTypeRepo packageTypeRepo

    @Autowired
    Environment environment

    @Autowired
    AppConstants appConstants


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
        palletTypeVO.setName("LARGE.BOX");
        palletTypeVO.setDescription("A large box used for shipping");
        palletTypeVO.setCreateTimeStamp(LocalDateTime.now());
        palletTypeVO.setUpdateTimeStamp(LocalDateTime.now());
        packageTypeRepo.save(palletTypeVO);

        // Creating the second PackageTypeVO instance for "small box"
        PackageTypeVO smallBoxTypeVO = new PackageTypeVO();
        smallBoxTypeVO.setName("SMALL.BOX");
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
                saveProductType("INDOOR.UNIT", "Indoor Unit");
                saveProductType("CART.2G.DISPO", "2 gram disposable");
                saveProductType("CART.0.5G.ROSIN", ".5 g rosin cart");
                saveProductType("CART.1G.RESIN", "1G RESIN CART DISPO");
                saveProductType("BOOMER.UNIT", "UNIT OF BOOMS");
        }



        System.out.println("ProductType ref data loaded")
    }

    void loadCustomers(){

        List<CustomerVO> list = customerRepo.findAll()
        if(list.size() > 0){
            return // return early to not pollute database with duplicates
        }

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

        List<LocationTypeVO> list = locationTypeRepo.findAll()
        if(list.size() > 0){
            return // return early to not pollute database with duplicates
        }


        LocationTypeVO locationType1 = new LocationTypeVO();
        locationType1.setName("B2B.DISTRO");
        locationType1.setDescription("biz to biz distribution");
        locationType1.setCreateTimeStamp(LocalDateTime.now());
        locationType1.setUpdateTimeStamp(LocalDateTime.now());
        locationTypeRepo.save(locationType1);

        LocationTypeVO locationType2 = new LocationTypeVO();
        locationType2.setName("B2C.RETAIL");
        locationType2.setDescription("biz to customer retail");
        locationType2.setCreateTimeStamp(LocalDateTime.now());
        locationType2.setUpdateTimeStamp(LocalDateTime.now());

        locationTypeRepo.save(locationType2);

        System.out.println("Location Type ref data loaded");



    }


    // Helper method to create and save product types
    private void saveProductType(String name, String description) {
        ProductTypeVO productTypeVO = new ProductTypeVO();
        productTypeVO.name = name;
        productTypeVO.description = description;
        productTypeVO.setCreateTimeStamp(LocalDateTime.now());
        productTypeVO.setUpdateTimeStamp(LocalDateTime.now());

        productTypeRepo.save(productTypeVO);
    }



}
