package com.techvvs.inventory.qrcode.impl

import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.model.ProductVO
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.FileSystems
import java.nio.file.Path

@Component
class QrCodeGenerator {


    @Autowired
    QrImageGenerator qrImageGenerator

    static String QR_BATCH_DIR = "./uploads/qr/"

    // need to make a qr code generator that generates a QR for every barcode
    void generateQrcodes(String filenameExtension, int batchnumber, int pagenumber, Set<ProductVO> productset) throws IOException {

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

                int toremove = productset.size() - 1
                // iterate through productset to remove things
                ProductVO productVO = productset.getAt(toremove)

                float x = leftMargin + col * labelWidth;
                float y = PDRectangle.LETTER.getHeight() - topMargin - (row + 1) * labelHeight;

             //   String barcodeData = barcodeHelper.generateBarcodeData(row, col, filenameExtension, batchnumber, pagenumber);
                String qrcodeData = "https://qr.techvvs.io"

                // Example method to generate barcode data
                BufferedImage qrImage = qrImageGenerator.generateQrImage(qrcodeData, limitStringTo20Chars(productVO.name));

                // Convert BufferedImage to PDImageXObject
                PDImageXObject pdImage = LosslessFactory.createFromImage(document, qrImage);

                // Draw the barcode image on the PDF
                contentStream.drawImage(pdImage, x, y, labelWidth, labelHeight);

                //write method here to add barcode data to product in database
                // todo: add a qr link column to product in database
               // addBarcodeToProduct(productVO, barcodeData);

                productset.remove(productVO)
            }
        }





        // todo: add some functionality here so the user knows if barcodes are already generated or not
        // Close the content stream
        contentStream.close();

        String filename = pagenumber+"-"+filenameExtension+"-"+batchnumber
        // Save the PDF document
        document.save(new File(QR_BATCH_DIR+"upc_batch-"+filename+".pdf"));


    }

    ProductVO productinscope = new ProductVO(product_id: 0)
    boolean skip = false
    BufferedImage qrImageInScope = null
    // need to make a qr code generator that generates a QR for every barcode
    void generateQrcodesForAllItems(String filenameExtension, int batchnumber, int pagenumber, List<ProductVO> productlist) throws IOException {

        System.out.println("Generating qrcodes for " + filenameExtension + " | pagenumber: "+pagenumber);

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

                //write method here to add barcode data to product in database
                // todo: add a qr link column to product in database
                // addBarcodeToProduct(productVO, barcodeData);
                productinscope = productVO // bind this so we only process barcodes for unique products
                skip = false
                qrImageInScope = qrImage
                productlist.remove(productVO)
                toremove = toremove + 1
            }
        }





        // todo: add some functionality here so the user knows if barcodes are already generated or not
        // Close the content stream
        contentStream.close();

        String filename = pagenumber+"-"+filenameExtension+"-"+batchnumber
        // Save the PDF document
        document.save(new File(QR_BATCH_DIR+"upc_batch-"+filename+".pdf"));


    }

    public static String limitStringTo20Chars(String input) {
        if (input == null) {
            return null;
        }
        return input.length() > 20 ? input.substring(0, 20) : input;
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







//    void generateQRCodes(List<ProductVO> products) {
//        for (ProductVO product : products) {
//            String qrText = "https://northstar.techvvs.io/qr"
//            String filePath = QR_BATCH_DIR + product.getName() + ".png"
//            generateQRCodeImage(qrText, 350, 350, filePath)
//        }
//    }

    void generateQRCodeImage(String text, int width, int height, String filePath) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter()
        BitMatrix bitMatrix
        try {
            bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
        } catch (WriterException e) {
            e.printStackTrace()
            return
        }
        Path path = FileSystems.getDefault().getPath(filePath)
        try {
            ImageIO.write(MatrixToImageWriter.toBufferedImage(bitMatrix), "PNG", path.toFile())
        } catch (IOException e) {
            e.printStackTrace()
        }
    }






}
