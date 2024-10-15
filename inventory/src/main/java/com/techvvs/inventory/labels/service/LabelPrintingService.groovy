package com.techvvs.inventory.labels.service

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.labels.impl.LabelPrintingGenerator
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.MenuVO
import org.apache.pdfbox.pdmodel.PDDocument
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

import java.nio.file.Files
import java.nio.file.Paths


@Service
class LabelPrintingService {

    @Autowired
    LabelPrintingGenerator labelPrintingGenerator

    @Autowired
    AppConstants appConstants


    void createDyno450TurboLabel(String labeltext){
        String outputpath = "";
        labelPrintingGenerator.createLabelPDF(labeltext, outputpath)
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
