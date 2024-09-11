package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.jparepo.PackageRepo
import com.techvvs.inventory.jparepo.PackageTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.PackageVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.Model

import javax.transaction.Transactional
import java.time.LocalDateTime

@Component
class ProductService {

    @Autowired
    ProductRepo productRepo

    @Autowired
    PackageRepo packageRepo

    @Autowired
    PackageTypeRepo packageTypeRepo

    void saveProductAssociations(TransactionVO transactionVO) {


        for (ProductVO productVO : transactionVO.product_list) {

            // save the product associations
            //Optional<ProductVO> productVO = productRepo.findByBarcode(cartVO.barcode)


            if (!productVO) {

                // update the product cart list association
                if (productVO.transaction_list == null) {
                    productVO.transaction_list = new ArrayList<>()
                }
                productVO.transaction_list.add(transactionVO)

                productVO.updateTimeStamp = LocalDateTime.now()
                ProductVO savedProduct = productRepo.save(productVO)

            }


        }


    }

    ProductVO saveProduct(ProductVO productVO){
        return productRepo.save(productVO)
    }

    ProductVO findProductByID(ProductVO productVO){
        return productRepo.findById(productVO.product_id).get()
    }

    List<ProductVO> getAllProducts(){
        List<ProductVO> products = productRepo.findAll()
        return products
    }


    void saveProductPackageAssociations(String barcode, PackageVO packageVO, Model model, int counter) {
        Optional<ProductVO> productVO = productRepo.findByBarcode(barcode)

        // todo: on second time thru we need to fully hydrate the customer and product_set before saving

        if(!productVO.empty){

            // update the product package list association
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
            model.addAttribute("successMessage","Package: "+productVO.get().name + " added successfully. Quantity: "+counter)
        } else {
            // need to bind the selected customer here otherwise the dropdown wont work
            packageVO.packagetype = packageTypeRepo.findById(packageVO.packagetype.packagetypeid).get()
            model.addAttribute("errorMessage","Product not found")
        }


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
