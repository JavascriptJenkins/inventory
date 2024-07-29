package com.techvvs.inventory.printers

import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.printers.service.BrotherHLL2300DSeriesSevice
import com.techvvs.inventory.printers.service.MunbynITPP905Service
import org.springframework.beans.factory.annotation.Autowired

import javax.print.PrintService;
import javax.print.PrintServiceLookup
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.SimpleDoc;
import javax.print.PrintException;
import org.springframework.stereotype.Component;


@Component
class PrinterService {

    @Autowired
    BrotherHLL2300DSeriesSevice brotherHLL2300DSeriesSevice

    @Autowired
    MunbynITPP905Service munbynITPP905Service



    void printReceipt(TransactionVO transactionVO) {
      //  munbynITPP905Service.print(data);
        munbynITPP905Service.printReceipt(transactionVO);
    }

    void printInvoice(TransactionVO transactionVO) {

        brotherHLL2300DSeriesSevice.printInvoiceApachePDFBOX(transactionVO)
    }





}
