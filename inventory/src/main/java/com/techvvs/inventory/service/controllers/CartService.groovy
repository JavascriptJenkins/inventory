package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.DiscountVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.util.FormattingUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.Model

import javax.transaction.Transactional
import java.time.LocalDateTime

@Component
class CartService {

    @Autowired
    ProductRepo productRepo

    @Autowired
    CartRepo cartRepo

    @Autowired
    CustomerRepo customerRepo

    @Autowired
    DiscountService discountService

    @Autowired
    FormattingUtil formattingUtil


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

    @Transactional
    CartVO applyDiscount(CartVO cartVO){
        // get existing cart
        CartVO existingCart = getExistingCart(cartVO)

        // get existing discount in db to apply
        DiscountVO discountVO = discountService.getDiscountById(Integer.valueOf(cartVO.discount.discountid))

        // add the discount to the cart
        existingCart.discount = discountVO

        // now that new discount has been added to the list, calculate the new total based on all discounts
        existingCart = discountService.applyDiscountToCart(existingCart)

        return cartRepo.save(existingCart) // save the discount after its applied so total will reflect in the database
    }

    @Transactional
    CartVO applyAdhocDiscount(CartVO cartVO){
        // get existing cart
        CartVO existingCart = getExistingCart(cartVO)

        // create a discount record in the DB
        DiscountVO discountVO = discountService.createAdhocDiscount(cartVO.discount)

        // add the discount to the cart
        existingCart.discount = discountVO

        // now that new discount has been added to the list, calculate the new total based on all discounts
        existingCart = discountService.applyDiscountToCart(existingCart)

        return cartRepo.save(existingCart) // save the discount after its applied so total will reflect in the database
    }


    @Transactional
    CartVO removeDiscount(CartVO cartVO){
        // get existing cart
        CartVO existingCart = getExistingCart(cartVO)

        // remove the discount
        existingCart.discount = null

        // recalculate the total price based on all products in the product_cart_list
        existingCart.total = calculateTotalPriceOfProductList(existingCart.product_cart_list)

        return cartRepo.save(existingCart) // save the cart with the discount removed and new total price
    }

    double calculateTotalPriceOfProductList(List<ProductVO> productlist){
        double total = 0.0
        for(ProductVO productVO : productlist){
            total+=productVO.price
        }
        return total
    }


}
