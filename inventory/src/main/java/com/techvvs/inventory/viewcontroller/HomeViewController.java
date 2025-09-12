package com.techvvs.inventory.viewcontroller;

import com.techvvs.inventory.model.MenuVO;
import com.techvvs.inventory.model.ProductTypeVO;
import com.techvvs.inventory.model.ProductVO;
import com.techvvs.inventory.security.CookieUtils;
import com.techvvs.inventory.security.SameSiteCookieResponseWrapper;
import com.techvvs.inventory.service.auth.TechvvsAuthService;
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper;
import com.techvvs.inventory.viewcontroller.helper.MenuHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RequestMapping("/dashboard")
@Controller
public class HomeViewController {

    @Autowired
    HttpServletResponse httpServletResponse;

    @Autowired
    MenuHelper menuHelper;

    @Autowired
    TechvvsAuthService techvvsAuthService;

    @Autowired
    CheckoutHelper checkoutHelper;

    @Autowired
    CookieUtils cookieUtils;



    @GetMapping("/index")
    String index(Model model,
                 @ModelAttribute( "menu" ) MenuVO menuVO,
                 @RequestParam("page") Optional<Integer> page,
                 @RequestParam("size") Optional<Integer> size){

        System.out.println("hit the dashboard index page");


        // todo: pull this from the logged in systemuser?  or pull it from a new table UICONFIG
        model.addAttribute("UIMODE", "RETRO");

        // bind the menu options here
        menuHelper.findMenus(model, page, size);
//
//        Set<ProductTypeVO> uniqueProductTypes = new HashSet<>();
//
//        for (ProductVO productVO : menuVO.getMenu_product_list()) {
//            uniqueProductTypes.add(productVO.getProducttypeid());
//        }
//        model.addAttribute("uniqueProductTypes", uniqueProductTypes); // Pass unique types to the template

        checkoutHelper.getAllCustomers(model);

        techvvsAuthService.checkuserauth(model);
        model.addAttribute("menu",menuVO);
        return "auth/index.html";
    }

    @PostMapping("/index")
    String indexPost(Model model,
                     @ModelAttribute( "menu" ) MenuVO menuVO,
                     @RequestParam("page") Optional<Integer> page,
                     @RequestParam("size") Optional<Integer> size,
                     @RequestParam(value = "oauth_success", required = false) String oauthSuccess){

        System.out.println("hit the dashboard index page via POST (OAuth redirect)");

        // todo: pull this from the logged in systemuser?  or pull it from a new table UICONFIG
        model.addAttribute("UIMODE", "RETRO");

        // bind the menu options here
        menuHelper.findMenus(model, page, size);

        checkoutHelper.getAllCustomers(model);

        techvvsAuthService.checkuserauth(model);
        model.addAttribute("menu",menuVO);
        
        // Add success message if this is from OAuth
        if ("true".equals(oauthSuccess)) {
            model.addAttribute("successMessage", "Successfully logged in with Google OAuth!");
        }
        
        return "auth/index.html";
    }


   // @PostMapping("/login")

    @PostMapping("/logout")
    String logout(Model model,HttpServletResponse response){


        System.out.println("someone is logging out");

        // Wrap response to add SameSite attribute automatically
        SameSiteCookieResponseWrapper wrappedResponse = cookieUtils.wrapResponse(response);
        
        // Remove the JWT cookie using utility
        Cookie cookie = cookieUtils.createLogoutCookie();
        wrappedResponse.addCookie(cookie);

        // Clear the security context
        SecurityContextHolder.clearContext();


        return "auth/index.html";
    }



    //  This will add an outgoing repsonse header to everything that hits this controller




}
