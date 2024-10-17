package com.techvvs.inventory.labels.impl

import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.barcode.impl.ImageGenerator
import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.qrcode.impl.QrImageGenerator
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
//import com.itextpdf.text.*
//import com.itextpdf.text.pdf.*
//import com.itextpdf.text.Image



import java.awt.image.BufferedImage
import java.nio.Buffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class LabelPrintingGenerator {


    @Autowired
    AppConstants appConstants

    @Autowired
    BarcodeHelper barcodeHelper

    @Autowired
    ImageGenerator imageGenerator

    @Autowired
    Environment env

    @Autowired
    QrImageGenerator qrImageGenerator

    String STATIC_LABEL_BATCH_DIR = "./uploads/staticlabels/"

    int amountoflabels = 50


    void generate50AdhocLabels(
            int entitynumber,
            int pagenumber,
            MenuVO menuvo,
            PDDocument document,
            String entity
    ){
        System.out.println("Generating label sheet for " + menuvo.adhoc_label1 + " | pagenumber: "+pagenumber);

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
        writeMetadataOnTopOfPage(contentStream, ttfFont, leftMargin, topMargin, pagenumber, entitynumber, menuvo.adhoc_label1, entity)

        generateAdhocLabels(
                menuvo,
                leftMargin,
                labelWidth,
                topMargin,
                labelHeight,
                contentStream,
                ttfFont
        )

        // Close the content stream
        contentStream.close();

    }


    void generateEpson4by6point5Label(
            int entitynumber,
            int pagenumber,
            BatchVO batchVO,
            ProductVO productVO,
            PDDocument document
    ){

        // create a pdf sheet
        PDPage page = new PDPage(appConstants.FOUR_BY_SIX_POINT_FIVE); // 4" x 6.5"
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page)
        // Define margins and layout parameters
        float topMargin = (6.0f / 16.0f) * 72; // 6/16 inches to points
        float bottomMargin = (1.0f / 16.0f) * 72; // 6/16 inches to points
        float leftMargin = 0.25f * 72; // 0.25" in points
        float rightMargin = 0.25f * 72; // 0.25" in points

        // dont need to print a header on the label (only product info)



        generateEpson6by4Point5Page(productVO,document,contentStream, page)



    }

    void generateEpson6by4Point5Page(
            ProductVO product,
            PDDocument document,
            PDPageContentStream contentStream,
            PDPage page
    ) throws IOException {

        // Layout adjustments for centering
        float pageWidth = 4 * 72;  // 4 inches in points
        float pageHeight = 6.5f * 72;  // 6.5 inches in points
        float margin = 10f;  // Minimal margin for spacing

        // Calculate available space and element sizes
        float barcodeWidth = 0.4f * pageWidth;  // 40% of the page width
        float barcodeHeight = barcodeWidth * 0.75f;  // Maintain aspect ratio

        float qrCodeSize = 0.3f * pageWidth;  // 30% of the page width

        float totalElementWidth = barcodeWidth + margin + qrCodeSize;
        float startX = (pageWidth - totalElementWidth) / 2;  // Center horizontally
        float centerY = pageHeight / 2;  // Center vertically

        // Calculate Y positions for barcode and QR code
        float barcodeY = centerY + barcodeHeight / 2;  // Top alignment for barcode
        float qrY = barcodeY - qrCodeSize;  // Align QR code to the barcode's bottom

        // Load font
        PDType0Font ttfFont = PDType0Font.load(document, new File("./uploads/font/Oswald-VariableFont_wght.ttf"));
        contentStream.setFont(ttfFont, 12f);  // Adjusted font size

        // Draw barcode image
        BufferedImage barcodeImage = imageGenerator.generateUPCABarcodeImage(product.barcode);
        PDImageXObject barcodePdImage = LosslessFactory.createFromImage(document, barcodeImage);
        contentStream.drawImage(barcodePdImage, startX, barcodeY - barcodeHeight as float, barcodeWidth, barcodeHeight);

        // Draw QR code image to the right of the barcode
        float qrX = startX + barcodeWidth + margin;  // Place QR code after barcode
        PDImageXObject qrPdImage = generateQrImageforEpson(product, document);
        contentStream.drawImage(qrPdImage, qrX, qrY, qrCodeSize, qrCodeSize);

        // Draw product name below the images, centered horizontally
        List<String> wrappedText = wrapText(product.name, 35);  // Wrap text for better readability

        float textY = qrY - 15f;  // Start position for text below QR code
        contentStream.beginText();
        contentStream.setFont(ttfFont, 12f);  // Set the font size for the product name
        for (String line : wrappedText) {
            float textWidth = ttfFont.getStringWidth(line) / 1000 * 12f;  // Calculate the width of the text
            float textStartX = (pageWidth - textWidth) / 2;  // Center the text horizontally
            contentStream.newLineAtOffset(textStartX, textY);
            contentStream.showText(line);
            textY -= (12f + 3f);  // Adjust for the next line with line spacing
        }
        contentStream.endText();

        contentStream.close();
    }

