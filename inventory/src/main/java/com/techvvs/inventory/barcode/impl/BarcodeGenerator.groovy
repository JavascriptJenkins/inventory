package com.techvvs.inventory.barcode.impl

import com.techvvs.inventory.constants.AppConstants
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



    ProductVO productinscope = new ProductVO(product_id: 0)
    boolean skip = false
    //
    void generateBarcodesForAllItems(
                                     int entitynumber,
                                     int pagenumber,
                                     List<ProductVO> productlist,
                                     String entityname,
                                    PDDocument document

    ) throws IOException {


        System.out.println("Generating barcodes for " + entityname + " | pagenumber: "+pagenumber);

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

        // Write text on the top of page - Batchnumber, date, page number, etc
        writeMetadataOnTopOfPage(contentStream, ttfFont, leftMargin, topMargin, pagenumber, entitynumber, entityname)

        generateAndDrawUpcaBarcodes(
                productlist,
                leftMargin,
                labelWidth,
                topMargin,
                labelHeight,
                entitynumber,
                pagenumber,
                document,
                contentStream
        )

        // Close the content stream
        contentStream.close();

    }


    void generateAndDrawUpcaBarcodes(List<ProductVO> productlist,
                                     float leftMargin,
                                     float labelWidth,
                                     float topMargin,
                                     float labelHeight,
                                     int entitynumber,
                                     int pagenumber,
                                     PDDocument document,
                                     PDPageContentStream contentStream) {
        // Initialize variables to track processed products
        ProductVO productInScope = null
        boolean skip = false

        int k = 0
        // Generate and draw UPC-A barcodes
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 5; col++) {


                // Stop if there are no more products to process
                if (productlist.isEmpty()) {
                    break
                }

                // todo: this is problem.  it is removing the first item from list instead of the last item
                // Get the last product in the list and remove it from the list
                int size = productlist.size()
//                ProductVO productVO = productlist.remove(size - 1)
                ProductVO productVO = productlist.remove(0)

                // Check if the current product is the same as the one in scope
                if (productInScope != null && productInScope.product_id == productVO.product_id) {
                    skip = true
                }

                // Calculate the position for the barcode
                float x = leftMargin + col * labelWidth
                float y = PDRectangle.LETTER.getHeight() - topMargin - (row + 1) * labelHeight

                String barcodeData
                if (skip) {
                    // Use the existing barcode for duplicate products
                    barcodeData = productInScope.barcode
                } else {
                    // Generate new barcode data for the product
                    barcodeData = barcodeHelper.generateBarcodeData(row, col, entitynumber, pagenumber)
                }

                // Generate the barcode image
                BufferedImage barcodeImage = imageGenerator.generateUPCABarcodeImage(barcodeData, labelWidth, labelHeight, col)

                // Convert BufferedImage to PDImageXObject
                PDImageXObject pdImage = LosslessFactory.createFromImage(document, barcodeImage)

                // Adjust the x position for the last column
                if (col == 4) {
                    x += 8
                }

                y = barcodeHelper.adjustRowXYMargin(row, y)

                // Draw the barcode image on the PDF
                contentStream.drawImage(pdImage, x, y, labelWidth, labelHeight)

                // Add the barcode data to the product in the database
                addBarcodeToProduct(productVO, barcodeData)

                // Update the product in scope and reset the skip flag
                productInScope = productVO
                skip = false
                k++
            }
        }
    }



    void writeMetadataOnTopOfPage(
            PDPageContentStream contentStream,
                            PDType0Font ttfFont,
                            float leftMargin,
                            float topMargin,
                            int pagenumber,
                            int entitynumber,
                            String entityname

    ) {

        topMargin += 17 // add 10 to the topmargin to adjust it

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
                        + " | Batch #: " + entitynumber
                        + " | Batch Name: " + entityname
        );
        contentStream.endText();
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
