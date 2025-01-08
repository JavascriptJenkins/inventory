package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.barcode.service.BarcodeService
import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.DiscountRepo
import com.techvvs.inventory.jparepo.MenuRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.jparepo.SystemUserRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.DiscountVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.SystemUserDAO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.security.JwtTokenProvider
import com.techvvs.inventory.security.Role
import com.techvvs.inventory.service.controllers.ProductService
import com.techvvs.inventory.service.controllers.TransactionService
import com.techvvs.inventory.service.transactional.CartDeleteService
import com.techvvs.inventory.util.TwilioTextUtil
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.parameters.P
import org.springframework.stereotype.Component
import org.springframework.ui.Model

import javax.persistence.EntityNotFoundException
import javax.transaction.Transactional
import java.time.LocalDateTime

@Component
class MenuHelper {

    @Autowired
    MenuRepo menuRepo

    @Autowired
    ProductTypeRepo productTypeRepo

    @Autowired
    CartDeleteService cartDeleteService

    @Autowired
    Environment environment

    @Autowired
    AppConstants appConstants

    @Autowired
    BarcodeService barcodeService

    @Autowired
    DiscountRepo discountRepo

    @Autowired
    JwtTokenProvider jwtTokenProvider

    @Autowired
    CustomerRepo customerRepo

    @Autowired
    TwilioTextUtil twilioTextUtil

    @Autowired
    CheckoutHelper checkoutHelper

    @Autowired
    ProductService productService

    @Autowired
    CartRepo cartRepo

    @Autowired
    ProductRepo productRepo

    @Autowired
    TransactionService transactionService

    @Autowired
    SystemUserRepo systemUserRepo

    @Transactional
    MenuVO changePrice(
            double newpriceadjustment,
            int existingmenuid,
            Model model,
            int producttypeid
    ) {
        try {
            MenuVO existingmenu = menuRepo.findById(existingmenuid).orElseThrow({
                new IllegalArgumentException("Menu not found")
            })

            boolean isamountvalid = checkIsAmountValid(newpriceadjustment, producttypeid, existingmenu.menu_product_list, model);

            if (!isamountvalid) {
                return new MenuVO(menuid: 0); // return early if discount will set the price all the way to 0.
            }

            ProductTypeVO productTypeVO = productTypeRepo.findById(producttypeid).orElseThrow({
                new IllegalArgumentException("Product type not found")
            })

            DiscountVO updatedDiscountVO = null;

            // Check for existing discount
            for (DiscountVO discountVO : existingmenu.getDiscount_list()) {
                if (discountVO.getProducttype().getProducttypeid() == productTypeVO.getProducttypeid()) {
                    discountVO.setUpdateTimeStamp(LocalDateTime.now());
                    discountVO.setDiscountamount(newpriceadjustment);
                    updatedDiscountVO = discountRepo.save(discountVO);
                    break;
                }
            }

            // If no existing discount, create a new one
            if (updatedDiscountVO == null) {
                DiscountVO newDiscountVO = new DiscountVO();
                newDiscountVO.setDiscountamount(newpriceadjustment);
                newDiscountVO.setName(productTypeVO.getName());
                newDiscountVO.setDescription("Menu Price Adjustment");
                newDiscountVO.setProducttype(productTypeVO);
                newDiscountVO.setMenu(existingmenu);
                newDiscountVO.setIsactive(1);
                newDiscountVO.setUpdateTimeStamp(LocalDateTime.now());
                newDiscountVO.setCreateTimeStamp(LocalDateTime.now());

                updatedDiscountVO = discountRepo.save(newDiscountVO);

                // Synchronize both sides of the relationship
                existingmenu.getDiscount_list().add(updatedDiscountVO);
            }

            existingmenu.setUpdateTimeStamp(LocalDateTime.now());
            existingmenu = menuRepo.save(existingmenu); // Save the updated menu

            model.addAttribute("successMessage", "Success: Updated menu: " + existingmenu.getName() + " | with price adjustment: " + newpriceadjustment + " | product type: " + productTypeVO.getName());

            return existingmenu;

        } catch (Exception e) {
            System.out.println("Caught Exception: " + e.getMessage());
            model.addAttribute("errorMessage", "Error: Problem creating new menu in changePrice.");
            return new MenuVO(menuid: 0);
        }
    }


