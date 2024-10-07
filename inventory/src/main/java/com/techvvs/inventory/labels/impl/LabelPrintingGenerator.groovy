package com.techvvs.inventory.labels.impl

import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.ProductVO
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.awt.image.BufferedImage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class LabelPrintingGenerator {



    @Autowired
    BarcodeHelper barcodeHelper

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
        List<String> lines = new ArrayList<>();
        int textLength = text.length();

        // Split the text into lines with maxCharsPerLine characters
        for (int i = 0; i < textLength; i += maxCharsPerLine) {
            int end = Math.min(i + maxCharsPerLine, textLength);
            lines.add(text.substring(i, end));
        }

        return lines;
    }

// Helper method to split text into lines that fit within a given width
    List<String> splitTextToFitWidth(String text, PDType0Font font, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        float currentWidth = 0;

        for (String word : text.split(" ")) {
            float wordWidth = font.getStringWidth(word) / 1000 * 10; // Assume font size 10 here
            if (currentWidth + wordWidth > maxWidth) {
                // If the word doesn't fit, add the current line to the list and start a new line
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
                currentWidth = wordWidth;
            } else {
                // Otherwise, add the word to the current line
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
                currentWidth += wordWidth;
            }
        }

        // Add the last line
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }



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




}
