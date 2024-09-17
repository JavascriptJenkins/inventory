//package com.techvvs.inventory.errorcontroller
//
//import org.springframework.http.HttpStatus
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseStatus;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//
//@Controller
//public class CustomErrorController {
//
//    @GetMapping("/403")
//    public String accessDenied() {
//        return "auth/error.html"; // Return the name of your 403 error page
//    }
//}
//
//
