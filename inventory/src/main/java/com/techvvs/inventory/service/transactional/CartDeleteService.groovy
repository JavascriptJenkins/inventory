package com.techvvs.inventory.service.transactional

import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.service.controllers.CartService
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

    @Autowired
    CustomerRepo customerRepo

    @Autowired
    CartService cartService

    @Autowired
    BarcodeHelper barcodeHelper

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

        // validate the barcode and add the last digit here
        int checksum =barcodeHelper.calculateUPCAChecksum(cartVO.barcode)

        String barcode = cartVO.barcode + String.valueOf(checksum)

        Optional<ProductVO> productVO = productRepo.findByBarcode(barcode)

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

            cartVO = refreshProductCartList(cartVO)

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



    // because of how we handle quantities on the frontend, this is needed to refresh the list before saving
    @Transactional
    CartVO refreshProductCartList(CartVO cartVO){

        if(cartVO.cartid == 0){
            return cartVO
        }

         cartVO.product_cart_list = cartRepo.findById(cartVO.cartid).get().product_cart_list
        return cartVO

    }


    @Transactional
    CartVO validateCartVO(CartVO cartVO, Model model){
        if(cartVO?.customer?.customerid == null){
            model.addAttribute("errorMessage","Please select a customer")
        }
        if(cartVO?.barcode == null || cartVO?.barcode?.empty){
            model.addAttribute("errorMessage","Please enter a barcode")
        } else {
            // only run this database check if barcode is not null
            Optional<ProductVO> productVO = doesProductExist(cartVO.barcode)
            if(productVO.empty){
                model.addAttribute("errorMessage","Product does not exist")
            } else {
                int cartcount = getCountOfProductInCartByBarcode(cartVO)
                // check here if the quantity we are trying to add will exceed the quantity in stock
                if(cartcount == productVO.get().quantity){
                    model.addAttribute("errorMessage","Quantity exceeds quantity in stock")
                }
            }

        }

        return cartVO
    }


    @Transactional
    int getCountOfProductInCartByBarcode(CartVO cartVO){
        cartVO = refreshProductCartList(cartVO)
        int count = 0
        for(ProductVO productincart : cartVO.product_cart_list){
            if(productincart.barcode.equals(cartVO.barcode)){
                count = count + 1
            }
        }
        return count
    }


    @Transactional
    Optional<ProductVO> doesProductExist(String barcode){
        int checksum =barcodeHelper.calculateUPCAChecksum(barcode)
        barcode = barcode + String.valueOf(checksum)


        Optional<ProductVO> existingproduct = productRepo.findByBarcode(barcode)
        return existingproduct
    }


    @Transactional
    CartVO saveCartIfNew(CartVO cartVO){

        // need to check to make sure there isn't an existing transaction with the same customer and no objects
        String barcode = cartVO.barcode

        if((cartVO.cartid == 0 || cartVO.cartid == null
                && cartVO == null || cartVO?.product_cart_list?.size() == 0)
//        &&
//                !doesTransactionExist(transactionVO.customervo, barcode) // dont think we need to do this
                &&
                !cartService.doesCartExist(cartVO)
        ){

            CustomerVO customerVO = customerRepo.findById(cartVO.customer.customerid).get()

            cartVO.updateTimeStamp = LocalDateTime.now()
            cartVO.createTimeStamp = LocalDateTime.now()
            cartVO.isprocessed = 0
            cartVO.setCustomer(customerVO)

            cartVO = cartRepo.save(cartVO)
            cartVO.barcode = barcode // need to re-bind this so that on first save it will not be null
        }

        // todo: handle case where a cart does exist

        return cartVO
    }



}
