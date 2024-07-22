package com.techvvs.inventory.viewcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvvs.inventory.model.CartVO;
import com.techvvs.inventory.model.MenuVO;
import com.techvvs.inventory.viewcontroller.helper.MenuHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RequestMapping("/dashboard")
@Controller
public class HomeViewController {

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    HttpServletResponse httpServletResponse;

    @Autowired
    MenuHelper menuHelper;



    @GetMapping("/index")
    String index(Model model,
                 @ModelAttribute( "menu" ) MenuVO menuVO,
                 @RequestParam("customJwtParameter") String token,
                 @RequestParam("page") Optional<Integer> page,
                 @RequestParam("size") Optional<Integer> size){

        System.out.println("hit the dashboard index page");



        // bind the menu options here
        menuHelper.findMenus(model, page, size);



        model.addAttribute("customJwtParameter",token);
        model.addAttribute("menu",menuVO);
        return "auth/index.html";
    }


   // @PostMapping("/login")

    @PostMapping("/logout")
    String logout(Model model){


        System.out.println("someone is logging out");

        Cookie cookie = new Cookie("Authorization", null); // Not necessary, but saves bandwidth.
        cookie.setPath("/");
        cookie.setMaxAge(0); // Don't set to -1 or it will become a session cookie!
        httpServletResponse.addCookie(cookie);

        return "auth/index.html";
    }



    //  This will add an outgoing repsonse header to everything that hits this controller




}
