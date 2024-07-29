package com.techvvs.inventory.printers.service

import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.printers.receipts.ReceiptGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.print.Doc
import javax.print.DocFlavor
import javax.print.DocPrintJob
import javax.print.PrintException
import javax.print.PrintService
import javax.print.PrintServiceLookup
import javax.print.SimpleDoc
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter


@Component
class MunbynITPP905Service {

    @Autowired
    ReceiptGenerator receiptGenerator

    public void print(String data) {


        data = generateReceipt()

        String printerName = "POS-80C";
//        String printerName = "Brother HL-L2300D series";

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



    public void printReceipt(TransactionVO transactionVO) {
        String printerName = "POS-80C";

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
            try {
                DocPrintJob job = selectedPrintService.createPrintJob();
                DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
                String receipt = receiptGenerator.generateTransactioonReciept(transactionVO);
                byte[] receiptBytes = receipt.getBytes(StandardCharsets.UTF_8);
//                byte[] cutCommand = [0x1D, 'V' as byte, 0x00] as byte[]
                byte[] cutCommand = [ 0x1D, 0x56, 0x00 ] as byte[]

                byte[] printData = new byte[receiptBytes.length + cutCommand.length];
                System.arraycopy(receiptBytes, 0, printData, 0, receiptBytes.length);
                System.arraycopy(cutCommand, 0, printData, receiptBytes.length, cutCommand.length);

                InputStream inputStream = new ByteArrayInputStream(printData);
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



}
