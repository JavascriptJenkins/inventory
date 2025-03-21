package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.SystemUserDAO
import com.techvvs.inventory.security.Role
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.transactional.CartDeleteService
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import com.techvvs.inventory.viewcontroller.helper.MenuHelper
import com.techvvs.inventory.viewcontroller.helper.SystemUserHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RequestMapping("/systemuser")
@Controller
public class SystemUserViewController {
    

    @Autowired
    CheckoutHelper checkoutHelper

    @Autowired
    CartDeleteService cartDeleteService

    @Autowired
    MenuHelper menuHelper

    @Autowired
    BatchControllerHelper batchControllerHelper

    @Autowired
    TechvvsAuthService techvvsAuthService

    @Autowired
    SystemUserHelper systemUserHelper
    



    @GetMapping
    String viewNewForm(
            @ModelAttribute( "systemuser" ) SystemUserDAO systemUser,
            Model model,
            @RequestParam("systemuserid") Optional<String> systemuserid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

//        systemUserHelper.addPaginatedData(model, page)

        if(systemuserid.isPresent())  {
            systemUserHelper.loadSystemUser(Integer.valueOf(systemuserid.get()), model)
        }

        systemUserHelper.loadAllSystemUsers(model, page, size)

//        systemUserHelper.loadAllSystemUsers(model)
        return "systemuser/systemuser.html";
    }

    @GetMapping("/myprofile")
    String viewMyProfile(
            @ModelAttribute( "systemuser" ) SystemUserDAO systemUser,
            Model model,
            @RequestParam("systemuserid") Optional<String> systemuserid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

//        systemUserHelper.addPaginatedData(model, page)
//        systemUserHelper.loadAllSystemUsers(model, page, size)

        if(systemuserid.isPresent())  {
            systemUserHelper.loadSystemUser(Integer.valueOf(systemuserid.get()), model)
        }
        systemUserHelper.loadAllSystemUsers(model, page, size)

        return "systemuser/systemuser.html";
    }


    // todo: enforce admin rights to edit user here
    @PostMapping("/edit")
    String editSystemUser(
            @ModelAttribute( "systemuser" ) SystemUserDAO systemUser,
            Model model,
            @RequestParam("systemuserid") Optional<String> systemuserid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        techvvsAuthService.checkuserauth(model)

        systemUserHelper.updateSystemUser(systemUser, model, true)

//        systemUserHelper.addPaginatedData(model, page)

        systemUserHelper.loadSystemUser(Integer.valueOf(systemUser.id), model)

        systemUserHelper.loadAllSystemUsers(model, page, size)


        String token = techvvsAuthService.getActiveCookie(request)
        // update the active jwt
        techvvsAuthService.updateJwtToken(token, response)


        return "systemuser/systemuser.html";
    }


    // todo: enforce admin rights to edit user here
    @PostMapping("/systemuser/role/remove")
    String removeRoleFromSystemUser(
            @ModelAttribute( "systemuser" ) SystemUserDAO systemUser,
            Model model,
            @RequestParam("systemuserid") Optional<String> systemuserid,
            @RequestParam("role") Optional<String> role,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        techvvsAuthService.checkuserauth(model)

        systemUserHelper.updateSystemUser(systemUser, model)

//        systemUserHelper.addPaginatedData(model, page)

        systemUserHelper.loadSystemUser(Integer.valueOf(systemUser.id), model)

//        systemUserHelper.loadAllSystemUsers(model)
        systemUserHelper.loadAllSystemUsers(model, page, size)


        // update the active jwt
        techvvsAuthService.updateJwtToken(techvvsAuthService.getActiveCookie(request), response)


        return "systemuser/systemuser.html";
    }






    


}
