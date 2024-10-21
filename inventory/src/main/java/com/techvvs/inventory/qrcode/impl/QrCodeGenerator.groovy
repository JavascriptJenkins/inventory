package com.techvvs.inventory.qrcode.impl

import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.pdf.TechvvsPdfWriterUtil
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component


import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Component
class QrCodeGenerator {


    @Autowired
    QrImageGenerator qrImageGenerator

    static String QR_BATCH_DIR = "./uploads/qr/"

    @Autowired
    AppConstants appConstants

    @Autowired
    BarcodeHelper barcodeHelper

    @Autowired
    Environment env

    @Autowired
    TechvvsPdfWriterUtil techvvsPdfWriterUtil

    @Autowired
    QrCodeBuilder qrCodeBuilder

    // need to make a qr code generator that generates a QR for every barcode
    void generateQrcodes(String filenameExtension, int batchnumber, int pagenumber, Set<ProductVO> productset) throws IOException {

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
                if(productset.size() == 0){
                    break
                }

                int toremove = productset.size() - 1
                // iterate through productset to remove things
                ProductVO productVO = productset.getAt(toremove)

                float x = leftMargin + col * labelWidth;
                float y = PDRectangle.LETTER.getHeight() - topMargin - (row + 1) * labelHeight;

             //   String barcodeData = barcodeHelper.generateBarcodeData(row, col, filenameExtension, batchnumber, pagenumber);
                String qrcodeData = "https://qr.jenkins.codes"

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
    void generateQrcodesForAllItems(
            String filenameExtension,
                                    int batchnumber,
                                    int pagenumber,
                                    List<ProductVO> productlist,
                                    String batchname,
                                    PDDocument document


    ) throws IOException {

        System.out.println("Generating qrcodes for " + filenameExtension + " | pagenumber: "+pagenumber);

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

        // write label and metadata info on top of each page
        techvvsPdfWriterUtil.writeMetadataOnTopOfPage(contentStream, ttfFont, leftMargin, topMargin, pagenumber, batchnumber, batchname)


        // Generate and draw UPC-A barcodes
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 5; col++) {

                // this will prevent duplicate barcodes
                if(productlist.isEmpty()){
                    break
                }

                ProductVO productVO = productlist.remove(0)

                float x = leftMargin + col * labelWidth;
                float y = PDRectangle.LETTER.getHeight() - topMargin - (row + 1) * labelHeight;


                BufferedImage qrImage = null;

                String qrcodeData = ""
                boolean isdev1 = env.getProperty("spring.profiles.active").equals(appConstants.DEV_1)
                String baseqrdomain = env.getProperty("base.qr.domain")
                if(isdev1){
                    qrcodeData = appConstants.QR_CODE_PUBLIC_INFO_LINK_DEV1+productVO.getProduct_id()
                } else {
                    // prod
                    boolean isqrmode = env.getProperty("qr.mode.leafly").equals("true") // only do leafly search if this is true
                    if(isqrmode){
                        qrcodeData = appConstants.QR_CODE_URI_LEAFYLY+productVO.name
                    } else {
                        qrcodeData = baseqrdomain+appConstants.QR_CODE_URI_EXTENSION+productVO.getProduct_id()
                    }
                }

                qrImage = qrImageGenerator.generateQrImage(qrcodeData, limitStringTo20Chars(productVO.name));


                // Convert BufferedImage to PDImageXObject
                PDImageXObject pdImage = LosslessFactory.createFromImage(document, qrImage);

                // Adjust the x position for the last column
                if (col == 4) {
                    x += 8
                }

                y = barcodeHelper.adjustRowXYMargin(row, y)

                // Draw the image on the PDF
                contentStream.drawImage(pdImage, x, y, labelWidth, labelHeight);

                //write method here to add barcode data to product in database
                // todo: add a qr link column to product in database

                //productlist.remove(productVO)
            }
        }

        // Close the content stream
        contentStream.close();

        // create a directory with the batchnumber and /barcodes dir if it doesn't exist yet
        Files.createDirectories(Paths.get(appConstants.PARENT_LEVEL_DIR+batchnumber+appConstants.QR_ALL_DIR));

    }

    void generateQrcodesForMediaAllItems(
            String filenameExtension,
            int batchnumber,
            int pagenumber,
            List<ProductVO> productlist,
            String batchname,
            PDDocument document


    ) throws IOException {

        System.out.println("Generating media qrcodes for " + filenameExtension + " | pagenumber: "+pagenumber);

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

        // write label and metadata info on top of each page
        techvvsPdfWriterUtil.writeMetadataOnTopOfPage(contentStream, ttfFont, leftMargin, topMargin, pagenumber, batchnumber, batchname)


        // Generate and draw UPC-A barcodes
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 5; col++) {

                // this will prevent duplicate barcodes
                if(productlist.isEmpty()){
                    break
                }

                ProductVO productVO = productlist.remove(0)

                float x = leftMargin + col * labelWidth;
                float y = PDRectangle.LETTER.getHeight() - topMargin - (row + 1) * labelHeight;


                BufferedImage qrImage = null;

                String qrcodeData = ""
                boolean isdev1 = env.getProperty("spring.profiles.active").equals(appConstants.DEV_1)
                String baseqrdomain = env.getProperty("base.qr.domain")
                if(isdev1){
                    qrcodeData = appConstants.QR_CODE_PUBLIC_INFO_LINK_DEV1+productVO.getProduct_id()
                } else {
                    // prod
                    boolean ismediamode = env.getProperty("qr.mode.media").equals("true") // make the qr codes
                    if(ismediamode) {
                        // todo: route this to a media link
                        // // Path path = Paths.get(appConstants.UPLOAD_DIR_MEDIA+appConstants.UPLOAD_DIR_PRODUCT+"/"+productVO.getProduct_id()+"/"+fileName);
                        // we are binding productid,name,number - that way if the database gets blown out we can use other items if need be....

                        qrcodeData = qrCodeBuilder.buildMediaQrCodeForProduct(baseqrdomain, productVO)
                        //qrcodeData = baseqrdomain+"/file/privateqrmediadownload?productid="+productVO.product_id+"&name="+productVO.name+"&number="+productVO.productnumber+"&batchnumber="+batchnumber+"&batchname="+batchname
                    } else {
                        qrcodeData = baseqrdomain+appConstants.QR_CODE_URI_EXTENSION+productVO.getProduct_id()
                    }
                }

                qrImage = qrImageGenerator.generateQrImage(qrcodeData, limitStringTo20Chars(productVO.name));


                // Convert BufferedImage to PDImageXObject
                PDImageXObject pdImage = LosslessFactory.createFromImage(document, qrImage);

                // Adjust the x position for the last column
                if (col == 4) {
                    x += 8
                }

                y = barcodeHelper.adjustRowXYMargin(row, y)

                // Draw the image on the PDF
                contentStream.drawImage(pdImage, x, y, labelWidth, labelHeight);

                //write method here to add barcode data to product in database
                // todo: add a qr link column to product in database

                //productlist.remove(productVO)
            }
        }

        // Close the content stream
        contentStream.close();

        // create a directory with the batchnumber and /barcodes dir if it doesn't exist yet
        Files.createDirectories(Paths.get(appConstants.PARENT_LEVEL_DIR+batchnumber+appConstants.QR_MEDIA_DIR));

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
