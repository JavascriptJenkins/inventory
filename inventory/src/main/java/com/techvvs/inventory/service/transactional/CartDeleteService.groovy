package com.techvvs.inventory.service.transactional

import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.service.controllers.CartService
import com.techvvs.inventory.service.controllers.ProductService
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

    @Autowired
    ProductService productService

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

        if(cartVO.quantityselected == 0){
            productService.saveProductCartAssociations(barcode, cartVO, model, 1)
        } else {
            int j = 0;
            // run the product save once for every quantity selected
            for (int i = 0; i < cartVO.quantityselected; i++) {
                j++
                productService.saveProductCartAssociations(barcode, cartVO, model, j)
            }
        }


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
                // here we have to rebind the cart
                if(cartVO.cartid == null || cartVO.cartid == 0){
                    // do nothing
                } else {
                    cartVO = cartRepo.findById(cartVO.cartid).get()
                    model.addAttribute("cart", cartVO);
                }
            } else {

                int cartcount = getCountOfProductInCartByBarcode(cartVO) + cartVO.quantityselected
                // check here if the quantity we are trying to add will exceed the quantity in stock
                if(cartcount > productVO.get().quantity){
                    model.addAttribute("errorMessage","Quantity exceeds quantity in stock")
                }

            }

        }

        return cartVO
    }


    @Transactional
    int getCountOfProductInCartByBarcode(CartVO cartVO){
        cartVO = productService.refreshProductCartList(cartVO)
        int count = 0

        String nochecksum = cartVO.barcode
        // validate the barcode and add the last digit here
        int checksum =barcodeHelper.calculateUPCAChecksum(cartVO.barcode)
        cartVO.barcode = String.valueOf(cartVO.barcode+checksum)

        for(ProductVO productincart : cartVO.product_cart_list){
            if(productincart.barcode.equals(cartVO.barcode)){
                count = count + 1
            }
        }

        // reset incoming barcode to not have checksum
        cartVO.barcode = nochecksum
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
        int quantityselected = cartVO.quantityselected

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
            cartVO.quantityselected = quantityselected // re-bind this after save
        }

        // todo: handle case where a cart does exist

        return cartVO
    }



}
