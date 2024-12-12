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

        if(batchVO.menu_set.size() > 0){
            int i = 0;
            for(MenuVO menuVO: batchVO.menu_set){
                if(menuVO.isdefault == 1){
                    batchVO.menu_set.remove(i) // remove the menu from associated batch
                    batchVO = batchRepo.save(batchVO)
                    menuRepo.deleteById(menuVO.menuid) // delete existing default menu before generating another one
                }
                i++
            }
        }


        LinkedHashSet linkedHashSet = barcodeHelper.convertToLinkedHashSet(batchVO.product_set)
        List<ProductVO> expandedlist = barcodeService.expandAndDuplicateProductQuantities(linkedHashSet)


        MenuVO menuVO = new MenuVO(

                name: batchVO.name+"-"+"default-menu",
                menu_product_list: expandedlist,
                isdefault: 1,
                notes: "Default Menu",
                createTimeStamp: LocalDateTime.now(),
                updateTimeStamp: LocalDateTime.now()
        )

        menuVO = menuRepo.save(menuVO)


        // add the new menu
        BatchVO refreshedbatchvo = batchRepo.getById(batchVO.batchid)

        refreshedbatchvo.menu_set.add(menuVO)

        batchRepo.save(refreshedbatchvo)

        return true
    }






}
