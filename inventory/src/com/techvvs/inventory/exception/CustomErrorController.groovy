package com.techvvs.inventory.exception

import com.techvvs.inventory.service.auth.TechvvsAuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

import javax.servlet.http.HttpServletRequest;


@Controller
public class CustomErrorController implements ErrorController {

    @Autowired
    TechvvsAuthService techvvsAuthService

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

        // catch the stupid bs error when we upload an xlsx file
        if (throwable != null && throwable.getMessage() != null) {
            if (throwable.getMessage().contains("xlsxbatch") && throwable.getMessage().contains("TemplateInputException")) {
                techvvsAuthService.checkuserauth(model) // we have to re-inject auth
                model.addAttribute("successMessage", "New Batch has been loaded into the System! Navigate Home->Browse Batches to view it");
            } else {
                // Add error details to the model
                model.addAttribute("statusCode", statusCode);
                model.addAttribute("errorMessage", throwable != null ? throwable.getMessage() : "Unknown error");
            }
        }


        // Return the name of a custom error template
        return "error/genericerror.html"; // Replace with your actual template path
    }


}