    @Transactional
    MenuVO createNewMenu(
            double newpriceadjustment,
            int existingmenuid,
            String name,
            Model model,
            int producttypeid // create the initial menu with a discount on this product type

    ){


        try{
            MenuVO existingmenu = menuRepo.findById(existingmenuid).get()


            boolean isamountvalid = checkIsAmountValid(newpriceadjustment, producttypeid, existingmenu.menu_product_list, model)

            if(!isamountvalid){
                return new MenuVO(menuid: 0) // return early if discount will set the price all the way to 0.
            }

            // Create a new list by detaching the 'menu' ownership temporarily
            List<ProductVO> expandedlist = new ArrayList<>()
            Iterator<ProductVO> iterator = existingmenu.menu_product_list.iterator()

            while (iterator.hasNext()) {
                ProductVO product = iterator.next()
                product.menu_list = null // Temporarily detach from the old menu list
                expandedlist.add(product)
            }

            // Create the new MenuVO and assign the expanded list
            MenuVO newmenu = new MenuVO(
                    name: name.replaceAll(" ", "_"),
                    menu_product_list: expandedlist,
                    isdefault: 0,
                    discount_list: null,
                    notes: "new menu created with price adjustment: " + newpriceadjustment,
                    createTimeStamp: LocalDateTime.now(),
                    updateTimeStamp: LocalDateTime.now()
            )

            // Save the new menu
            newmenu = menuRepo.save(newmenu)
            if(newmenu.discount_list == null){
                newmenu.discount_list = new ArrayList<>()
            }


            /* Now, make a new discount entry in the discount table that is tied to the menu that was just created */
            ProductTypeVO productTypeVO = productTypeRepo.findById(producttypeid).get()

            DiscountVO discountVO = new DiscountVO(
                    discountamount: newpriceadjustment,
                    name: productTypeVO.name,
                    description: "Menu Price Adjustment",
                    producttype: productTypeVO,
                    menu: newmenu,
                    isactive: 1,
                    updateTimeStamp: LocalDateTime.now(),
                    createTimeStamp: LocalDateTime.now(),
            )

            discountVO = discountRepo.save(discountVO)

            newmenu.discount_list.add(discountVO)

            newmenu.updateTimeStamp = LocalDateTime.now()
            newmenu = menuRepo.save(newmenu)


            model.addAttribute("successMessage", "Success: Created new menu with price adjustment: " + newpriceadjustment)

            return newmenu

        } catch (Exception e){
            System.out.println("Caught Exception: "+e.getMessage())
            model.addAttribute("errorMessage", "Error: Problem creating new menu in createNewMenu. ")
            return new MenuVO(menuid: 0)
        }



    }

    @Transactional
    TransactionVO checkForExistingDiscountOfSameProducttype(
            TransactionVO transactionVO,
            String transactionid,
            Integer producttypeid
    ) {
        List<DiscountVO> discountsCopy = new ArrayList<>(transactionVO.getDiscount_list());

        for (DiscountVO existingOldDiscount : discountsCopy) {
            if (existingOldDiscount.getProducttype().getProducttypeid().equals(producttypeid)
                    && existingOldDiscount.getIsactive() == 1) {

                // Deactivate the matching discount
                existingOldDiscount.setIsactive(0);
                existingOldDiscount.setUpdateTimeStamp(LocalDateTime.now());
                discountRepo.save(existingOldDiscount);

                // Re-fetch the transaction to ensure the latest state is loaded
                transactionVO = transactionRepo.findById(Integer.valueOf(transactionid)).orElseThrow({ new EntityNotFoundException("Transaction not found: " + transactionid) });

                // Recalculate totals by crediting back the removed discount
                transactionVO = removeDiscountFromTransactionReCalcTotals(transactionVO, existingOldDiscount, transactionVO.originalprice, transactionVO.total);
            }
        }

        return transactionVO;
    }


    boolean checkIsAmountValid(double newpriceadjustment, int producttypeid, List<ProductVO> productVOS, Model model){
        for(ProductVO productVO : productVOS){
            // when we find a match check the price to make sure we don't set it below 0
            if(productVO.producttypeid.producttypeid == producttypeid){
                double priceafterdiscount = Math.max(0.00, productVO.price - newpriceadjustment)
                if(priceafterdiscount == Double.valueOf(0.00)){
                    model.addAttribute("errorMessage", "Price after discount on: "+productVO.name+" must be greater than 0")
                    return false
                }
            }
        }
        return true

    }


