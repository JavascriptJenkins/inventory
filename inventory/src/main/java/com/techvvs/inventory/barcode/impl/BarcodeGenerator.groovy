package com.techvvs.inventory.barcode.impl

import com.techvvs.inventory.model.ProductVO
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.krysalis.barcode4j.impl.upcean.UPCABean
import org.springframework.stereotype.Component

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;

import java.awt.image.BufferedImage

@Component
class BarcodeGenerator {

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
                    float topMargin = 0.5f * 72; // 0.5" in points
                    float bottomMargin = 0.5f * 72; // 0.5" in points
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

                        String barcodeData = generateBarcodeData(row, col, filenameExtension, batchnumber);



                        // Example method to generate barcode data
                        BufferedImage barcodeImage = generateUPCABarcodeImage(barcodeData, labelWidth, labelHeight);

                        // Convert BufferedImage to PDImageXObject
                        PDImageXObject pdImage = LosslessFactory.createFromImage(document, barcodeImage);

                        // Draw the barcode image on the PDF
                        contentStream.drawImage(pdImage, x, y, labelWidth, labelHeight);

                        int toremove = productset.size() - 1
                        // iterate through productset to remove things
                        ProductVO productVO = productset.getAt(toremove)
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



    // passing the batchnumber into here for first part of upc barcode
    private static String generateBarcodeData(int row, int col, String filenameExtension, int batchnumber) {
        // Example method to generate unique barcode data based on row and column
        // note: this baseData can only be 6 characters long - batchnumbers are 7 characters so we are removing the last char
        String baseData = removeLastCharacter(String.valueOf(batchnumber)); // Base data for the barcode
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

    private static BufferedImage generateUPCABarcodeImage(String barcodeData, float width, float height) {
        UPCABean bean = new UPCABean();
        bean.setModuleWidth(0.2); // Adjust module width as needed
        bean.setBarHeight(10f); // Adjust bar height as needed
        bean.setQuietZone(5f)
//        bean.setVerticalQuietZone(10f)

//        BitmapCanvasProvider canvas = new BitmapCanvasProvider(width, height, BufferedImage.TYPE_BYTE_BINARY, false, 0);
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(260, BufferedImage.TYPE_BYTE_BINARY, false, 0);

        bean.generateBarcode(canvas, barcodeData);

        return canvas.getBufferedImage();
    }


    public static String removeLastCharacter(String str) {
        if (str == null || str.isEmpty()) {
            return str; // Return the original string if it's null or empty.
        }
        return str.substring(0, str.length() - 1); // Use substring to remove the last character.
    }




}
