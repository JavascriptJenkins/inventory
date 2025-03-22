package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.model.*
import com.techvvs.inventory.security.JwtTokenProvider
import com.techvvs.inventory.security.Role
import com.techvvs.inventory.security.rbac.RbacEnforcer
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.controllers.MenuService
import com.techvvs.inventory.service.transactional.CartDeleteService
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import com.techvvs.inventory.viewcontroller.helper.CustomerHelper
import com.techvvs.inventory.viewcontroller.helper.MenuHelper
import org.apache.commons.lang.mutable.MutableBoolean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletRequest

/* This controller is for handling admin actions of making the actual menus to serve.
*  A Menu is a collection of products that can be from one or many different batches
*  Once a Menu is created, other actions can be done, like serving the menu for media or sales.
*
*  */
@RequestMapping("/menu/admin")
@Controller
public class MenuAdminViewController {
    

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

    @Autowired
    CustomerHelper customerHelper

    @Autowired
    RbacEnforcer rbacEnforcer

    @Autowired
    MenuService menuService



    /* This will populate the paginated table of menus when called without menuid
    * When called with a menuid, it will load that menu into the menu in scope
    *  */
    @GetMapping("/manage")
    String menuAdminPage(
            @ModelAttribute( "menu" ) MenuVO menuVO,
            Model model,
            @RequestParam("menuid") Optional<Integer> menuid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("productPageInt") Optional<Integer> productpage,
            @RequestParam("productSizeInt") Optional<Integer> productsize,
            @RequestParam("selectionproductPageInt") Optional<Integer> selectionproductpage,
            @RequestParam("selectionproductSizeInt") Optional<Integer> selectionproductsize,
            HttpServletRequest req
    ){

        techvvsAuthService.checkuserauth(model)
        if(rbacEnforcer.enforceAdminRights(model,req)) {
            // do nothing, proceed.  We have injected a value into the model for viewing admin buttons on the ui too
        } else {
            return "auth/index.html" // return to home page, will send user to logout page if they have expired cookie i think
        }

        // todo: need to bind the productPage from the menu_product_list
        // load selected menu into scope
        if(menuid.isPresent() && menuid.get() != null && menuid.get() != 0) {
            MenuVO boundMenu = menuHelper.loadMenu(String.valueOf(menuid.get()), model)
            menuHelper.addPaginatedListOfProducts(boundMenu, model, productpage, productsize) // on the menu in scope
            menuHelper.addPaginatedListOfSelectionProducts(boundMenu, model, selectionproductpage, selectionproductsize) // from the system
            model.addAttribute("editmode", true);
        } else {
            model.addAttribute("menu", new MenuVO(menuid: 0));
            model.addAttribute("editmode", false);
        }



        // bind all menus here
        menuService.bindAllMenus(model, page, size)


        return "menu/admin.html";
    }


    @PostMapping("/remove/product")
    String removeProduct(
            @ModelAttribute( "menu" ) MenuVO menuVO,
            Model model,
            @RequestParam("menuid") Optional<Integer> menuid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("productPageInt") Optional<Integer> productpage,
            @RequestParam("productSizeInt") Optional<Integer> productsize,
            @RequestParam("selectionproductPageInt") Optional<Integer> selectionproductpage,
            @RequestParam("selectionproductSizeInt") Optional<Integer> selectionproductsize,
            @RequestParam Map<String, String> selectedProducts,
            HttpServletRequest req
    ){

        techvvsAuthService.checkuserauth(model)
        if(rbacEnforcer.enforceAdminRights(model,req)) {
            // do nothing, proceed.  We have injected a value into the model for viewing admin buttons on the ui too
        } else {
            return "auth/index.html" // return to home page, will send user to logout page if they have expired cookie i think
        }


        // load selected menu into scope
        if(menuid.isPresent() && menuid.get() != null && menuid.get() != 0) {
            MenuVO boundMenu = menuHelper.loadMenu(String.valueOf(menuid.get()), model)
            menuHelper.removeSelectedProducts(boundMenu, model, selectedProducts)
            menuHelper.addPaginatedListOfProducts(boundMenu, model, productpage, productsize) // on the menu in scope
            menuHelper.addPaginatedListOfSelectionProducts(boundMenu, model, selectionproductpage, selectionproductsize) // from the system
            model.addAttribute("editmode", true);
        } else {
            model.addAttribute("menu", new MenuVO(menuid: 0));
            model.addAttribute("editmode", false);
        }



        // bind all menus here
        menuService.bindAllMenus(model, page, size)


        return "menu/admin.html";
    }