    void findMenus(Model model, Optional<Integer> page, Optional<Integer> size){

        // START PAGINATION
        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);
        int pageSize = 5;
        Pageable pageable;
        if(currentPage == 0){
            pageable = PageRequest.of(0 , pageSize);
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize);
        }

        Page<MenuVO> pageOfMenu = menuRepo.findAll(pageable);

        int totalPages = pageOfMenu.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }


        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfMenu.getTotalPages());
        model.addAttribute("menuPage", pageOfMenu);
        model.addAttribute("menuPage2", pageOfMenu);// need 2 menuPages so thymeleaf can page it twice with :each
        // END PAGINATION



    }


    MenuVO getExistingMenu(String menuid){

        Optional<MenuVO> menuVO = menuRepo.findById(Integer.valueOf(menuid))

        if(!menuVO.empty){
            return menuVO.get()
        } else {
            return new MenuVO(menuid: 0)
        }
    }


//    MenuVO hydrateTransientQuantitiesForDisplay(MenuVO menuVO){
//
//        // cycle thru here and if the productid is the same then update the quantity
//        ProductVO previous = new ProductVO(barcode: 0)
//        for(ProductVO productVO : menuVO.menu_product_list){
//            if(productVO.displayquantity == null){
//                productVO.displayquantity = 1
//            }
//            if(productVO.barcode == previous.barcode){
//                productVO.displayquantity = productVO.displayquantity + 1
//            }
//            previous = productVO
//        }
//
//        return menuVO
//
//    }

    // todo: this needs to fill a transient field that holds the path to an image?
    MenuVO loadMenu(String menuid, Model model){

        MenuVO menuVO = new MenuVO()
        // if cartid == 0 then load normally, otherwise load the existing transaction
        if(menuid == "0"){
            // do nothing
            // if it is the first time loading the page
            if(menuVO.menu_product_list == null){
                // menuVO.setTotal(0) // set total to 0 initially
            }
            model.addAttribute("menu", menuVO);
            return menuVO

        } else {
            menuVO = getExistingMenu(menuid)
            menuVO = hydrateTransientQuantitiesForDisplay(menuVO)

            List<ProductVO> uniqueproducts = ProductVO.getUniqueProducts(menuVO.menu_product_list)
            // cycle through every unique product and build the uri for the primary photo
            for(ProductVO productVO : uniqueproducts){
                productVO.setPrimaryphoto(appConstants.UPLOAD_DIR_IMAGES+productVO.product_id)
                productVO.setVideodir(appConstants.UPLOAD_DIR_IMAGES+productVO.product_id)
            }


            menuVO.menu_product_list = uniqueproducts

            ProductVO.sortProductsByPrice(menuVO.menu_product_list)

            model.addAttribute("menu", menuVO)
            return menuVO
        }
    }


    boolean validateShoppingToken(String menuid, String token, Model model){
        // check to see if token is valid
        if(!jwtTokenProvider.validateShoppingToken(token, menuid)){
            model.addAttribute("errorMessage", "Invalid shopping token")
            return false
        }
        return true
    }

    // todo: this needs to fill a transient field that holds the path to an image?
    MenuVO loadMenuWithToken(String menuid, Model model, String token){

        if(!validateShoppingToken(menuid, token, model)){
            return null // should do something other than this but whatever
        }

        // if token was validated, then we can load the menu

        MenuVO menuVO = new MenuVO()
        // if cartid == 0 then load normally, otherwise load the existing transaction
        if(menuid == "0"){
            // do nothing
            // if it is the first time loading the page
            if(menuVO.menu_product_list == null){
                // menuVO.setTotal(0) // set total to 0 initially
            }
            model.addAttribute("menu", menuVO);
            return menuVO

        } else {
            menuVO = getExistingMenu(menuid)
            //menuVO = hydrateTransientQuantitiesForDisplay(menuVO)

            List<ProductVO> uniqueproducts = ProductVO.getUniqueProducts(menuVO.menu_product_list)
            // cycle through every unique product and build the uri for the primary photo
            for(ProductVO productVO : uniqueproducts){
                productVO.setPrimaryphoto(appConstants.UPLOAD_DIR_IMAGES+productVO.product_id)
                productVO.setVideodir(appConstants.UPLOAD_DIR_IMAGES+productVO.product_id)
                // apply discount to these products so price displays on ui correctly
                checkoutHelper.applyDiscountByProductTypeForMenu(Optional.of(menuVO), productVO)
            }



            menuVO.menu_product_list = uniqueproducts

            // now that we have the unique products, find all discounts with this menuid and apply them to prices
            //List<DiscountVO> discountVOS = discountRepo.findAllByMenu(menuVO)
            menuVO.setDisplayPrice() // this sets the displayprice on the products

            ProductVO.sortProductsByDisplayPrice(menuVO.menu_product_list)

//            menuVO.menu_product_list.each { item ->
//                item.price = item.displayprice // set all prices to the displayprice
//            }

            model.addAttribute("menu", menuVO)
            return menuVO
        }
    }


    CartVO validateCartVO(CartVO cartVO, Model model){
        if(cartVO?.customer?.customerid == null){
            model.addAttribute("errorMessage","Please select a customer")
        }
        if(cartVO?.barcode == null || cartVO?.barcode?.empty){
            model.addAttribute("primaryMessage","Add a product to your cart")
        } else {
            // only run this database check if barcode is not null
            Optional<ProductVO> productVO = cartDeleteService.doesProductExist(cartVO.barcode)
            if(productVO.empty){
                model.addAttribute("errorMessage","Product does not exist")
            } else {
                int cartcount = cartDeleteService.getCountOfProductInCartByBarcode(cartVO)
                // check here if the quantity we are trying to add will exceed the quantity in stock
                if(cartcount >= productVO.get().quantityremaining){
                    model.addAttribute("errorMessage","Quantity exceeds quantity in stock")
                }
            }

        }

        return cartVO
    }

    CartVO validateMenuPageCartVO(CartVO cartVO, Model model){
        if(cartVO?.customer?.customerid == null && cartVO?.customerid == null){
            model.addAttribute("errorMessage","Please select a customer")
        }
        if(cartVO?.barcode == null || cartVO?.barcode?.empty){
            model.addAttribute("primaryMessage","Add a product to your cart")
        } else {
            // only run this database check if barcode is not null
            Optional<ProductVO> productVO = cartDeleteService.doesProductExist(cartVO.barcode)
            if(productVO.empty){
                model.addAttribute("errorMessage","Product does not exist")
            } else {
                int cartcount = cartDeleteService.getCountOfProductInCartByBarcode(cartVO)
                // check here if the quantity we are trying to add will exceed the quantity in stock
                if(cartcount >= productVO.get().quantityremaining){
                    model.addAttribute("errorMessage","Quantity exceeds quantity in stock")
                }
            }

        }

        return cartVO
    }

    void sendShoppingToken(

                            String menuid,
                           String customerid,
                           String phonenumber,
                           String tokenlength,
                           Model model

    ){

        List<Role> roles = Arrays.asList(Role.ROLE_CLIENT, Role.ROLE_SHOPPING_TOKEN);

        CustomerVO customerVO = customerRepo.findByCustomerid(Integer.valueOf(customerid)).get()

        //generate a token
        String token = jwtTokenProvider.createMenuShoppingToken(customerVO.email, roles, Integer.valueOf(tokenlength), menuid, customerid)

        boolean isDev1 = "dev1".equals(environment.getProperty("spring.profiles.active"));

        BigInteger numberfromui = new BigInteger("1" + phonenumber);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SystemUserDAO systemUserDAO = systemUserRepo.findByEmail(authentication.getPrincipal().username)

        if(systemUserDAO.phone.equals(String.valueOf(numberfromui))){
            // if the phone number entered is the same as the logged in user, just send a single token out
            twilioTextUtil.sendShoppingTokenLinkSMS(phonenumber,isDev1, menuid, token)
        } else {
            // send a copy of the token to the system user logged in number and also to the customer phone number
            twilioTextUtil.sendShoppingTokenLinkSMS(phonenumber,isDev1, menuid, token)

            twilioTextUtil.sendShoppingTokenLinkSMSWithCustomMessage(
                    systemUserDAO.phone,isDev1, menuid, token, "Menu for Customer: "+customerVO.name+"              ")
        }


        twilioTextUtil.sendShoppingTokenLinkSMS(phonenumber,isDev1, menuid, token)

        model.addAttribute("successMessage","Shopping token sent to :"+phonenumber+" valid for "+tokenlength+" hours.  ")
    }


    @Transactional
    int addProductToCart(
            Integer cartid,
            Integer menuid,
            Integer productid,
            Integer quantityselected,
            Integer customerid,
            Model model,
            String token
    ){

        if(!validateShoppingToken(String.valueOf(menuid), token, model)){
            return 0
        }



        CustomerVO customerVO = customerRepo.findById(customerid).get()
        MenuVO menuVO = menuRepo.findById(menuid).get()

        // check to see if we need to make a new cart or not
        CartVO cartVO
        if(cartid == null || cartid == 0){

            List<CartVO> listcarts = cartRepo.findAllByMenuAndCustomer(menuVO,customerVO)
            boolean found = false
            for(CartVO cart : listcarts){
                if(cart.isprocessed == 0){
                    found = true
                    cartVO = cart
                    break
                }
            }

            if(!found){
                //create a new cart
                cartVO = cartRepo.save(new CartVO(
                        menu: menuVO,
                        customer: customerVO,
                        updateTimeStamp: LocalDateTime.now(),
                        createTimeStamp: LocalDateTime.now(),
                        isprocessed: 0
                ))
            }


        } else {
            // get the existing cart
            cartVO = cartRepo.findById(cartid).get()
        }


        // now that we have correct cart loaded for the customer, we can add the product
        cartVO = productService.addProductToCart(cartVO, quantityselected, model, String.valueOf(productid), String.valueOf(menuid))
        return cartVO.cartid
    }

    @Transactional
    int removeProductFromCart(
            Integer cartid,
            Integer menuid,
            Integer productid,
            Integer quantityselected,
            Integer customerid,
            Model model,
            String token
    ){

        if(!validateShoppingToken(String.valueOf(menuid), token, model)){
            return 0
        }



        CustomerVO customerVO = customerRepo.findById(customerid).get()
        MenuVO menuVO = menuRepo.findById(menuid).get()

        // check to see if we need to make a new cart or not
        CartVO cartVO
        if(cartid == null || cartid == 0){

            Optional<CartVO> existingcart = cartRepo.findByMenuAndCustomer(menuVO,customerVO)

            if(existingcart.empty){

                return 0

            } else {
                // this should never execute - in here as a safety measure to make sure we never have more
                // than a single cart per customerid and menuid combination
                cartVO = cartRepo.findById(cartid).get()
                deleteProductFromCart(cartVO, productid)
            }

        } else {
            // get the existing cart
            cartVO = cartRepo.findById(cartid).get()
            // delete it from cart
            cartVO = deleteProductFromCart(cartVO, productid)
        }

        return cartVO.cartid
    }

    @Transactional
    CartVO deleteProductFromCart(CartVO cartVO, int product_id){

        CartVO carttoremove = new CartVO(cartid: 0)
        // we are only removing one product at a time
        for(ProductVO productVO : cartVO.product_cart_list){
            if(productVO.product_id == product_id){
                cartVO.product_cart_list.remove(productVO)
                cartVO.total = Math.max(0, cartVO.total - productVO.price) // subtract the price from the cart total

                productVO.quantityremaining = productVO.quantityremaining + 1
                // remove the cart association from the product
                for(CartVO existingCart : productVO.cart_list){
                    if(existingCart.cartid == cartVO.cartid){
                        carttoremove = existingCart
                    }
                }
                productVO.cart_list.remove(carttoremove)

                productVO.updateTimeStamp = LocalDateTime.now()
                productRepo.save(productVO)
                break
            }
        }

        cartVO.updateTimeStamp = LocalDateTime.now()
        cartVO = cartRepo.save(cartVO)


        return cartVO

    }

    @Transactional
    CartVO emptyCart(CartVO cartVO) {

        CartVO carttoremove = new CartVO(cartid: 0);

        // Use an iterator to safely modify the list while iterating
        Iterator<ProductVO> iterator = cartVO.product_cart_list.iterator();
        while (iterator.hasNext()) {
            ProductVO productVO = iterator.next();

            // Remove the product from the cart
            iterator.remove();
            cartVO.total = Math.max(0, cartVO.total - productVO.price); // Subtract the price from the cart total

            // Update the product quantity remaining
            productVO.quantityremaining = productVO.quantityremaining + 1;

            // Remove the cart association from the product
            for (CartVO existingCart : productVO.cart_list) {
                if (existingCart.cartid == cartVO.cartid) {
                    carttoremove = existingCart;
                }
            }
            productVO.cart_list.remove(carttoremove);

            // Update timestamps and save product
            productVO.updateTimeStamp = LocalDateTime.now();
            productRepo.save(productVO);
        }

        // Update and save the cart
        cartVO.updateTimeStamp = LocalDateTime.now();
        cartVO = cartRepo.save(cartVO);

        return cartVO;
    }


    void loadCart(int cartid, Model model){
        if(cartid != 0){
            CartVO cartVO = cartRepo.findById(cartid).get()
            // sort the items in the cartVO
            checkoutHelper.hydrateTransientQuantitiesForDisplay(cartVO)

            model.addAttribute("cart", cartVO)
            model.addAttribute("cartid", cartVO.cartid) // bind this for uri param
        } else {
            model.addAttribute("cart", new CartVO(cartid: 0))
        }

    }

    void loadCartByCustomerIdAndMenuId(String shoppingtoken, Model model){
        String customerid = jwtTokenProvider.getCustomerIdFromToken(shoppingtoken)
        String menuid = jwtTokenProvider.getMenuIdFromToken(shoppingtoken)
        CustomerVO customerVO = customerRepo.findByCustomerid(Integer.valueOf(customerid)).get()
        MenuVO menuVO = menuRepo.findById(Integer.valueOf(menuid)).get()


        // The System is assuming we will only ever have a single cart per menu and customer combination
        List<CartVO> cartlist = cartRepo.findAllByMenuAndCustomer(menuVO, customerVO)

        // we are assuming here we will only ever have a single cart per user that is not processed
        for(CartVO cart : cartlist){
            if(cart.isprocessed == 0){
                // here we need to format the items inside the cart to consolidate them for proper display on ui
                checkoutHelper.hydrateTransientQuantitiesForDisplay(cart)

                model.addAttribute("cart", cart)
                model.addAttribute("cartid", cart.cartid) // bind this for uri param
            } else{
                model.addAttribute("cartid", 0) // bind this for uri param
            }
            break // break just to make sure we only ever do this once.....
        }

    }


    // we need to bind these because initially they come from the user navigating from a phone SMS message with them in the URI
    void bindHiddenValues(Model model, String shoppingtoken, String menuid){

        model.addAttribute("shoppingtoken", shoppingtoken)
        model.addAttribute("menuid", menuid)
    }



    @Transactional
    boolean checkQuantity(Integer productid, Integer quantityselected, Model model) {
        // Retrieve the product from the repository
        Optional<ProductVO> productVO = productRepo.findById(productid);

        // Check if the product exists
        if (productVO.isPresent()) {
            // Get the quantity remaining for the product
            ProductVO product = productVO.get();

            // Check if the quantity remaining is sufficient
            if (product.quantityremaining >= quantityselected) {
                return true; // Quantity is sufficient
            } else {
                // Add an error message for insufficient quantity
                model.addAttribute("errorMessage", "Insufficient quantity available");
                return false;
            }
        } else {
            // Add an error message if the product does not exist
            model.addAttribute("errorMessage", "Product does not exist");
            return false;
        }
    }


    @Transactional
    int emptyWholeCart(
            Integer cartid,
            Integer menuid,
            Model model,
            String token
    ){

        // not validating here
//        if(!validateShoppingToken(String.valueOf(menuid), token, model)){
//            return 0
//        }

        // get the existing cart
        CartVO cartVO = cartRepo.findById(cartid).get()
        // delete it from cart
        cartVO = emptyCart(cartVO)


        return cartVO.cartid
    }


    @Transactional
    TransactionVO checkoutCart(
            Integer cartid,
            Integer menuid,
            Integer locationid,
            String  deliverynotes,
            String token,
            Model model
    ){
        if(!validateShoppingToken(String.valueOf(menuid), token, model)){
            return false
        }

        // get the existing cart
        CartVO cartVO = cartRepo.findById(cartid).get()

        // generate a new transaction
        TransactionVO transactionVO = transactionService.processCartGenerateNewTransactionForDelivery(cartVO, locationid, deliverynotes)

        return transactionVO
    }



}
