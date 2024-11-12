package com.techvvs.inventory.labels.service

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.labels.impl.LabelPrintingGenerator
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.viewcontroller.helper.ProductHelper
import org.apache.pdfbox.pdmodel.PDDocument
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

import javax.transaction.Transactional
import java.nio.file.Files
import java.nio.file.Paths


@Service
class LabelPrintingService {

    @Autowired
    LabelPrintingGenerator labelPrintingGenerator

    @Autowired
    AppConstants appConstants

    @Autowired
    ProductHelper productHelper


    // Dimensions: 1-1/8" x 3-1/2" (28mm x 89mm)
    void createDyno550TurboLabel28mmx89mm(BatchVO batchVO){

        // gonna feed in lists of 10 products at a time cuz that seems to work best
        try{
            PDDocument document = new PDDocument()
            int pagenumber = 0;
            // create a single epson for each product (print multiple times if needed....)
            for(ProductVO product : batchVO.product_set){

                labelPrintingGenerator.generateDynmo55028mmx98mmLabel(
                        batchVO.batchid,
                        pagenumber,
                        batchVO,
                        product,
                        document)
                pagenumber++
            }

            saveBarcode28mmx89mmLabelsPdfFile(document, appConstants.BARCODES_EPSON_64_DIR, batchVO.name,batchVO.batchnumber)

        } catch(Exception ex){
            System.out.println("Generate epson failed: "+ex.getMessage().toString())

        }

    }

    void createEpsonC6000AuLabel4by6point5(BatchVO batchVO){

        // nee
      //  List<List<ProductVO>> result = productHelper.convertProductSetIntoListofLists(batchVO.product_set)


        // gonna feed in lists of 10 products at a time cuz that seems to work best
        try{
            PDDocument document = new PDDocument()
            int pagenumber = 0;
            // create a single epson for each product (print multiple times if needed....)
            for(ProductVO product : batchVO.product_set){

                labelPrintingGenerator.generateEpson4by6point5Label(
                        batchVO.batchid,
                        pagenumber,
                        batchVO,
                        product,
                        document)
                pagenumber++
            }

            saveBarcodeEpson4by6point5LabelsPdfFile(document, appConstants.BARCODES_EPSON_64_DIR, batchVO.name,batchVO.batchnumber)

        } catch(Exception ex){
            System.out.println("Generate epson failed: "+ex.getMessage().toString())

        }
    }


    // this will generate a barcode manifest like youre at a grocery store scanning in
    @Transactional
    void createBarcodeManifestScanSheetForBatch(BatchVO batchVO){


        // chop the batchVO product set into lists of 10
        List<List<ProductVO>> result = productHelper.convertProductSetIntoListofLists(batchVO.product_set)


        try{
            PDDocument document = new PDDocument()

            int pagenumber = 0;
            // cycle thru the list of 10 and print 5 on each side of paper sheet
            for(List<ProductVO> listofTen : result){
                labelPrintingGenerator.generateBarcodeManifest(
                        batchVO.batchid,
                        pagenumber,
                        batchVO,
                        listofTen,
                        document)
                pagenumber++
            }
            saveBarcodeManifestPdfFile(document, appConstants.BARCODES_MANIFEST_DIR, batchVO.name,batchVO.batchnumber)

        } catch (Exception ex){
            System.out.println("Generate barcode manifest sheet failed: "+ex.getMessage().toString())
        }
    }

