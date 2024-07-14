package com.techvvs.inventory.printers.util

import org.springframework.stereotype.Component

import javax.print.DocFlavor
import javax.print.PrintService
import javax.print.PrintServiceLookup

@Component
class PrinterUtil {

    //
    public void checkPrinterFlavors(String data) {

        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

        for (PrintService printService : printServices) {
            System.out.println("Printer: " + printService.getName());
            DocFlavor[] flavors = printService.getSupportedDocFlavors();
            for (DocFlavor flavor : flavors) {
                System.out.println("Supported flavor: " + flavor);
            }
            System.out.println("----------------------------------");
        }


    }

//
//        String printerName = "POS-80C";
//
//        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
//
//        PrintService selectedPrintService = null;
//
//        for (PrintService printService : printServices) {
//            if (printService.getName().equalsIgnoreCase(printerName)) {
//                selectedPrintService = printService;
//                break;
//            }
//        }
//
//        if (selectedPrintService != null) {
//            try {
//                DocPrintJob job = selectedPrintService.createPrintJob();
//                DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
//                byte[] bytes = data.getBytes();
//                Doc doc = new SimpleDoc(bytes, flavor, null);
//                job.print(doc, null);
//                System.out.println("Print job sent to " + printerName);
//            } catch (PrintException e) {
//                e.printStackTrace();
//            }
//        } else {
//            System.out.println("Printer not found: " + printerName);
//        }

    private DocFlavor findSupportedFlavor(PrintService printService) {
        DocFlavor[] supportedFlavors = printService.getSupportedDocFlavors();
        for (DocFlavor flavor : supportedFlavors) {
            if (flavor.equals(DocFlavor.BYTE_ARRAY.AUTOSENSE)) {
                return flavor;
            }
        }
        return null;
    }



}
