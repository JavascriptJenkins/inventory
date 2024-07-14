package com.techvvs.inventory.printers.service

import org.springframework.stereotype.Component

import javax.print.Doc
import javax.print.DocFlavor
import javax.print.DocPrintJob
import javax.print.PrintException
import javax.print.PrintService
import javax.print.PrintServiceLookup
import javax.print.SimpleDoc


@Component
class MunbynITPP905Service {

    public void print(String data) {
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


}