// Helper function to wrap text by character limit
    private List<String> wrapText(String text, int maxLineLength) {
        List<String> lines = new ArrayList<>();
        int index = 0;
        while (index < text.length()) {
            int end = Math.min(index + maxLineLength, text.length());
            lines.add(text.substring(index, end));
            index = end;
        }
        return lines;
    }

// Generate QR code image for the product
    PDImageXObject generateQrImageforEpson(ProductVO productVO, PDDocument document) throws IOException {
        String qrcodeData = "";
        boolean isDev1 = env.getProperty("spring.profiles.active").equals(appConstants.DEV_1);
        String baseQrDomain = env.getProperty("base.qr.domain");

        if (isDev1) {
            qrcodeData = appConstants.QR_CODE_PUBLIC_INFO_LINK_DEV1 + productVO.getProduct_id();
        } else {
            boolean isMediaMode = env.getProperty("qr.mode.media").equals("true");
            if (isMediaMode) {
                qrcodeData = baseQrDomain + "/file/privateqrmediadownload?productid="+productVO.getProduct_id()+"&name="+productVO.name+"&number="+productVO.productnumber;
            } else {
                qrcodeData = baseQrDomain + appConstants.QR_CODE_URI_EXTENSION + productVO.getProduct_id();
            }
        }

        BufferedImage qrImage = qrImageGenerator.generateQrImageWithCustomSizes(
                qrcodeData, limitStringTo25Chars(productVO.name), 200, 56);
        return LosslessFactory.createFromImage(document, qrImage);
    }

