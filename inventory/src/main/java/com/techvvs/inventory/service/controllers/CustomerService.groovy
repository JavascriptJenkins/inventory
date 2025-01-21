package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.SystemUserRepo
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.security.JwtTokenProvider
import com.techvvs.inventory.security.Role
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CustomerService {


    @Autowired
    CustomerRepo customerRepo

    @Autowired
    JwtTokenProvider jwtTokenProvider


    CustomerVO saveActiveShoppingToken(CustomerVO customerVO, List<Role> roles, String hours, String menuid, String customerid){

        String token = ""

        if(customerVO.shoppingtoken != null && jwtTokenProvider.validateTokenSimple(customerVO.shoppingtoken)){
            System.out.println("We already have an active shopping token")
        } else {

            // generate a new shopping token
            token = jwtTokenProvider.createMenuShoppingToken(customerVO.email, roles, Integer.valueOf(hours), menuid, customerid)

            // overwrite/set the current shoppingtoken to the new value
            customerVO.shoppingtoken = token
            return customerRepo.save(customerVO)
        }


        return customerVO
    }




}
