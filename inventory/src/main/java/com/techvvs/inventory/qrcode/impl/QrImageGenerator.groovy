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
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.nio.file.FileSystems
import java.nio.file.Path


@Component
class QrImageGenerator {

    public static BufferedImage generateQrImage(String qrcodeData, float width, float height) {

        // Set up the canvas provider
//        BitmapCanvasProvider canvas = new BitmapCanvasProvider(260, BufferedImage.TYPE_BYTE_BINARY, false, 0);

        // Get the generated barcode image
        BufferedImage qrcodeImage = createQRCodeImage(qrcodeData, 350, 350)

        // Define the bottom margin (1/16th of an inch in points)
        float bottomMargin = 2.0f / 16 * 72; // 1/16th of an inch in points
        int marginPixels = (int) (bottomMargin * 260 / 72);

        // Create a new image with the bottom margin
        int widthWithMargin = qrcodeImage.getWidth();
        int heightWithMargin = qrcodeImage.getHeight() + marginPixels
        BufferedImage imageWithMargin = new BufferedImage(widthWithMargin, heightWithMargin, BufferedImage.TYPE_BYTE_BINARY);

        // Draw the original qrcode image onto the new image
        Graphics2D g2d = imageWithMargin.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, qrcodeImage.getHeight(), widthWithMargin, marginPixels);
        g2d.drawImage(qrcodeImage, 0, 0, null);
        g2d.dispose();

        return imageWithMargin;
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
