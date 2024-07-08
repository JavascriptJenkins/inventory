package com.techvvs.inventory.barcode.impl

import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.ProductVO
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.krysalis.barcode4j.impl.upcean.UPCABean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.parameters.P
import org.springframework.stereotype.Component

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory

import java.awt.Color
import java.awt.Graphics2D;
import java.awt.image.BufferedImage

@Component
class BarcodeGenerator {

    @Autowired
    ProductRepo productRepo

    List producthashes = new ArrayList()
    String currentBarcodeData = ""


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


            // this iterator stops barcodes from being printed

                // Generate and draw UPC-A barcodes
                for (int row = 0; row < 10; row++) {
                    for (int col = 0; col < 5; col++) {


                        // this will prevent duplicate barcodes
                        if(productset.size() == 0){
                            break
                        }


                        float x = leftMargin + col * labelWidth;
                        float y = PDRectangle.LETTER.getHeight() - topMargin - (row + 1) * labelHeight;

                        String barcodeData = generateBarcodeData(row, col, filenameExtension, batchnumber, pagenumber);



                        // Example method to generate barcode data
                        BufferedImage barcodeImage = generateUPCABarcodeImage(barcodeData, labelWidth, labelHeight);

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


    // todo: this can handle up to 950 products in a single batch.  Need to write logic to handle more than 950
    // passing the batchnumber into here for first part of upc barcode
    private static String generateBarcodeData(int row, int col, String filenameExtension, int batchnumber, int pagenumber) {
        // Example method to generate unique barcode data based on row and column
        // note: this baseData can only be 6 characters long - batchnumbers are 7 characters so we are removing the last char
        String baseData = removeLast2Character(String.valueOf(batchnumber))+String.valueOf(pagenumber); // Base data for the barcode
        String rowColData = String.format("%02d%02d", row, col); // Row and column indices padded with leading zeros

        // Combine base data with row and column data
        String barcodeData = baseData + rowColData;


        // Calculate and append the checksum
        int checksum = calculateUPCAChecksum(barcodeData);
        barcodeData += checksum;

        return barcodeData;
    }


    // Method to calculate the checksum for UPC-A barcode data
    private static int calculateUPCAChecksum(String data) {
        int sum = 0;
        for (int i = 0; i < data.length(); i++) {
            int digit = Character.getNumericValue(data.charAt(i));
            if ((i + data.length()) % 2 == 0) {
                sum += digit * 3;
            } else {
                sum += digit;
            }
        }
        return (10 - (sum % 10)) % 10;
    }

//    private static BufferedImage generateUPCABarcodeImage(String barcodeData, float width, float height) {
//        UPCABean bean = new UPCABean();
//        bean.setModuleWidth(0.2); // Adjust module width as needed
//        bean.setBarHeight(10f); // Adjust bar height as needed
//        bean.setQuietZone(5f)
////        bean.setVerticalQuietZone(10f)
//
////        BitmapCanvasProvider canvas = new BitmapCanvasProvider(width, height, BufferedImage.TYPE_BYTE_BINARY, false, 0);
//        BitmapCanvasProvider canvas = new BitmapCanvasProvider(260, BufferedImage.TYPE_BYTE_BINARY, false, 0);
//
//        bean.generateBarcode(canvas, barcodeData);
//
//        return canvas.getBufferedImage();
//    }

    private static BufferedImage generateUPCABarcodeImage(String barcodeData, float width, float height) {
        UPCABean bean = new UPCABean();
        bean.setModuleWidth(0.2); // Adjust module width as needed
        bean.setBarHeight(10f);   // Adjust bar height as needed
        bean.setQuietZone(5f);    // Adjust quiet zone as needed

        // Set up the canvas provider
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(260, BufferedImage.TYPE_BYTE_BINARY, false, 0);

        // Generate the barcode
        bean.generateBarcode(canvas, barcodeData);

        // Get the generated barcode image
        BufferedImage barcodeImage = canvas.getBufferedImage();

        // Define the bottom margin (1/16th of an inch in points)
        float bottomMargin = 2.0f / 16 * 72; // 1/16th of an inch in points
        int marginPixels = (int) (bottomMargin * 260 / 72);

        // Create a new image with the bottom margin
        int widthWithMargin = barcodeImage.getWidth();
        int heightWithMargin = barcodeImage.getHeight() + marginPixels
        BufferedImage imageWithMargin = new BufferedImage(widthWithMargin, heightWithMargin, BufferedImage.TYPE_BYTE_BINARY);

        // Draw the original barcode image onto the new image
        Graphics2D g2d = imageWithMargin.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, barcodeImage.getHeight(), widthWithMargin, marginPixels);
        g2d.drawImage(barcodeImage, 0, 0, null);
        g2d.dispose();

        return imageWithMargin;
    }


    static String removeLast2Character(String str) {
        if (str == null || str.isEmpty()) {
            return str; // Return the original string if it's null or empty.
        }
        return str.substring(0, str.length() - 2); // Use substring to remove the last character.
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
