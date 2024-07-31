package com.techvvs.inventory.labels.impl

import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductVO
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.stereotype.Component

import java.awt.image.BufferedImage

@Component
class LabelPrintingGenerator {



    String STATIC_LABEL_BATCH_DIR = "./uploads/staticlabels/"

    int amountoflabels = 50

    void generate50StaticWeightLabels(){



        System.out.println("Generating 50 static weight labels");

        PDDocument document = new PDDocument()
        PDPage page = new PDPage(PDRectangle.LETTER); // 8.5" x 11"
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page)
        // Define margins and layout parameters
        float topMargin = (6.0f / 16.0f) * 72; // 6/16 inches to points
        float bottomMargin = (1.0f / 16.0f) * 72; // 6/16 inches to points
        float leftMargin = 0.25f * 72; // 0.25" in points
        float rightMargin = 0.25f * 72; // 0.25" in points
        float labelWidth = (PDRectangle.LETTER.getWidth() - leftMargin - rightMargin) / 5; // 5 barcodes per row
        float labelHeight = (PDRectangle.LETTER.getHeight() - topMargin - bottomMargin) / 10; // 10 rows


        // Generate and draw UPC-A barcodes
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 5; col++) {


                if(amountoflabels == 0){
                    break
                }


                float x = leftMargin + col * labelWidth;
                float y = PDRectangle.LETTER.getHeight() - topMargin - (row + 1) * labelHeight;

                //   String barcodeData = barcodeHelper.generateBarcodeData(row, col, filenameExtension, batchnumber, pagenumber);
                String qrcodeData = "https://qr.techvvs.io"

                BufferedImage qrImage = null;
                if(skip){
                    qrImage = qrImageInScope
                } else {
                    qrImage = qrImageGenerator.generateQrImage(qrcodeData, limitStringTo20Chars(productVO.name));
                }

                // Convert BufferedImage to PDImageXObject
                PDImageXObject pdImage = LosslessFactory.createFromImage(document, qrImage);

                // Draw the barcode image on the PDF
                contentStream.drawImage(pdImage, x, y, labelWidth, labelHeight);


                amountoflabels = amountoflabels - 1
            }
        }





        // todo: add some functionality here so the user knows if barcodes are already generated or not
        // Close the content stream
        contentStream.close();

        String filename = "static-weight-labels"
        // Save the PDF document
        document.save(new File(STATIC_LABEL_BATCH_DIR+filename+".pdf"));

    }




}