    @PostMapping("/add/product")
    String addProduct(
            @ModelAttribute( "menu" ) MenuVO menuVO,
            Model model,
            @RequestParam("menuid") Optional<Integer> menuid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("productPageInt") Optional<Integer> productpage,
            @RequestParam("productSizeInt") Optional<Integer> productsize,
            @RequestParam("selectionproductPageInt") Optional<Integer> selectionproductpage,
            @RequestParam("selectionproductSizeInt") Optional<Integer> selectionproductsize,
            @RequestParam Map<String, String> selectedProducts,
            HttpServletRequest req
    ){

        techvvsAuthService.checkuserauth(model)
        if(rbacEnforcer.enforceAdminRights(model,req)) {
            // do nothing, proceed.  We have injected a value into the model for viewing admin buttons on the ui too
        } else {
            return "auth/index.html" // return to home page, will send user to logout page if they have expired cookie i think
        }

        // todo: need to bind the productPage from the menu_product_list
        // load selected menu into scope
        if(menuid.isPresent() && menuid.get() != null && menuid.get() != 0) {
            MenuVO boundMenu = menuHelper.loadMenu(String.valueOf(menuid.get()), model)
            menuHelper.addSelectedProducts(boundMenu, model, selectedProducts)
            menuHelper.addPaginatedListOfSelectionProducts(boundMenu, model, selectionproductpage, selectionproductsize) // from the system
            menuHelper.addPaginatedListOfProducts(boundMenu, model, productpage, productsize) // on the menu in scope
            model.addAttribute("editmode", true);
        } else {
            model.addAttribute("menu", new MenuVO(menuid: 0));
            model.addAttribute("editmode", false);
        }



        // bind all menus here
        menuService.bindAllMenus(model, page, size)


        return "menu/admin.html";
    }



    @PostMapping("/create")
    String createMenu(
            @ModelAttribute( "menu" ) MenuVO menuVO,
            Model model,
            @RequestParam("menuid") Optional<Integer> menuid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("editmode") Optional<Boolean> editmode,
            HttpServletRequest req
    ){

        techvvsAuthService.checkuserauth(model)
        if(rbacEnforcer.enforceAdminRights(model,req)) {
            // do nothing, proceed.  We have injected a value into the model for viewing admin buttons on the ui too
        } else {
            return "auth/index.html" // return to home page, will send user to logout page if they have expired cookie i think
        }


        // here we check if MenuVO is coming in with values and 0 as it's id
        if(menuVO != null && menuVO.getMenuid() == 0) {

            /* CREATE */
            menuVO = menuService.validateMenuOnAdminPage(menuVO, model, true)

            // only proceed if there is no error
            if(model.getAttribute(MessageConstants.ERROR_MSG) == null){

                // create a new menu
                menuVO = menuService.createMenu(menuVO)

                // bind in the menuid that was just created
                menuid = Optional.of(menuVO.getMenuid())
                model.addAttribute("successMessage", "Menu created successfully!")
            }

        }

        // here we check if MenuVO is coming in with values and 0 as it's id
        if(menuVO != null && menuVO.getMenuid() > 0) {

            /* UPDATE/EDIT */
            menuVO = menuService.validateMenuOnAdminPage(menuVO, model, true)

            // only proceed if there is no error
            if(model.getAttribute(MessageConstants.ERROR_MSG) == null){

                // create a new menu
                menuVO = menuService.updateMenu(menuVO)

                // bind in the menuid that was just created
                menuid = Optional.of(menuVO.getMenuid())
                model.addAttribute("successMessage", "Menu updated successfully!")
            }

        }







        // load selected menu into scope
        if(menuid.isPresent() && menuid.get() != null && menuid.get() != 0) {
            menuHelper.loadMenu(String.valueOf(menuid.get()), model)
            model.addAttribute("editmode", true);
        } else {
            model.addAttribute("menu", new MenuVO(menuid: 0));
            model.addAttribute("editmode", false);
        }

        // bind all menus here
        menuService.bindAllMenus(model, page, size)


        return "menu/admin.html";
    }


