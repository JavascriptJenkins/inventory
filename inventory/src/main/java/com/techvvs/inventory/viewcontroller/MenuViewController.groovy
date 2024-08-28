package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.transactional.CartDeleteService
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import com.techvvs.inventory.viewcontroller.helper.MenuHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*


@RequestMapping("/menu")
@Controller
public class MenuViewController {
    

    @Autowired
    CheckoutHelper checkoutHelper

    @Autowired
    CartDeleteService cartDeleteService

    @Autowired
    MenuHelper menuHelper
    
    @Autowired
    TechvvsAuthService techvvsAuthService
    

    //default home mapping
    @GetMapping
    String viewNewForm(
            @ModelAttribute( "menu" ) MenuVO menuVO,
            Model model,
            
            @RequestParam("menuid") String menuid,
            @RequestParam("cartid") String cartid,
            @ModelAttribute( "cart" ) CartVO cartVO
    ){



        menuVO = menuHelper.loadMenu(menuid, model)
        model = checkoutHelper.loadCart(cartid, model, cartVO, menuid)



        // todo: add a button on the ui to pull the latest transaction for customer (so if someone clicks off page
        //  you can come back and finish the transaction)

        



        // fetch all customers from database and bind them to model
        checkoutHelper.getAllCustomers(model)
        techvvsAuthService.checkuserauth(model)
        return "menu/menu.html";
    }


    // todo: make this so that the quantity is taking off the display amount instead of relying on the product cart list
    @PostMapping("/scan")
    String scan(@ModelAttribute( "cart" ) CartVO cartVO,
                Model model,
                
                @RequestParam("page") Optional<Integer> page,
                @RequestParam("size") Optional<Integer> size
    ){

        

        cartVO = menuHelper.validateMenuPageCartVO(cartVO, model)

        String menuid = cartVO.menuid

        // only proceed if there is no error
        if(model.getAttribute("errorMessage") == null){
            // save a new transaction object in database if we don't have one

            cartVO = cartDeleteService.saveCartIfNew(cartVO)

            if(cartVO.barcode != null && cartVO.barcode != ""){
                    // cartVO = cartService.searchForProductByBarcode(cartVO, model, page, size)
                cartVO = cartDeleteService.searchForProductByBarcode(cartVO, model, page, size)
            }



        }

        cartVO = checkoutHelper.hydrateTransientQuantitiesForDisplay(cartVO)

        cartVO.barcode = "" // reset barcode to empty
        cartVO.menuid = menuid

        techvvsAuthService.checkuserauth(model)
        model.addAttribute("cart", cartVO);
        // fetch all customers from database and bind them to model
        checkoutHelper.getAllCustomers(model)

        return "menu/menu.html";
    }


    


}
