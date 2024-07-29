package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.ProductVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.Model

import java.time.LocalDateTime

@Component
class CartService {

    @Autowired
    ProductRepo productRepo

    @Autowired
    CartRepo cartRepo

    @Autowired
    CustomerRepo customerRepo



    // add product to cart and then update the cart and product associations
    CartVO searchForProductByBarcode(CartVO cartVO, Model model, Optional<Integer> page, Optional<Integer> size

    ){


        Optional<ProductVO> productVO = productRepo.findByBarcode(cartVO.barcode)

        // todo: on second time thru we need to fully hydrate the customer and product_set before saving

        if(!productVO.empty){

            // update the product cart list association
            if(productVO.get().cart_list == null){
                productVO.get().cart_list = new ArrayList<>()
            }
            productVO.get().cart_list.add(cartVO)

            productVO.get().updateTimeStamp = LocalDateTime.now()
            ProductVO savedProduct = productRepo.save(productVO.get())

            if(cartVO.total == null){
                cartVO.total = 0
            }

            /* Cart code below */
            cartVO.total += Integer.valueOf(productVO.get().price) // add the product price to the total

            // handle quantity here (have to iterate thru all product cert list and update the quantity)

            // if it's the first time adding a product we need to create the set to hold them
            if(cartVO.product_cart_list == null){
                cartVO.product_cart_list = new ArrayList<ProductVO>()
            }
            // now save the cart side of the many to many
            cartVO.product_cart_list.add(savedProduct)
            cartVO.updateTimeStamp = LocalDateTime.now()
            cartVO = cartRepo.save(cartVO)
            model.addAttribute("successMessage","Product: "+productVO.get().name + " added successfully")
        } else {
            // need to bind the selected customer here otherwise the dropdown wont work
            cartVO.customer = customerRepo.findById(cartVO.customer.customerid).get()
            model.addAttribute("errorMessage","Product not found")
        }



        return cartVO
    }


    boolean doesCartExist(CartVO cartVO){
        if(cartVO == null || cartVO.cartid == null){
            return false
        }
        Optional<CartVO> existingcart = cartRepo.findById(cartVO.cartid)
        return !existingcart.empty
    }

    CartVO getExistingCart(CartVO cartVO){
//        if(cartVO == null || cartVO.cartid == null){
//            return false
//        }
        Optional<CartVO> existingcart = cartRepo.findById(cartVO.cartid)
        return existingcart.get()
    }




}
