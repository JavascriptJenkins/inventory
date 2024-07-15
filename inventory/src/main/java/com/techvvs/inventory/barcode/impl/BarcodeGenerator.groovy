package com.techvvs.inventory.barcode.impl

import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.ProductVO
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory

import javax.imageio.ImageIO
import java.nio.file.FileSystems
import java.nio.file.Path

import java.awt.image.BufferedImage

@Component
class BarcodeGenerator {

    @Autowired
    ProductRepo productRepo

    @Autowired
    BarcodeHelper barcodeHelper

    @Autowired
    ImageGenerator imageGenerator

    String PDF_BATCH_DIR = "./uploads/pdf/"

    void generateBarcodes(String filenameExtension, int batchnumber, int pagenumber, Set<ProductVO> productset) throws IOException {

        System.out.println("Generating barcodes for " + filenameExtension + " | pagenumber: "+pagenumber);

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

                    // this will prevent duplicate barcodes
                    if(productset.size() == 0){
                        break
                    }

                    float x = leftMargin + col * labelWidth;
                    float y = PDRectangle.LETTER.getHeight() - topMargin - (row + 1) * labelHeight;

                    String barcodeData = barcodeHelper.generateBarcodeData(row, col, filenameExtension, batchnumber, pagenumber);

                    // Example method to generate barcode data
                    BufferedImage barcodeImage = imageGenerator.generateUPCABarcodeImage(barcodeData, labelWidth, labelHeight);

                    // Convert BufferedImage to PDImageXObject
                    PDImageXObject pdImage = LosslessFactory.createFromImage(document, barcodeImage);

                    // Draw the barcode image on the PDF
                    contentStream.drawImage(pdImage, x, y, labelWidth, labelHeight);

                    int toremove = productset.size() - 1
                    // iterate through productset to remove things
                    ProductVO productVO = productset.getAt(toremove)

                    //write method here to add barcode data to product in database
                    addBarcodeToProduct(productVO, barcodeData);

                     productset.remove(productVO)
                }
            }





        // todo: add some functionality here so the user knows if barcodes are already generated or not
        // Close the content stream
        contentStream.close();

        String filename = pagenumber+"-"+filenameExtension+"-"+batchnumber
        // Save the PDF document
        document.save(new File(PDF_BATCH_DIR+"upc_batch-"+filename+".pdf"));


    }


    // todo: add a check here to make sure barcodes are unique before adding....
    void addBarcodeToProduct(ProductVO productVO, String barcodedata){

        Optional<ProductVO> existingproduct = productRepo.findById(productVO.getProduct_id())

        // if we have an existing barcode do NOT overwrite it.
        if(existingproduct.get().barcode != null && existingproduct.get().barcode.length() > 0){
            // do nothing
        } else {
            productVO.setBarcode(barcodedata)
            productRepo.save(productVO)
        }

    }



}
