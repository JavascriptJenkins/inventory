package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.viewcontroller.helper.ProductHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam


@RequestMapping("/landing")
@Controller
public class PublicViewController {

    @Autowired
    ProductHelper productHelper

    @Autowired
    TechvvsAuthService techvvsAuthService


    // todo: add rate limiting and also more validation on incoming data
    // http://localhost:8080/qr/publicinfo?productid=12
    //default home mapping
    @GetMapping
    String viewNewFormPublic(
            Model model
    ){


        return "public/landing.html";
    }








}