    /* On the menu admin page, copy existing items from an existing menu */
    @GetMapping("/copyexistingitems")
    String menuCopyExistingItems(
            @ModelAttribute( "menu" ) MenuVO menuVO,
            Model model,
            @RequestParam("menuid") Optional<Integer> menuid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("productPageInt") Optional<Integer> productpage,
            @RequestParam("productSizeInt") Optional<Integer> productsize,
            @RequestParam("selectionproductPageInt") Optional<Integer> selectionproductpage,
            @RequestParam("selectionproductSizeInt") Optional<Integer> selectionproductsize,
            HttpServletRequest req
    ){

        techvvsAuthService.checkuserauth(model)
        if(rbacEnforcer.enforceAdminRights(model,req)) {
            // do nothing, proceed.  We have injected a value into the model for viewing admin buttons on the ui too
        } else {
            return "auth/index.html" // return to home page, will send user to logout page if they have expired cookie i think
        }

        // todo: need to bind the productPage from the menu_product_list
        // load selected menu into scope
        if(menuid.isPresent() && menuid.get() != null && menuid.get() != 0) {

            // this is the target menu we want to copy the items to
            MenuVO boundMenu = menuHelper.loadMenu(String.valueOf(menuid.get()), model)



            menuHelper.addPaginatedListOfProducts(boundMenu, model, productpage, productsize) // on the menu in scope
            menuHelper.addPaginatedListOfSelectionProducts(boundMenu, model, selectionproductpage, selectionproductsize) // from the system
            model.addAttribute("editmode", true);
        } else {
            model.addAttribute("menu", new MenuVO(menuid: 0));
            model.addAttribute("editmode", false);
        }



        // bind all menus here
        menuService.bindAllMenusSimple(model)
//        menuService.bindAllMenus(model, page, size)


        return "menu/copyexistingitems.html";
    }





    /* On the menu admin page, copy existing items from an existing menu */
    @PostMapping("/copyexistingitems")
    String menuCopyExistingItemsPost(
            @ModelAttribute( "menu" ) MenuVO menuVO,
            Model model,
            @RequestParam("targetmenuid") Optional<Integer> targetmenuid,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("productPageInt") Optional<Integer> productpage,
            @RequestParam("productSizeInt") Optional<Integer> productsize,
            @RequestParam("selectionproductPageInt") Optional<Integer> selectionproductpage,
            @RequestParam("selectionproductSizeInt") Optional<Integer> selectionproductsize,
            HttpServletRequest req
    ){

        techvvsAuthService.checkuserauth(model)
        if(rbacEnforcer.enforceAdminRights(model,req)) {
            // do nothing, proceed.  We have injected a value into the model for viewing admin buttons on the ui too
        } else {
            return "auth/index.html" // return to home page, will send user to logout page if they have expired cookie i think
        }

        // load selected menu into scope
        if((menuVO && menuVO != null && menuVO.menuid != 0) && (targetmenuid.isPresent() && targetmenuid.get() != null && targetmenuid.get() != 0)) {

            // do the actual copying of the items
            boolean success = menuHelper.copyExistingItemsFromMenu(targetmenuid.get(), menuVO.menuid)

            if(success) {
                model.addAttribute("successMessage", "Items Copied Successfully")
            } else {
                model.addAttribute("errorMessage", "Failed to Copy Items")
            }

            model.addAttribute("editmode", true);
        } else {
            model.addAttribute("menu", new MenuVO(menuid: 0));
            model.addAttribute("editmode", false);
        }



        // bind all menus here
        menuService.bindAllMenusSimple(model)
//        menuService.bindAllMenus(model, page, size)


        return "menu/copyexistingitems.html";
    }














}
