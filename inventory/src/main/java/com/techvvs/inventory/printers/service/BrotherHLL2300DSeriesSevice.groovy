package com.techvvs.inventory.printers.service

import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.printers.invoice.InvoiceGenerator
import com.techvvs.inventory.util.TechvvsFileHelper
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.printing.PDFPageable
import org.apache.pdfbox.printing.PDFPrintable
import org.apache.pdfbox.printing.Scaling
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.print.Doc
import javax.print.DocFlavor
import javax.print.DocPrintJob
import javax.print.PrintException
import javax.print.PrintService
import javax.print.PrintServiceLookup
import javax.print.SimpleDoc
import javax.print.attribute.HashPrintRequestAttributeSet
import javax.print.attribute.PrintRequestAttributeSet
import javax.print.attribute.standard.Copies
import javax.print.attribute.standard.MediaSizeName
import javax.print.attribute.standard.Sides
import javax.print.event.PrintJobAdapter
import javax.print.event.PrintJobEvent
import java.awt.print.Pageable
import java.awt.print.PrinterJob
import java.nio.charset.StandardCharsets

import static org.apache.pdfbox.pdmodel.common.PDRectangle.*
import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4 as A4

@Component
class BrotherHLL2300DSeriesSevice {




    @Autowired
    InvoiceGenerator invoiceGenerator

    @Autowired
    TechvvsFileHelper techvvsFileHelper

    void printInvoice(TransactionVO transactionVO) {


        String data = invoiceGenerator.generateDefaultInvoice(transactionVO)
        String printerName = "Brother HL-L2300D series";

        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        PrintService selectedPrintService = null;

        for (PrintService printService : printServices) {
            if (printService.getName().equalsIgnoreCase(printerName)) {
                selectedPrintService = printService;
                break;
            }
        }

        if (selectedPrintService != null) {
            println("Selected printer: " + selectedPrintService.getName())
//            Arrays.stream(selectedPrintService.getSupportedDocFlavors()).forEach(f->System.out.println(f.getMediaType() + ":" + f.getMimeType() + ":" + f.getRepresentationClassName()));


            try {
                DocPrintJob job = selectedPrintService.createPrintJob();
//                DocFlavor flavor = DocFlavor.STRING.TEXT_PLAIN
//                DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;

//                InputStream inputStream = new ByteArrayInputStream(data.getBytes());

                DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE
                byte[] bytes = data.getBytes(StandardCharsets.UTF_8)
               // InputStream inputStream = new ByteArrayInputStream(data.getBytes());
                Doc doc = new SimpleDoc(bytes, flavor, null);


                // Adding print attributes
//                PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet()
//                printAttributes.add(new Copies(1))
//                printAttributes.add(Sides.ONE_SIDED)
//                printAttributes.add(MediaSizeName.ISO_A4)



                //document.silentPrint(printJob);


                // Add a print job listener to monitor job status
                job.addPrintJobListener(new PrintJobAdapter() {
                    @Override
                    void printJobCompleted(PrintJobEvent event) {
                        println("Print job completed.")
                    }

                    @Override
                    void printJobFailed(PrintJobEvent event) {
                        println("Print job failed.")
                    }

                    @Override
                    void printJobCanceled(PrintJobEvent event) {
                        println("Print job canceled.")
                    }

                    @Override
                    void printJobNoMoreEvents(PrintJobEvent event) {
                        println("No more events.")
                    }
                })



                job.print(doc, null);
                System.out.println("Print job sent to " + printerName);




            } catch (PrintException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Printer not found: " + printerName);
        }
    }

    public void print(String data) {
        String printerName = "Brother HL-L2300D series";

        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        PrintService selectedPrintService = null;

        for (PrintService printService : printServices) {
            if (printService.getName().equalsIgnoreCase(printerName)) {
                selectedPrintService = printService;
                break;
            }
        }

        if (selectedPrintService != null) {
            try {
                DocPrintJob job = selectedPrintService.createPrintJob();
                DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
                InputStream inputStream = new ByteArrayInputStream(data.getBytes());
                Doc doc = new SimpleDoc(inputStream, flavor, null);
                job.print(doc, null);
                System.out.println("Print job sent to " + printerName);
            } catch (PrintException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Printer not found: " + printerName);
        }
    }






    void printInvoiceApachePDFBOX(TransactionVO transactionVO) {
        String invoiceContent = invoiceGenerator.generateDefaultInvoice(transactionVO)
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream()

        generatePdf(invoiceContent, pdfOutputStream)

        byte[] pdfBytes = pdfOutputStream.toByteArray()
        ByteArrayInputStream pdfInputStream = new ByteArrayInputStream(pdfBytes)

        def document = PDDocument.load(pdfInputStream)

        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null)
        PrintService selectedPrintService = null
        String printerName = "Brother HL-L2300D series"

        // chatgpt - write a method to save the PDDocument to the file system
        techvvsFileHelper.saveInvoiceToFileSystem(document, transactionVO);

        for (PrintService printService : printServices) {
            println("Found printer: ${printService.getName()}")
            if (printService.getName().equalsIgnoreCase(printerName)) {
                selectedPrintService = printService
                break
            }
        }

        if (selectedPrintService != null) {
            println("Selected printer: " + selectedPrintService.getName())
            try {
                PrinterJob job = PrinterJob.getPrinterJob()
                job.setPageable(new PDFPageable(document))

                job.setPrintService(selectedPrintService)

                // Adding print attributes
                PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet()
                printAttributes.add(new Copies(1))
                printAttributes.add(Sides.ONE_SIDED)
                printAttributes.add(MediaSizeName.ISO_A4)

                job.print(printAttributes)
                println("Print job sent to " + printerName)

            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                document.close()
            }
        } else {
            println("Printer not found: " + printerName)
        }
    }



    static void generatePdf(String invoiceContent, OutputStream outputStream) {
        PDDocument document = new PDDocument()
        PDPage page = new PDPage(A4)
        document.addPage(page)

        PDPageContentStream contentStream = new PDPageContentStream(document, page)

        contentStream.setFont(PDType1Font.HELVETICA, 12)
        contentStream.beginText()
        contentStream.setLeading(14.5)
        contentStream.newLineAtOffset(25, 750)

        invoiceContent.eachLine { line ->
            contentStream.showText(line)
            contentStream.newLine()
        }

        contentStream.endText()
        contentStream.close()

        document.save(outputStream)
        document.close()
    }


}
