package com.techvvs.inventory.service.transactional

import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.ProductVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

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





}
