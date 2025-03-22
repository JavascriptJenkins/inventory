package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.jparepo.MenuRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.validation.StringSecurityValidator
import com.techvvs.inventory.validation.generic.ObjectValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.ui.Model

import java.time.LocalDateTime

@Service
class MenuService {

    @Autowired
    MenuRepo menuRepo

    @Autowired
    StringSecurityValidator stringSecurityValidator

    @Autowired
    ObjectValidator objectValidator




    MenuVO createMenu(MenuVO menuVO){

        menuVO.updateTimeStamp = LocalDateTime.now()
        menuVO.createTimeStamp = LocalDateTime.now()
        menuVO.isdefault = 0

        menuVO = menuRepo.save(menuVO)

        return menuVO
    }

    MenuVO updateMenu(MenuVO menuVO){

        menuVO.updateTimeStamp = LocalDateTime.now()
        menuVO = menuRepo.save(menuVO)
        return menuVO
    }

    MenuVO validateMenuOnAdminPage(MenuVO menuVO, Model model, boolean iscreate) {

        // first - validate against security issues
        stringSecurityValidator.validateStringValues(menuVO, model)

        // second - validate all object fields
        objectValidator.validateForCreateOrEdit(menuVO, model, iscreate)

        // third - do any business logic / page specific validation below


        return menuVO
    }

    // being used on the batch/admin.html page
    void bindAllMenus(Model model,
                      Optional<Integer> page,
                      Optional<Integer> size){


        //pagination
        int currentPage = page.orElse(0);
        int pageSize = size.isEmpty() ? 5 : size.get();
        Pageable pageable;
        Sort sort = Sort.by("menuid").descending(); // ← change "title" to your desired field
        if(currentPage == 0){
            pageable = PageRequest.of(0 , pageSize, sort);
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize, sort);
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
    }

    void bindAllMenusSimple(Model model){
        List<MenuVO> menulist = menuRepo.findAll();
        model.addAttribute("menus", menulist);
    }



}
