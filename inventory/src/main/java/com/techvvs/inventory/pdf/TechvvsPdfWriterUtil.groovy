package com.techvvs.inventory.pdf

import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.springframework.stereotype.Component

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class TechvvsPdfWriterUtil {



    void writeMetadataOnTopOfPage(
            PDPageContentStream contentStream,
            PDType0Font ttfFont,
            float leftMargin,
            float topMargin,
            int pagenumber,
            int entitynumber,
            String entityname

    ) {

        topMargin += 17 // add 10 to the topmargin to adjust it

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
                        + " | Batch #: " + entitynumber
                        + " | Batch Name: " + entityname
        );
        contentStream.endText();
    }

}