    void printAdhocLabelSheet(MenuVO menuVO) {

        // NOTE: right now this is going to generate barcodes for every product in batch regardless of product type
        try {

            // run this 50 times to make a barcode sheet
            PDDocument document = new PDDocument()

            labelPrintingGenerator.generate50AdhocLabels(
                    0,
                    0,
                    menuVO,
                    document,
                    "Adhoc" // This will show up on the metadata at top of barcode sheet
            )


            saveAdhocLabelPdfFile(document, appConstants.ADHOC_DIR, menuVO.adhoc_label1+menuVO.adhoc_label2+menuVO.adhoc_label3+generateGUID(), 0)

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    def generateGUID() {
        return UUID.randomUUID().toString()
    }
    void generate50StaticWeightLabels(BatchVO batchVO) {

        try {

            labelPrintingGenerator.generate50StaticWeightLabels();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    void saveBarcode28mmx89mmLabelsPdfFile(PDDocument document,
                                                 String entitysubdirectory,
                                                 String entityname,
                                                 int entitynumber) {

        entityname = chopRightPaddedString(entityname)
        entityname = entityname.replace(" ", "_")
        entityname = entityname.replace("|", "_")

        // create a directory with the batchnumber and /barcodes dir if it doesn't exist yet
        Files.createDirectories(Paths.get(appConstants.PARENT_LEVEL_DIR+String.valueOf(entitynumber)+entitysubdirectory));
        String filename = entityname+"-"+entitynumber
        // save the actual file after looping thru all products
        document.save(appConstants.PARENT_LEVEL_DIR+String.valueOf(entitynumber)+entitysubdirectory+appConstants.filenameprefix_dymno_28mmx89mm+filename+".pdf")
        document.close();
    }

    void saveBarcodeEpson4by6point5LabelsPdfFile(PDDocument document,
                                    String entitysubdirectory,
                                    String entityname,
                                    int entitynumber) {

        entityname = chopRightPaddedString(entityname)
        entityname = entityname.replace(" ", "_")
        entityname = entityname.replace("|", "_")

        // create a directory with the batchnumber and /barcodes dir if it doesn't exist yet
        Files.createDirectories(Paths.get(appConstants.PARENT_LEVEL_DIR+String.valueOf(entitynumber)+entitysubdirectory));
        String filename = entityname+"-"+entitynumber
        // save the actual file after looping thru all products
        document.save(appConstants.PARENT_LEVEL_DIR+String.valueOf(entitynumber)+entitysubdirectory+appConstants.filenameprefix_epson46+filename+".pdf")
        document.close();
    }

    void saveBarcodeManifestPdfFile(PDDocument document,
                                    String entitysubdirectory,
                                    String entityname,
                                    int entitynumber) {

        entityname = chopRightPaddedString(entityname)
        entityname = entityname.replace(" ", "_")
        entityname = entityname.replace("|", "_")

        // create a directory with the batchnumber and /barcodes dir if it doesn't exist yet
        Files.createDirectories(Paths.get(appConstants.PARENT_LEVEL_DIR+String.valueOf(entitynumber)+entitysubdirectory));
        String filename = entityname+"-"+entitynumber
        // save the actual file after looping thru all products
        document.save(appConstants.PARENT_LEVEL_DIR+String.valueOf(entitynumber)+entitysubdirectory+appConstants.filenameprefix_manifest+filename+".pdf")
        document.close();
    }

    void saveAdhocLabelPdfFile(PDDocument document, String entitysubdirectory, String entityname, int entitynumber) {


        entityname = chopRightPaddedString(entityname)
        entityname = entityname.replace(" ", "_")
        entityname = entityname.replace("|", "_")

        // create a directory with the batchnumber and /barcodes dir if it doesn't exist yet
        Files.createDirectories(Paths.get(appConstants.PARENT_LEVEL_DIR+appConstants.LABEL_DIR+String.valueOf(entitynumber)+entitysubdirectory));

        String filename = entityname+"-"+entitynumber
        // save the actual file after looping thru all products
        document.save(appConstants.PARENT_LEVEL_DIR+appConstants.LABEL_DIR+entitynumber+entitysubdirectory+appConstants.filenameprefix_adhoc_label+filename+".pdf");
        document.close();
    }

    void saveQrMediaLabelPdfFile(PDDocument document, String entitysubdirectory, String entityname, int entitynumber) {


        entityname = chopRightPaddedString(entityname)
        entityname = entityname.replace(" ", "_")
        entityname = entityname.replace("|", "_")

        // create a directory with the batchnumber and /barcodes dir if it doesn't exist yet
        Files.createDirectories(Paths.get(appConstants.PARENT_LEVEL_DIR+appConstants.LABEL_DIR+String.valueOf(entitynumber)+entitysubdirectory));

        String filename = entityname+"-"+entitynumber
        // save the actual file after looping thru all products
        document.save(appConstants.PARENT_LEVEL_DIR+appConstants.LABEL_DIR+entitynumber+entitysubdirectory+appConstants.filenameprefix_qr_media+filename+".pdf");
        document.close();
    }

    def chopRightPaddedString(String input) {
        // Split by whitespace and filter out empty strings, then join with a single space
        return input.split(/\s+/).findAll { it }.join(' ')
    }




}
