package com.techvvs.inventory.exception
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

import javax.servlet.http.HttpServletRequest;


@Controller
public class CustomErrorController implements ErrorController {


    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

        System.out.println("WE INTERCEPTED THE ERROR")

        // Retrieve error details from the request
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");

        // Log the error (optional)
        if (throwable != null) {
            System.err.println("Error occurred: " + throwable.getMessage());
        }

        // Add error details to the model
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorMessage", throwable != null ? throwable.getMessage() : "Unknown error");

        // Return the name of a custom error template
        return "service/xlsxbatch.html"; // Replace with your actual template path
    }


}
