package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.barcode.service.BarcodeService
import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.MenuRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.service.transactional.CartDeleteService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.ui.Model

import javax.transaction.Transactional
import java.time.LocalDateTime

@Component
class MenuHelper {

    @Autowired
    MenuRepo menuRepo

    @Autowired
    CartDeleteService cartDeleteService

    @Autowired
    AppConstants appConstants

    @Autowired
    BarcodeService barcodeService

    @Transactional
    MenuVO changePrice(double newpriceadjustment, int existingmenuid, Model model){

        try{
            MenuVO existingmenu = menuRepo.findById(existingmenuid).get()

            existingmenu.setAmount(Math.max(0.00,newpriceadjustment))
            existingmenu.updateTimeStamp = LocalDateTime.now()
            model.addAttribute("successMessage", "Success: Updated menu: "+ existingmenu.name+" with price adjustment: "+newpriceadjustment)

            return menuRepo.save(existingmenu)
        } catch (Exception e){
            System.out.println("Caught Exception: "+e.getMessage())
            model.addAttribute("errorMessage", "Error: Problem creating new menu in changePrice. ")
            return MenuVO(menuid: 0)
        }
    }

    @Transactional
    MenuVO createNewMenu(double newpriceadjustment, int existingmenuid, String name, Model model){


        try{
            MenuVO existingmenu = menuRepo.findById(existingmenuid).get()

            // Create a new list by detaching the 'menu' ownership temporarily
            List<ProductVO> expandedlist = new ArrayList<>()
            Iterator<ProductVO> iterator = existingmenu.menu_product_list.iterator()

            while (iterator.hasNext()) {
                ProductVO product = iterator.next()
                product.menu_list = null // Temporarily detach from the old menu list
                expandedlist.add(product)
            }

            // Create the new MenuVO and assign the expanded list
            MenuVO menuVO = new MenuVO(
                    name: name + "_" + "pa_" + newpriceadjustment,
                    menu_product_list: expandedlist,
                    isdefault: 0,
                    amount: Math.max(0.00, newpriceadjustment),
                    notes: "new menu created with price adjustment: " + newpriceadjustment,
                    createTimeStamp: LocalDateTime.now(),
                    updateTimeStamp: LocalDateTime.now()
            )

            // Save the new menu
            model.addAttribute("successMessage", "Success: Created new menu with price adjustment: " + newpriceadjustment)
            return menuRepo.save(menuVO)
        } catch (Exception e){
            System.out.println("Caught Exception: "+e.getMessage())
            model.addAttribute("errorMessage", "Error: Problem creating new menu in createNewMenu. ")
            return new MenuVO(menuid: 0)
        }



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


    MenuVO hydrateTransientQuantitiesForDisplay(MenuVO menuVO){

        // cycle thru here and if the productid is the same then update the quantity
        ProductVO previous = new ProductVO(barcode: 0)
        for(ProductVO productVO : menuVO.menu_product_list){
            if(productVO.displayquantity == null){
                productVO.displayquantity = 1
            }
            if(productVO.barcode == previous.barcode){
                productVO.displayquantity = productVO.displayquantity + 1
            }
            previous = productVO
        }

        return menuVO

    }

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
    
    
}
