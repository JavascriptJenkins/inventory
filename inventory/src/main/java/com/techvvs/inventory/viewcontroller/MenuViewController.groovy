package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.security.JwtTokenProvider
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.transactional.CartDeleteService
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper
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
    BatchControllerHelper batchControllerHelper

    @Autowired
    TechvvsAuthService techvvsAuthService

    @Autowired
    JwtTokenProvider jwtTokenProvider
    

    // todo: modify this to parse user cookie from request and check user permissions
    //default home mapping
    @GetMapping
    String viewNewForm(
            @ModelAttribute( "menu" ) MenuVO menuVO,
            Model model,
            @RequestParam("menuid") Optional<String> menuid,
            @RequestParam("cartid") Optional<String> cartid,
            @RequestParam("page") Optional<String> page,
            @RequestParam("size") Optional<String> size,
            @ModelAttribute( "cart" ) CartVO cartVO
    ){



        if(menuid.isPresent())  {
            menuHelper.loadMenu(menuid.get(), model)
        }
        if(cartid.isPresent()) {
            checkoutHelper.loadCart(cartid.get(), model, cartVO, menuid.get())
        }



        // todo: add a button on the ui to pull the latest transaction for customer (so if someone clicks off page
        //  you can come back and finish the transaction)

        



        // fetch all customers from database and bind them to model
        checkoutHelper.getAllCustomers(model)
        //techvvsAuthService.checkuserauth(model)
        return "menu/menu.html";
    }

    // todo: open this up on the firewall
    // allow user to shop menu with shopping token
    @GetMapping("/shop")
    String shopMenu(
            @ModelAttribute( "menu" ) MenuVO menuVO,
            Model model,
            @RequestParam("menuid") Optional<String> menuid,
            @RequestParam("shoppingtoken") Optional<String> shoppingtoken,
            @RequestParam("size") Optional<String> size,
            @ModelAttribute( "cart" ) CartVO cartVO
    ){
        // this needs to do a search on the customerid and see if there is cart pending with this exact menu id
        if(shoppingtoken.isPresent()) {
            menuHelper.loadCartByCustomerIdAndMenuId(shoppingtoken.get(), model)
        }


        if(menuid.isPresent() && shoppingtoken.isPresent()) {
            menuHelper.loadMenuWithToken(menuid.get(), model, shoppingtoken.get())
            // hydrate hidden values for passing into the post methods like token etc
            menuHelper.bindHiddenValues(model, shoppingtoken.get(), menuid.get())
        }




        // fetch all customers from database and bind them to model
        checkoutHelper.getAllCustomers(model)
        //techvvsAuthService.checkuserauth(model)
        return "menu/menu.html";
    }


    // allow user to shop menu with shopping token
    @PostMapping("/shop/product/cart/add")
    String addProductToCart(
            @ModelAttribute( "menu" ) MenuVO menuVO,
            Model model,
            @RequestParam("menuid") Optional<String> menuid,
            @RequestParam("shoppingtoken") Optional<String> shoppingtoken,
            @RequestParam("cartid") Optional<String> cartid,
            @RequestParam("productid") Optional<String> productid,
            @RequestParam("quantityselected") Optional<String> quantityselected,
            @RequestParam("size") Optional<String> size,
            @ModelAttribute( "cart" ) CartVO cartVO
    ){



        // pass in the cartid and menuid and productid and quantityselected into a method to add to the cart
        if(cartid.isPresent() && menuid.isPresent() && productid.isPresent() && quantityselected.isPresent()
        && menuHelper.checkQuantity(Integer.valueOf(productid.get()), Integer.valueOf(quantityselected.get()), model)
        ) {

            String customerid = jwtTokenProvider.getCustomerIdFromToken(shoppingtoken.get())
            int savedcartid = menuHelper.addProductToCart(Integer.valueOf(cartid.get()), Integer.valueOf(menuid.get()), Integer.valueOf(productid.get()), Integer.valueOf(quantityselected.get()), Integer.valueOf(customerid), model, shoppingtoken.get())
            menuHelper.loadCart(savedcartid, model)
            // hydrate hidden values for passing into the post methods like token etc
            menuHelper.bindHiddenValues(model, shoppingtoken.get(), menuid.get())
        } else {
            loadDataOnError(menuid, shoppingtoken, cartid, model)
        }

        processRestOfStuff(menuid, shoppingtoken, model)


        return "menu/menu.html";
    }

    @PostMapping("/shop/product/cart/subtract")
    String subtractProductFromCart(
            @ModelAttribute( "menu" ) MenuVO menuVO,
            Model model,
            @RequestParam("menuid") Optional<String> menuid,
            @RequestParam("shoppingtoken") Optional<String> shoppingtoken,
            @RequestParam("cartid") Optional<String> cartid,
            @RequestParam("productid") Optional<String> productid,
            @RequestParam("quantityselected") Optional<String> quantityselected,
            @RequestParam("size") Optional<String> size,
            @ModelAttribute( "cart" ) CartVO cartVO
    ){



        // pass in the cartid and menuid and productid and quantityselected into a method to add to the cart
        if(cartid.isPresent() && menuid.isPresent() && productid.isPresent() && quantityselected.isPresent()) {
            String customerid = jwtTokenProvider.getCustomerIdFromToken(shoppingtoken.get())
            int savedcartid = menuHelper.removeProductFromCart(Integer.valueOf(cartid.get()), Integer.valueOf(menuid.get()), Integer.valueOf(productid.get()), Integer.valueOf(quantityselected.get()), Integer.valueOf(customerid), model, shoppingtoken.get())
            menuHelper.loadCart(savedcartid, model)
            // hydrate hidden values for passing into the post methods like token etc
            menuHelper.bindHiddenValues(model, shoppingtoken.get(), menuid.get())
        } else {
                loadDataOnError(menuid, shoppingtoken, cartid, model)
        }

        processRestOfStuff(menuid, shoppingtoken, model)
        return "menu/menu.html";
    }

    @PostMapping("/shop/empty/cart")
    String emptyCart(
            @ModelAttribute( "menu" ) MenuVO menuVO,
            Model model,
            @RequestParam("menuid") Optional<String> menuid,
            @RequestParam("shoppingtoken") Optional<String> shoppingtoken,
            @RequestParam("cartid") Optional<String> cartid,
            @RequestParam("productid") Optional<String> productid,
            @RequestParam("quantityselected") Optional<String> quantityselected,
            @RequestParam("size") Optional<String> size,
            @ModelAttribute( "cart" ) CartVO cartVO
    ){


        // first we need to check if we have all required items
        if(shoppingtoken.present && cartid.present && menuid.present){
            // empty the cart
            int emptiedcartid = menuHelper.emptyWholeCart(Integer.valueOf(cartid.get()), Integer.valueOf(menuid.get()), model, shoppingtoken.get())
            menuHelper.loadCart(emptiedcartid, model)
            model.addAttribute("successMessage", "Cart emptied")
            // hydrate hidden values for passing into the post methods like token etc
            menuHelper.bindHiddenValues(model, shoppingtoken.get(), menuid.get())
        } else {
            loadDataOnError(menuid, shoppingtoken, cartid, model)
        }

        processRestOfStuff(menuid, shoppingtoken, model)
        return "menu/menu.html";
    }

    @PostMapping("/shop/checkout")
    String checkoutCartFromMenu(
            Model model,
            @RequestParam("menuid") Optional<String> menuid,
            @RequestParam("shoppingtoken") Optional<String> shoppingtoken,
            @RequestParam("cartid") Optional<String> cartid,
            @RequestParam("productid") Optional<String> productid,
            @RequestParam("quantityselected") Optional<String> quantityselected,
            @RequestParam("size") Optional<String> size,
            @ModelAttribute( "cart" ) CartVO cartVO
    ){

        // first we need to check if we have all required items
        if(shoppingtoken.present && cartid.present && menuid.present){

            //

        }






        // pass in the cartid and menuid and productid and quantityselected into a method to add to the cart
        if(cartid.isPresent() && menuid.isPresent() && productid.isPresent() && quantityselected.isPresent()) {
            String customerid = jwtTokenProvider.getCustomerIdFromToken(shoppingtoken.get())
            int savedcartid = menuHelper.removeProductFromCart(Integer.valueOf(cartid.get()), Integer.valueOf(menuid.get()), Integer.valueOf(productid.get()), Integer.valueOf(quantityselected.get()), Integer.valueOf(customerid), model, shoppingtoken.get())
            menuHelper.loadCart(savedcartid, model)
            // hydrate hidden values for passing into the post methods like token etc
            menuHelper.bindHiddenValues(model, shoppingtoken.get(), menuid.get())
        }

        processRestOfStuff(menuid, shoppingtoken, model)
        return "menu/menu.html";
    }


    void loadDataOnError(Optional<String> menuid, Optional<String> shoppingtoken, Optional<String> cartid, Model model) {
        menuHelper.loadCart(Integer.valueOf(cartid.get()), model)
        // hydrate hidden values for passing into the post methods like token etc
        menuHelper.bindHiddenValues(model, shoppingtoken.get(), menuid.get())
    }


    void processRestOfStuff(Optional<String> menuid, Optional<String> shoppingtoken, Model model) {
        if(menuid.isPresent() && shoppingtoken.isPresent()) {
            // load the product menu to be displayed back to the user
            menuHelper.loadMenuWithToken(menuid.get(), model, shoppingtoken.get())
        }

        // make sure we have a cart bound even an empty one
        if(null == model.getAttribute("cart")){
            model.addAttribute("cart", new CartVO(cartid:0))
        }

        // fetch all customers from database and bind them to model
        checkoutHelper.getAllCustomers(model)
    }

    // todo: modify this to parse user cookie from request and check user permissions
    // this serves the default menu for the batch
    @GetMapping("/batch")
    String viewBatchMenu(
            @ModelAttribute( "menu" ) MenuVO menuVO,
            Model model,
            @RequestParam("cartid") Optional<String> cartid,
            @RequestParam("batchid") Optional<String> batchid,
            @RequestParam("page") Optional<String> page,
            @RequestParam("size") Optional<String> size,
            @ModelAttribute( "cart" ) CartVO cartVO
    ){


        String menuid = "0"
        if(batchid.isPresent())  {
            BatchVO batchVO = batchControllerHelper.loadBatch(batchid.get(), model)
            if(batchVO.menu_set.size() == 1){
                menuid = String.valueOf(batchVO.menu_set[0].menuid) // grab the default menu id
            } else {
                for(MenuVO menu222 : batchVO.menu_set){
                    if(menu222.isdefault == 1){
                        menuid = String.valueOf(menu222.menuid) // find the default menu and serve it
                        break
                    }
                }
            }
        }

        // todo: modify this to load the cart without a menu id? is that possible or do we require a menuid?
        if(cartid.isPresent()) {
            checkoutHelper.loadCart(cartid.get(), model, cartVO, menuid)
        }

        // load the menu
        menuHelper.loadMenu(menuid, model)

        // fetch all customers from database and bind them to model
        //checkoutHelper.getAllCustomers(model)
        //techvvsAuthService.checkuserauth(model)
        return "menu/menu.html";
    }

    @PostMapping("/pricechange")
    String pricechange(
                Model model,
                @RequestParam("menuid") Optional<String> menuid,
                @RequestParam("cartid") Optional<String> cartid,
                @RequestParam("amount") Optional<String> amount,
                @RequestParam("isnew") Optional<String> isnew,
                @RequestParam("newname") Optional<String> newname,
                @RequestParam("producttype") Optional<String> producttypeid,
                @RequestParam("page") Optional<Integer> page,
                @RequestParam("size") Optional<Integer> size
    ){

        techvvsAuthService.checkuserauth(model)

        MenuVO returnVO = new MenuVO()

        if(menuid.isPresent() && amount.isPresent() &&
                isnew.isPresent() && isnew.get() == "yes" && newname.isPresent()
                && producttypeid.isPresent()
        )  {


            // make a new menu with the new price
            returnVO = menuHelper.createNewMenu(
                    Double.valueOf(amount.get()),
                    Integer.valueOf(menuid.get()),
                    newname.get(),
                    model,
                    Integer.valueOf(producttypeid.get())

            )


        } else if(menuid.isPresent() && amount.isPresent() && producttypeid.isPresent() && isnew.isEmpty()){
            // add a discount tied to an existing menu
            returnVO = menuHelper.changePrice(
                    Double.valueOf(amount.get()),
                    Integer.valueOf(menuid.get()),
                    model,
                    Integer.valueOf(producttypeid.get())
            )

        } else {
            model.addAttribute("errorMessage", "menuid and amount are required")
        }


        // bind the menu options here
        menuHelper.findMenus(model, page, size);


        return "auth/index.html";
    }

    @PostMapping("/shoppingtoken/send")
    String sendShoppingToken(
            Model model,
            @RequestParam("menuid") Optional<String> menuid,
            @RequestParam("customerid") Optional<String> customerid,
            @RequestParam("phonenumber") Optional<String> phonenumber,
            @RequestParam("tokenlength") Optional<String> tokenlength,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ){

        techvvsAuthService.checkuserauth(model)

        // if all values present, send token
        if(menuid.isPresent() && customerid.isPresent() && phonenumber.isPresent() && tokenlength.isPresent())  {
            menuHelper.sendShoppingToken(menuid.get(), customerid.get(), phonenumber.get(), tokenlength.get(), model)
        } else {
            model.addAttribute("errorMessage", "menuid, customerid, phonenumber, and tokenlength are required")
        }

        // bind the menu options here
        menuHelper.findMenus(model, page, size)

        checkoutHelper.getAllCustomers(model);

        return "auth/index.html";
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
