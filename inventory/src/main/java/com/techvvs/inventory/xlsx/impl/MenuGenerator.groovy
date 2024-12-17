package com.techvvs.inventory.xlsx.impl

import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.barcode.service.BarcodeService
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.MenuRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.ProductVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.LocalDateTime


@Component
class MenuGenerator {


    @Autowired
    MenuRepo menuRepo

    @Autowired
    ProductRepo productRepo

    @Autowired
    BarcodeService barcodeService

    @Autowired
    BarcodeHelper barcodeHelper

    @Autowired
    BatchRepo batchRepo

    boolean generateDefaultMenuFromBatch(BatchVO batchVO){

        if (batchVO.menu_set.size() > 0) {
            // Use an Iterator to safely remove elements during iteration
            Iterator<MenuVO> iterator = batchVO.menu_set.iterator();
            while (iterator.hasNext()) {
                MenuVO menuVO = iterator.next();
                if (menuVO.isdefault == 1) {
                    iterator.remove(); // Safely remove the menu from the collection
                    batchVO = batchRepo.save(batchVO); // Save the updated batch
                    menuRepo.deleteById(menuVO.menuid); // Delete the menu from the repository
                }
            }
        }



        LinkedHashSet linkedHashSet = barcodeHelper.convertToLinkedHashSet(batchVO.product_set)
        List<ProductVO> expandedlist = barcodeService.expandAndDuplicateProductQuantities(linkedHashSet)


        MenuVO menuVO = new MenuVO(

                name: batchVO.name+"-"+"default-menu",
                menu_product_list: expandedlist,
                isdefault: 1,
                amount: 0.00,
                notes: "Default Menu",
                createTimeStamp: LocalDateTime.now(),
                updateTimeStamp: LocalDateTime.now()
        )

        menuVO = menuRepo.save(menuVO)


        // add the new menu
//        batchVO = batchRepo.getById(batchVO.batchid)

        batchVO.menu_set.add(menuVO)

        batchRepo.save(batchVO)

        return true
    }






}
