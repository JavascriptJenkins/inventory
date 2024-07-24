package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.jparepo.MenuRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.ProductVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.ui.Model

@Component
class MenuHelper {

    @Autowired
    MenuRepo menuRepo


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

    Model loadMenu(String menuid, Model model, MenuVO menuVO){
        // if cartid == 0 then load normally, otherwise load the existing transaction
        if(menuid == "0"){
            // do nothing
            // if it is the first time loading the page
            if(menuVO.menu_product_list == null){
                // menuVO.setTotal(0) // set total to 0 initially
            }
            model.addAttribute("menu", menuVO);

        } else {
            menuVO = getExistingMenu(menuid)
            menuVO = hydrateTransientQuantitiesForDisplay(menuVO)

            List<ProductVO> uniqueproducts = ProductVO.getUniqueProducts(menuVO.menu_product_list)

            menuVO.menu_product_list = uniqueproducts

            model.addAttribute("menu", menuVO)
        }
    }
    
    
}
