package com.techvvs.inventory.xlsx

import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchTypeVO
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper
import com.techvvs.inventory.xlsx.impl.ImportBatch
import com.techvvs.inventory.xlsx.impl.MenuGenerator
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

import javax.transaction.Transactional
import java.security.SecureRandom
import java.time.LocalDateTime

// Implementation code for importing XLSX files
@Service
class XlsxImporter {

    @Autowired
    ImportBatch importBatch

    @Autowired
    MenuGenerator menuGenerator

    @Autowired
    BatchControllerHelper batchControllerHelper



    /**
     * Create Batch Record
     * Create barcodes for the batch
     * Create a default menu for the batch
     *
     * **/
    @Transactional
    void importBatchFromExistingXlsxFile(String filename){

        BatchVO batchVO = importBatch.importBatchFromExistingXlsxFile(filename)

        boolean barcodesuccess = batchControllerHelper.generateAllBarcodesForBatch(String.valueOf(batchVO.batchnumber))
        boolean qrsuccess = batchControllerHelper.generateAllQrcodesForBatch(String.valueOf(batchVO.batchnumber))

        if(barcodesuccess && qrsuccess){
            menuGenerator.generateDefaultMenuFromBatch(batchVO)
        }


    }












}
