package com.techvvs.inventory.barcode

import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.krysalis.barcode4j.impl.upcean.UPCABean
import org.springframework.stereotype.Component

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.krysalis.barcode4j.impl.upcean.UPCEANBean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import javax.imageio.ImageIO;


import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Component
class BarcodeGenerator {


        void generateBarcodes(String filenameExtension) throws IOException {



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

                    // Generate and draw UPC-A barcodes
                    for (int row = 0; row < 10; row++) {
                        for (int col = 0; col < 5; col++) {
                            float x = leftMargin + col * labelWidth;
                            float y = PDRectangle.LETTER.getHeight() - topMargin - (row + 1) * labelHeight;

                            String barcodeData = generateBarcodeData(row, col, filenameExtension); // Example method to generate barcode data
                            BufferedImage barcodeImage = generateUPCABarcodeImage(barcodeData, labelWidth, labelHeight);

                            // Convert BufferedImage to PDImageXObject
                            PDImageXObject pdImage = LosslessFactory.createFromImage(document, barcodeImage);

                            // Draw the barcode image on the PDF
                            contentStream.drawImage(pdImage, x, y, labelWidth, labelHeight);


                        }
                    }


            //                // Close the content stream
            contentStream.close();
                // Save the PDF document
                document.save(new File("multiple_upc_barcodes+"+filenameExtension+".pdf"));





//
//            // Create a new PDF document
//            PDDocument document = new PDDocument();
//
//
//                // Create a new page
//                PDPage page = new PDPage(PDRectangle.LETTER); // 8.5" x 11"
//                document.addPage(page);
//
//                // Create content stream for the page
//                PDPageContentStream contentStream = new PDPageContentStream(document, page);
//
//                // Generate UPC barcode
//                BufferedImage barcodeImage = generateBarcodeImage("123456789012", LABEL_WIDTH, LABEL_HEIGHT);
//
//            // Convert BufferedImage to PDImageXObject
//            PDImageXObject pdImage = LosslessFactory.createFromImage(document, barcodeImage);
//
//
//                // Draw the barcode image on the page
//                contentStream.drawImage(pdImage, 0.floatValue(), 0.floatValue(), LABEL_WIDTH, LABEL_HEIGHT);
//
//
//                // Close the content stream
//                contentStream.close();


            // Save the PDF document
//            document.save(new File("upc_barcodes.pdf"));
//            document.close();
        }



    private static String generateBarcodeData(int row, int col, String filenameExtension) {
        // Example method to generate unique barcode data based on row and column
        String baseData = "12345"+filenameExtension; // Base data for the barcode
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






        private BufferedImage generateBarcodeImage(String barcodeData, float width, float height) {

            UPCABean barcodeGenerator = new UPCABean();
//
//            barcodeGenerator.setModuleWidth(width)
//            barcodeGenerator.setBarHeight(height)

            barcodeGenerator.setModuleWidth(0.3)
            barcodeGenerator.setBarHeight(10f)

//            UPCEANBean bean = new UPCBean();
//            bean.setModuleWidth(0.3);
//            bean.doQuietZone(true);
//            bean.setQuietZone(10);

            //    public BitmapCanvasProvider(OutputStream out, String mime, int resolution, int imageType, boolean antiAlias, int orientation) {
            //    public BitmapCanvasProvider(int resolution, int imageType, boolean antiAlias, int orientation) {
            BitmapCanvasProvider canvas = new BitmapCanvasProvider(160, BufferedImage.TYPE_BYTE_BINARY, false, 0);

            barcodeGenerator.generateBarcode(canvas, barcodeData);

            return canvas.getBufferedImage();
        }




}