// Helper function to limit a string to 25 characters
    private String limitStringTo25Chars(String text) {
        return text.length() > 25 ? text.substring(0, 25) : text;
    }



    void generateBarcodeManifest(
                                int entitynumber,
                                int pagenumber,
                                BatchVO batchVO,
                                List<ProductVO> listofTen,
                                 PDDocument document
    ){




        // create a pdf sheet
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
        writeMetadataOnTopOfPage(contentStream, ttfFont, leftMargin, topMargin, pagenumber, entitynumber, batchVO.name, "Productlist")

        // cycle through all products in the product_set to print each one
        generateProductManifestPage(listofTen, document,contentStream, page)


    }

    void generateProductManifestPage(List<ProductVO> productList,
                                     PDDocument document,
                                     PDPageContentStream contentStream,
                                     PDPage page
    ) {

        // Margins and spacing
        float margin = 50
        float barcodeHeight = 72  // 1 inch in points
        float barcodeWidth = 108  // 1.5 inches in points
        float nameFontSize = 12
        float lineSpacing = 5  // Extra space between barcode and name

        // Start coordinates for the left and right columns
        float leftColumnX = margin
        float rightColumnX = page.getMediaBox().getWidth() / 2 + margin / 2
        float startY = page.getMediaBox().getHeight() - margin

        // Example of using a TrueType font
        PDType0Font ttfFont = PDType0Font.load(document, new File("./uploads/font/Oswald-VariableFont_wght.ttf"));
        contentStream.setFont(ttfFont, nameFontSize)

        // Draw 5 labels on the left and 5 on the right side
        for (int i = 0; i < productList.size(); i++) {
            ProductVO product = productList.get(i)
            float x = (i < 5) ? leftColumnX : rightColumnX
            float y = startY - (i % 5) * (barcodeHeight + nameFontSize + lineSpacing + margin)


            // Generate the barcode image
            BufferedImage barcodeImage = imageGenerator.generateUPCABarcodeImage(product.barcode)
            // Convert BufferedImage to PDImageXObject
            org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject pdImage = LosslessFactory.createFromImage(document, barcodeImage)
            // Draw barcode image
            contentStream.drawImage(pdImage, x, y - barcodeHeight as float, barcodeWidth, barcodeHeight)
            // Draw product name below the barcode, truncate to 20 chars
            String productName = product.name.take(20)
            contentStream.beginText()
            contentStream.newLineAtOffset(x, y - barcodeHeight - nameFontSize - lineSpacing as float)
            contentStream.showText(productName)
            contentStream.endText()
        }
        contentStream.close()

    }



    void generateAdhocLabels(
            MenuVO menuvo,
            float leftMargin,
            float labelWidth,
            float topMargin,
            float labelHeight,
            PDPageContentStream contentStream,
            PDType0Font font) { // added font to measure and draw text

        int k = 0;
        // Generate adhoclabel text (will run 50 times)
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 5; col++) {

                // Calculate the position for the label text
                float x = leftMargin + col * labelWidth;
                float y = PDRectangle.LETTER.getHeight() - topMargin - (row + 1) * labelHeight;



                // Adjust the x position for these columns
                if (col == 0) {
                    x -= 5;
                }

                // Adjust the x position for these columns
                if (col == 1) {
                    x -= 2;
                }

                // Adjust the x position for these columns
                if (col == 2) {
                    x += 6;
                }

                // Adjust the x position for these columns
                if (col == 3) {
                    x += 12;
                }

                // Adjust the x position for these columns
                if (col == 4) {
                    x += 19;
                }

                y = barcodeHelper.adjustRowXYMargin(row, y);

                // Now write the adhoclabel text below the barcode within the label width
                float textX = x;
                float textY = y + 50;

                // Split adhoclabel into lines that fit within labelWidth
//                List<String> wrappedText = splitTextToFitWidth(adhoclabel, font, labelWidth);
                List<String> wrappedText = [menuvo.adhoc_label1, menuvo.adhoc_label2, menuvo.adhoc_label3]

                // Draw each line of the wrapped text
                for (String line : wrappedText) {
                    contentStream.beginText();
                    contentStream.setFont(font, 10); // Set the font size (adjust if necessary)
                    contentStream.newLineAtOffset(textX, textY);
                    contentStream.showText(line);
                    contentStream.endText();

                    // Move the y position up for the next line of text
                    textY -= 12; // Adjust line spacing
                }

                k++; // Not actually using this but keeping it if we need it
            }
        }
    }

    List<String> splitTextToFitWidth(String text, int maxCharsPerLine) {
        List<String> lines = new ArrayList<String>();
        int textLength = text.length();

        // Split the text into lines with maxCharsPerLine characters
        for (int i = 0; i < textLength; i += maxCharsPerLine) {
            int end = Math.min(i + maxCharsPerLine, textLength);
            lines.add(text.substring(i, end));
        }

        return lines;
    }

// Helper method to split text into lines that fit within a given width
//    List<String> splitTextToFitWidth(String text, PDType0Font font, float maxWidth) throws IOException {
//        List<String> lines = new ArrayList<>();
//        StringBuilder currentLine = new StringBuilder();
//        float currentWidth = 0;
//
//        for (String word : text.split(" ")) {
//            float wordWidth = font.getStringWidth(word) / 1000 * 10; // Assume font size 10 here
//            if (currentWidth + wordWidth > maxWidth) {
//                // If the word doesn't fit, add the current line to the list and start a new line
//                lines.add(currentLine.toString());
//                currentLine = new StringBuilder(word);
//                currentWidth = wordWidth;
//            } else {
//                // Otherwise, add the word to the current line
//                if (currentLine.length() > 0) {
//                    currentLine.append(" ");
//                }
//                currentLine.append(word);
//                currentWidth += wordWidth;
//            }
//        }
//
//        // Add the last line
//        if (currentLine.length() > 0) {
//            lines.add(currentLine.toString());
//        }
//
//        return lines;
//    }



    void writeMetadataOnTopOfPage(
            PDPageContentStream contentStream,
            PDType0Font ttfFont,
            float leftMargin,
            float topMargin,
            int pagenumber,
            int entitynumber,
            String entityname,
            String entity // ex Package / Crate / Product

    ) {

        topMargin += 10

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
                        + " | " + entity + " #: " + entitynumber
                        + " | " + entity + " Name: " + chopRightPaddedString(entityname)
        );
        contentStream.endText();
    }

    def chopRightPaddedString(String input) {
        // Split by whitespace and filter out empty strings, then join with a single space
        return input.split(/\s+/).findAll { it }.join(' ')
    }



    void generate50StaticWeightLabels(){



        System.out.println("Generating 50 static weight labels");

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


                if(amountoflabels == 0){
                    break
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


                amountoflabels = amountoflabels - 1
            }
        }





        // todo: add some functionality here so the user knows if barcodes are already generated or not
        // Close the content stream
        contentStream.close();

        String filename = "static-weight-labels"
        // Save the PDF document
        document.save(new File(STATIC_LABEL_BATCH_DIR+filename+".pdf"));

    }



