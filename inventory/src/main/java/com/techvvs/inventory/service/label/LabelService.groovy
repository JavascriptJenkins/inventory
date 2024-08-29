package com.techvvs.inventory.service.label

import com.techvvs.inventory.barcode.impl.BarcodeGenerator
import com.techvvs.inventory.barcode.service.BarcodeService
import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.viewcontroller.helper.ProductHelper
import org.apache.pdfbox.pdmodel.PDDocument
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.transaction.Transactional

@Service
class LabelService {


    @Autowired
    BarcodeService barcodeService

    @Autowired
    AppConstants appConstants

    @Autowired
    ProductHelper productHelper

    @Autowired
    BarcodeGenerator barcodeGenerator

    // this is going to take in a menu as argument, and then print barcodes for every product in the menu
    @Transactional
    boolean generateBarcodeAndQrLabelsForMenu(MenuVO menuVO) {
        try {

            List<List<ProductVO>> result = productHelper.sortAndExpandProductList(menuVO.menu_product_list)

            // create document before we loop over the collections of products so all pdf pages land in a single document
            PDDocument document = new PDDocument()
            for(int i = 0; i < result.size(); i++) {
                barcodeGenerator.generateBarcodesForAllItems(
                        menuVO.menuid,
                        i,
                        result.get(i),
                        menuVO.name,
                        document
                )
            }

            barcodeService.saveBarcodeLabelPdfFileForEntity(document, appConstants.BARCODES_MENU_DIR, menuVO.name, menuVO.menuid)

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }





}
