package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.service.auth.TechvvsAuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@RequestMapping("/admin")
@Controller
public class AdminViewController {


    @Autowired
    TechvvsAuthService techvvsAuthService

    //default home mapping
    @GetMapping
    String viewNewForm(
            Model model
    ){


        techvvsAuthService.checkuserauth(model)
        return "admin/admin.html";
    }





}