//    def createLabelPDF(String text, PDImageXObject image1, PDImageXObject image2, String outputFilePath) {
//        // 2-1/8" by 4" label, converted to points (72 points = 1 inch)
//        def labelWidth = 2.125 * 72  // 153 points
//        def labelHeight = 4 * 72     // 288 points
//        def sectionWidth = labelWidth / 3  // Each section gets 1/3 of the width
//
//        // Create a new PDF document with specified size
//        Document document = new Document(new Rectangle(labelWidth, labelHeight))
//        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFilePath))
//        document.open()
//
//        // Add the first image (left 1/3)
//        Image img1 = Image.getInstance(image1.getImageBytes())
//        img1.scaleToFit((float) sectionWidth, (float) labelHeight)  // Scale to fit within the 1/3 section
//        img1.setAbsolutePosition(0f, 0f)  // Left-most section
//        document.add(img1)
//
//        // Add the second image (right 1/3)
//        Image img2 = Image.getInstance(image2.getImageBytes())
//        img2.scaleToFit((float) sectionWidth, (float) labelHeight)  // Scale to fit within the 1/3 section
//        img2.setAbsolutePosition((float) (2 * sectionWidth), 0f)  // Right-most section
//        document.add(img2)
//
//        // Add the text in the middle 1/3 section
//        BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED)
//        float fontSize = 72 // Start with a large font size and decrease until it fits
//        boolean textFits = false
//
//        while (!textFits && fontSize > 1) {
//            Font font = new Font(baseFont, fontSize)
//            Paragraph paragraph = new Paragraph(text, font)
//
//            // Measure the width of the text
//            PdfContentByte cb = writer.getDirectContent()
//            float textWidth = baseFont.getWidthPoint(text, fontSize)
//
//            // Check if the text fits within the middle 1/3 section width
//            if (textWidth <= sectionWidth - 10) { // 10 points margin
//                textFits = true
//            } else {
//                fontSize -= 1 // Decrease font size and try again
//            }
//        }
//
//        // Create the text paragraph with the final font size and center it vertically
//        Font finalFont = new Font(baseFont, fontSize)
//        Paragraph finalParagraph = new Paragraph(text, finalFont)
//        finalParagraph.setAlignment(Element.ALIGN_CENTER)
//
//        // Set the text's absolute position in the middle 1/3 section
//        float textXPosition = (float) sectionWidth  // Start at the 1/3 mark
//        float textYPosition = (labelHeight - fontSize) / 2  // Vertically center
//        PdfContentByte cb = writer.getDirectContent()
//        cb.beginText()
//        cb.setFontAndSize(baseFont, fontSize)
//        cb.showTextAligned(Element.ALIGN_CENTER, text, (float) (textXPosition + sectionWidth / 2), textYPosition, 0f)
//        cb.endText()
//
//        // Close the document
//        document.close()
//
//        println "Label PDF created at: $outputFilePath"
//    }

// Mock implementation of PDImageXObject to simulate its behavior
//    class PDImageXObject {
//        byte[] imageBytes
//
//        PDImageXObject(String imagePath) {
//            imageBytes = new File(imagePath).bytes
//        }
//
//        byte[] getImageBytes() {
//            return imageBytes
//        }
//    }





}
