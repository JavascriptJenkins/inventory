package com.techvvs.inventory.service.transactional

import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.ProductVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.ui.Model

import javax.transaction.Transactional
import java.time.LocalDateTime

@Service
class CartDeleteService {

    @Autowired
    CartRepo cartRepo

    @Autowired
    ProductRepo productRepo

    @Transactional
    CartVO deleteProductFromCart(CartVO cartVO, String barcode){

        CartVO carttoremove = new CartVO(cartid: 0)
        // we are only removing one product at a time
        for(ProductVO productVO : cartVO.product_cart_list){
            if(productVO.barcode == barcode){
                cartVO.product_cart_list.remove(productVO)
                cartVO.total = cartVO.total - productVO.price // subtract the price from the cart total

                productVO.quantityremaining = productVO.quantityremaining + 1
                // remove the cart association from the product
                for(CartVO existingCart : productVO.cart_list){
                    if(existingCart.cartid == cartVO.cartid){
                        carttoremove = existingCart
                    }
                }
                productVO.cart_list.remove(carttoremove)

                productVO.updateTimeStamp = LocalDateTime.now()
                productRepo.save(productVO)
                break
            }
        }

        cartVO.updateTimeStamp = LocalDateTime.now()
        cartVO = cartRepo.save(cartVO)


        return cartVO

    }



    // add product to cart and then update the cart and product associations
    @Transactional
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
            // when product is added to the cart, decrease the quantity remaining.
            productVO.get().quantityremaining = productVO.get().quantityremaining == 0 ? 0 : productVO.get().quantityremaining - 1
            ProductVO savedProduct = productRepo.save(productVO.get())

            if(cartVO.total == null){
                cartVO.total = 0.00
            }

            /* Cart code below */
            cartVO.total += Double.valueOf(productVO.get().price) // add the product price to the total

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





}
