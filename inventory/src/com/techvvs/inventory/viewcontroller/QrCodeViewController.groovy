package com.techvvs.inventory.viewcontroller
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.viewcontroller.helper.ProductHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*


@RequestMapping("/qr")
@Controller
public class QrCodeViewController {

    @Autowired
    ProductHelper productHelper

    @Autowired
    TechvvsAuthService techvvsAuthService


    // todo: add rate limiting and also more validation on incoming data
    // http://localhost:8080/qr/publicinfo?productid=12
    //default home mapping
    @GetMapping("/publicinfo")
    String viewNewForm(
            Model model,
            @RequestParam("productid") String productid
    ){

        productid = productid == null ? "0" : String.valueOf(productid)

        // attach the paymentVO to the model
        ProductVO productVO = productHelper.loadProduct(Integer.valueOf(productid))
        model.addAttribute("product", productVO)
        return "product/publicinfo.html";
    }


    @GetMapping
    String viewQrInfo(
            Model model,
            TransactionVO transactionVO,
            Optional<String> productkey
    ){
        // productkey controls which product test results and info to show
        productkey.present ? model.addAttribute("productkey", productkey.get()) : model.addAttribute("productkey", "")

        model.addAttribute("transaction", new TransactionVO())
        return "public/qrinfo.html";
    }







}
