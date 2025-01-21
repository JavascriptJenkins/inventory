package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.SystemUserRepo
import com.techvvs.inventory.jparepo.TokenRepo
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.TokenDAO
import com.techvvs.inventory.security.JwtTokenProvider
import com.techvvs.inventory.security.Role
import com.techvvs.inventory.viewcontroller.helper.MenuHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.transaction.Transactional
import java.time.LocalDateTime

@Component
class CustomerService {


    @Autowired
    CustomerRepo customerRepo

    @Autowired
    JwtTokenProvider jwtTokenProvider

    @Autowired
    MenuHelper menuHelper

    @Autowired
    TokenRepo tokenRepo


    @Transactional
    CustomerVO saveActiveShoppingToken(CustomerVO customerVO, List<Role> roles, String hours, String menuid, String customerid){

        Optional<TokenDAO> tokenDAO = tokenRepo.findByToken(customerVO.shoppingtoken)

        // this scenario means we are sending a new token to a customer.
        // this could be because we sent them the wrong menu, or because we want to send them a longer token
        if(tokenDAO.present && tokenDAO.get().tokenused == 0 && jwtTokenProvider.validateTokenSimple(tokenDAO.get().token)){
            System.out.println("We already have an active shopping token.  Marking it as invalid and emptying the customer cart. ")
            setTokenToUsedAndEmptyCart(customerVO, roles, hours, menuid, customerid, tokenDAO.get())
        }

        if(tokenDAO.present && !jwtTokenProvider.validateTokenSimple(customerVO.shoppingtoken)){
            System.out.println("We have an expired token.  Marking it as invalid and emptying the customer cart. ")
            setTokenToUsedAndEmptyCart(customerVO, roles, hours, menuid, customerid, tokenDAO.get())
        }


        if(tokenDAO.isEmpty()){


            customerVO = generateTokenAndSaveForCustomer(customerVO, roles, hours, menuid, customerid)

            // This means we are writing a new token for the user for the very first time
            // there will be no records in the Token table for this token so we will have to create one
            TokenDAO newtoken = new TokenDAO(
                    token: customerVO.shoppingtoken, // NOTE: this was assigned when the token was created in the above method "generateTokenAndSaveForCustomer()"
                    tokenused: 0,
                    usermetadata: "customerid: "+customerVO.customerid + " | menuid: "+menuid + " | hours: "+hours,
                    updatedtimestamp: LocalDateTime.now(),
                    createtimestamp: LocalDateTime.now()
            )
            tokenRepo.save(newtoken)

        }



        return customerVO
    }

    CustomerVO generateTokenAndSaveForCustomer(CustomerVO customerVO, List<Role> roles, String hours, String menuid, String customerid){
        // generate a new shopping token
        String token = jwtTokenProvider.createMenuShoppingToken(customerVO.email, roles, Integer.valueOf(hours), menuid, customerid)

        // overwrite/set the current shoppingtoken to the new value
        customerVO.shoppingtoken = token
        return customerRepo.save(customerVO)
    }

    CustomerVO setTokenToUsedAndEmptyCart(CustomerVO customerVO, List<Role> roles, String hours, String menuid, String customerid, TokenDAO tokenDAO){
        // if the user tries to access with a token that has expired, we have to check to see if they had a cart and then empty it
        // then we are going to mark the token as used
        // mark the existing token as used in db
        tokenDAO.tokenused = 1
        tokenRepo.save(tokenDAO)

        // empty the customer's cart and mark it as processed
        menuHelper.emptyWholeCartForCustomer(customerVO, Integer.valueOf(menuid))

        // generate a new shopping token
        customerVO = generateTokenAndSaveForCustomer(customerVO, roles, hours, menuid, customerid)

        return customerVO
    }




}
