package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.jparepo.DiscountRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.DiscountVO
import com.techvvs.inventory.util.FormattingUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.ui.Model

import javax.transaction.Transactional

@Service
class DiscountService {


    @Autowired
    DiscountRepo discountRepo

    @Autowired
    FormattingUtil formattingUtil



    void bindAllDiscounts(Model model){
        model.addAttribute("discounts", discountRepo.findAll())
    }

    DiscountVO getDiscountById(int id) {
        return discountRepo.findById(id).get()
    }

    CartVO applyDiscountToCart(CartVO cartVO) {

        // the rules here are we can only have one discount at a time
        if(cartVO.discount.discountpercentage != null && cartVO.discount.discountpercentage > 0){
            cartVO.total = formattingUtil.calculateTotalWithDiscountPercentage(cartVO.total, cartVO.discount.discountpercentage)
            cartVO.total < 0 ? 0 : cartVO.total // make sure it doesnt go below 0 for some reason...
        }

        if(cartVO.discount.discountamount != null && cartVO.discount.discountamount > 0){
            cartVO.total = formattingUtil.calculateTotalWithDiscountAmount(cartVO.total, cartVO.discount.discountamount)
            cartVO.total < 0 ? 0 : cartVO.total // make sure it doesnt go below 0 for some reason...
        }

        return cartVO
    }


}
