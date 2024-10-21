package com.techvvs.inventory.qrcode.impl

import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import org.krysalis.barcode4j.impl.upcean.UPCABean
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider
import org.springframework.stereotype.Component

import javax.imageio.ImageIO
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.nio.file.FileSystems
import java.nio.file.Path


@Component
class QrImageGenerator {



    public static BufferedImage generateQrImage(String qrcodeData, String text) throws WriterException {
        int qrSize = 175; // QR code size
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrcodeData, BarcodeFormat.QR_CODE, qrSize, qrSize);

        // Convert BitMatrix to BufferedImage
        BufferedImage qrcodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // Define the total size of the final image
        int finalWidth = qrSize * 2; // Make the final image twice the width of the QR code
        int finalHeight = qrSize * 2; // Keep the same height as the QR code

        // Create a new image with the final dimensions
        BufferedImage imageWithText = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_RGB);

        // Draw the original QR code image onto the new image
        Graphics2D g2d = imageWithText.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, finalWidth, finalHeight); // Fill the entire image with white

        // Draw the QR code in the top-left corner, taking up half the space
        g2d.drawImage(qrcodeImage, 70, 0, null);

        // Set font and color for the text
        g2d.setFont(new Font("Arial", Font.PLAIN, 28));
        g2d.setColor(Color.BLACK);

        // Calculate the position for the text to be centered below the QR code
        int textX = 70; // Adjust the X position as needed to center text in the right half
        int textY = qrSize; // Adjust the Y position as needed

        // Draw the text on the right side of the image
        g2d.drawString(text, textX, textY+30);

        g2d.dispose();

        return imageWithText;
    }

    public static BufferedImage generateQrImageWithCustomSizes(
            String qrcodeData,
            String text,
            int qrSize,
            int fontSize

    ) throws WriterException {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrcodeData, BarcodeFormat.QR_CODE, qrSize, qrSize);

        // Convert BitMatrix to BufferedImage
        BufferedImage qrcodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // Define the total size of the final image
        int finalWidth = qrSize * 2; // Make the final image twice the width of the QR code
        int finalHeight = qrSize * 2; // Keep the same height as the QR code

        // Create a new image with the final dimensions
        BufferedImage imageWithText = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_RGB);

        // Draw the original QR code image onto the new image
        Graphics2D g2d = imageWithText.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, finalWidth, finalHeight); // Fill the entire image with white

        // Draw the QR code
        g2d.drawImage(qrcodeImage, 0, 0, null);

        // Set font and color for the text
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSize));
        g2d.setColor(Color.BLACK);

        // Calculate the position for the text to be centered below the QR code
        int textX = 70; // Adjust the X position as needed to center text in the right half
        int textY = qrSize; // Adjust the Y position as needed

        // Draw the text on the right side of the image
        //g2d.drawString(text, textX, textY);

        g2d.dispose();

        return imageWithText;
    }




    static BufferedImage createQRCodeImage(String text, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter()
        BitMatrix bitMatrix
        try {
            bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
        } catch (WriterException e) {
            e.printStackTrace()
            return null
        }
        return MatrixToImageWriter.toBufferedImage(bitMatrix)
    }








}
