package com.techvvs.inventory.xlsx.impl

import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.barcode.service.BarcodeService
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

    boolean generateDefaultMenuFromBatch(BatchVO batchVO){


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

        menuRepo.save(menuVO)
        return true
    }






}
