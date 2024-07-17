package com.techvvs.inventory.barcode.impl

import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.ProductVO
import org.apache.pdfbox.pdmodel.font.PDType0Font
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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






    ProductVO productinscope = new ProductVO(product_id: 0)
    boolean skip = false
    //
    void generateBarcodesForAllItems(String filenameExtension, int batchnumber, int pagenumber, List<ProductVO> productlist, String batchname) throws IOException {



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


        // Example of using a TrueType font
        PDType0Font ttfFont = PDType0Font.load(document, new File("./uploads/font/Oswald-VariableFont_wght.ttf"));


        // Print page number on the upper left corner
        contentStream.beginText();
        contentStream.setFont(ttfFont, 12);
        contentStream.newLineAtOffset(leftMargin, PDRectangle.LETTER.getHeight() - topMargin / 2 as float);
        contentStream.showText("Page " + pagenumber);
        contentStream.endText();

        // metadata accross the top
        contentStream.beginText();
        contentStream.setFont(ttfFont, 12);
        contentStream.newLineAtOffset(leftMargin+100 as float, PDRectangle.LETTER.getHeight() - topMargin / 2 as float);
        contentStream.showText(
                "Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        + " | Batch #: " + batchnumber
                        + " | Batch Name: " + batchname
        );
        contentStream.endText();


        // Generate and draw UPC-A barcodes
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 5; col++) {

                // this will prevent duplicate barcodes
                if(productlist.size() == 0){
                    break
                }

                int toremove = productlist.size() - 1
                ProductVO productVO = productlist.getAt(toremove)
                if(productinscope.product_id == productVO.product_id){
                    skip = true
                }






                float x = leftMargin + col * labelWidth;
                float y = PDRectangle.LETTER.getHeight() - topMargin - (row + 1) * labelHeight;

                String barcodeData = "";
                if(skip){
                    barcodeData = productinscope.barcode
                } else {
                    // this only needs to be done one time for each product
                    barcodeData = barcodeHelper.generateBarcodeData(row, col, batchnumber, pagenumber);
                }

                // Example method to generate barcode data
                BufferedImage barcodeImage = imageGenerator.generateUPCABarcodeImage(barcodeData, labelWidth, labelHeight, col);

                // Convert BufferedImage to PDImageXObject
                PDImageXObject pdImage = LosslessFactory.createFromImage(document, barcodeImage);

                if(col == 4){
                    // Draw the barcode image on the PDF
                    contentStream.drawImage(pdImage, x + 8 as float, y, labelWidth, labelHeight);
                } else {
                    // Draw the barcode image on the PDF
                    contentStream.drawImage(pdImage, x, y, labelWidth, labelHeight);
                }


//                if(){
//
//                }

                //write method here to add barcode data to product in database
                addBarcodeToProduct(productVO, barcodeData);

                productlist.remove(productVO)
                productinscope = productVO // bind this so we only process barcodes for unique products
                skip = false
                toremove = toremove + 1
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
