package com.techvvs.inventory.refdata

import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.DiscountRepo
import com.techvvs.inventory.jparepo.PackageTypeRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchTypeVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.DiscountVO
import com.techvvs.inventory.model.PackageTypeVO
import com.techvvs.inventory.model.ProductTypeVO
import org.springframework.beans.factory.annotation.Autowired
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
    DiscountRepo discountRepo

    @Autowired
    PackageTypeRepo packageTypeRepo


    void loadRefData(){
        loadBatchTypes()
        loadProductTypes()
        loadCustomers()
        loadPackageTypes()
        loadDiscounts()
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

    void loadProductTypes(){

        List<ProductTypeVO> list = productTypeRepo.findAll()
        if(list.size() > 0){
            return // return early to not pollute database with duplicates
        }


        ProductTypeVO productTypeVO = new ProductTypeVO();
     //   productTypeVO.producttypeid = 2
        productTypeVO.name = "INDOOR.UNIT"
        productTypeVO.description = "Indoor Unit"

        productTypeVO.setCreateTimeStamp(LocalDateTime.now());
        productTypeVO.setUpdateTimeStamp(LocalDateTime.now());

        productTypeRepo.save(productTypeVO)


        ProductTypeVO productTypeVO2 = new ProductTypeVO();
     //   productTypeVO2.producttypeid = 3
        productTypeVO2.name = "CART.2G.DISPO"
        productTypeVO2.description = "2 gram disposable"

        productTypeVO2.setCreateTimeStamp(LocalDateTime.now());
        productTypeVO2.setUpdateTimeStamp(LocalDateTime.now());

        productTypeRepo.save(productTypeVO2)


        ProductTypeVO productTypeVO3 = new ProductTypeVO();
      //  productTypeVO3.producttypeid = 4
        productTypeVO3.name = "CART.0.5G.ROSIN"
        productTypeVO3.description = ".5 g rosin cart"

        productTypeVO3.setCreateTimeStamp(LocalDateTime.now());
        productTypeVO3.setUpdateTimeStamp(LocalDateTime.now());

        productTypeRepo.save(productTypeVO3)


        ProductTypeVO productTypeVO4 = new ProductTypeVO();
       // productTypeVO4.producttypeid = 5
        productTypeVO4.name = "CART.1G.RESIN"
        productTypeVO4.description = "1G RESIN CART DISPO"

        productTypeVO4.setCreateTimeStamp(LocalDateTime.now());
        productTypeVO4.setUpdateTimeStamp(LocalDateTime.now());

        productTypeRepo.save(productTypeVO4)


        ProductTypeVO productTypeVO5 = new ProductTypeVO();
       // productTypeVO5.producttypeid = 6
        productTypeVO5.name = "BOOMER.UNIT"
        productTypeVO5.description = "UNIT OF BOOMS"

        productTypeVO5.setCreateTimeStamp(LocalDateTime.now());
        productTypeVO5.setUpdateTimeStamp(LocalDateTime.now());

        productTypeRepo.save(productTypeVO5)

        System.out.println("ProductType ref data loaded")
    }

    void loadCustomers(){

        List<CustomerVO> list = customerRepo.findAll()
        if(list.size() > 0){
            return // return early to not pollute database with duplicates
        }

        CustomerVO customerVO = new CustomerVO();
        customerVO.name = "John Doe"
        customerVO.email = "mrchihuahua@techvvs.io"
        customerVO.phone = "6127673388"
        customerVO.address = "6969 420 ave"
        customerVO.address2 = "apartment 9"
        customerVO.notes = "likes to touch your butt"
        customerVO.setCreateTimeStamp(LocalDateTime.now());
        customerVO.setUpdateTimeStamp(LocalDateTime.now());
        customerRepo.save(customerVO)

        CustomerVO customerVO2 = new CustomerVO();
        customerVO2.name = "Dildino Baggins"
        customerVO2.email = "bagboi@techvvs.io"
        customerVO2.phone = "7872228888"
        customerVO2.address = "69 425 ave"
        customerVO2.address2 = "apartment 8"
        customerVO2.notes = "long hair doesnt care"
        customerVO2.setCreateTimeStamp(LocalDateTime.now());
        customerVO2.setUpdateTimeStamp(LocalDateTime.now());
        customerRepo.save(customerVO2)

        System.out.println("Customer ref data loaded")
    }


    void loadDiscounts() {

        List<DiscountVO> discountVOS = discountRepo.findAll()
        if(discountVOS.size() > 0){
            return // return early to not pollute database with duplicates
        }
        for (int i = 1; i <= 100; i++) {
            DiscountVO discountVO = new DiscountVO();
            discountVO.name = i + "% off";
            discountVO.description = i + "% discount";
            discountVO.discountamount = 0.00; // Keep discount amount constant for percentage discounts
            discountVO.discountpercentage = i;

            discountVO.setCreateTimeStamp(LocalDateTime.now());
            discountVO.setUpdateTimeStamp(LocalDateTime.now());
            discountRepo.save(discountVO);
        }

        System.out.println("Percentage discount options loaded");

        for (int i = 1; i <= 10; i++) {
            DiscountVO discountVO = new DiscountVO();
            discountVO.name = "${i * 100} off";
            discountVO.description = "${i * 100} dollar discount";
            discountVO.discountamount = i * 100.00;
            discountVO.discountpercentage = 0;

            discountVO.setCreateTimeStamp(LocalDateTime.now());
            discountVO.setUpdateTimeStamp(LocalDateTime.now());
            discountRepo.save(discountVO);
        }

        System.out.println("Discount ref data loaded");

    }





}
