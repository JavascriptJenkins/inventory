package com.techvvs.inventory.barcode.impl

import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.model.ProductVO
import org.krysalis.barcode4j.impl.upcean.UPCABean
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.imageio.ImageIO
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.nio.file.FileSystems
import java.nio.file.Path


@Component
class ImageGenerator {

    @Autowired
    AppConstants appConstants

    static BufferedImage generateUPCABarcodeImage(String barcodeData) {
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

    BufferedImage generateSidewaysUPCABarcodeImage(String barcodeData, String labeltype) {
        UPCABean bean = new UPCABean();

        switch (labeltype) {
            case appConstants.filenameprefix_dymno_28mmx89mm:
                    bean.setModuleWidth(0.4); // Adjust module width as needed
                    bean.setBarHeight(15f);   // Adjust bar height as needed
                    bean.setQuietZone(5f);    // Adjust quiet zone as needed
                break;
            default:
                bean.setModuleWidth(0.2); // Adjust module width as needed
                bean.setBarHeight(10f);   // Adjust bar height as needed
                bean.setQuietZone(5f);    // Adjust quiet zone as needed
        }


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
        int heightWithMargin = barcodeImage.getHeight() + marginPixels;
        BufferedImage imageWithMargin = new BufferedImage(widthWithMargin, heightWithMargin, BufferedImage.TYPE_BYTE_BINARY);

        // Draw the original barcode image onto the new image with the margin
        Graphics2D g2d = imageWithMargin.createGraphics();
        g2d.setColor(Color.WHITE);  // Set background color to white
        g2d.fillRect(0, 0, widthWithMargin, heightWithMargin);  // Fill entire background
        g2d.setColor(Color.BLACK);  // Set color for barcode drawing
        g2d.drawImage(barcodeImage, 0, 0, null);  // Draw barcode at the top
        g2d.dispose();

        // Rotate the image with the margin by 90 degrees
        int rotatedWidth = heightWithMargin;
        int rotatedHeight = widthWithMargin;
        BufferedImage rotatedImage = new BufferedImage(rotatedWidth, rotatedHeight, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2dRotated = rotatedImage.createGraphics();

        // Set the background for the rotated image to white
        g2dRotated.setColor(Color.WHITE);
        g2dRotated.fillRect(0, 0, rotatedWidth, rotatedHeight);

        // Apply rotation transformation around the center
        g2dRotated.rotate(Math.toRadians(270), rotatedWidth / 2.0, rotatedHeight / 2.0);

        // Translate to align the original image within the rotated canvas
        g2dRotated.translate((rotatedWidth - widthWithMargin) / 2.0, (rotatedHeight - heightWithMargin) / 2.0);

        // Draw the original image with margin onto the rotated graphics context
        g2dRotated.drawImage(imageWithMargin, 0, 0, null);
        g2dRotated.dispose();

        return rotatedImage;
    }





//    static void generateQRCodes(List<ProductVO> products) {
//        for (ProductVO product : products) {
//            String qrText = "https://northstar.techvvs.io/qr"
//            String filePath = "qr_codes/" + product.getName() + ".png"
//            generateQRCodeImage(qrText, 350, 350, filePath)
//        }
//    }
//
//    static void generateQRCodeImage(String text, int width, int height, String filePath) {
//        QRCodeWriter qrCodeWriter = new QRCodeWriter()
//        BitMatrix bitMatrix
//        try {
//            bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
//        } catch (WriterException e) {
//            e.printStackTrace()
//            return
//        }
//        Path path = FileSystems.getDefault().getPath(filePath)
//        try {
//            ImageIO.write(MatrixToImageWriter.toBufferedImage(bitMatrix), "PNG", path.toFile())
//        } catch (IOException e) {
//            e.printStackTrace()
//        }
//    }



}
