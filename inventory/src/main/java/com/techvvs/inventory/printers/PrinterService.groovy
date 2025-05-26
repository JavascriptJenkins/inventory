package com.techvvs.inventory.printers

import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.printers.service.BrotherHLL2300DSeriesSevice
import com.techvvs.inventory.printers.service.MunbynITPP905Service
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import org.springframework.beans.factory.annotation.Autowired


import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

import javax.transaction.Transactional;


@Service
class PrinterService {

    @Autowired
    BrotherHLL2300DSeriesSevice brotherHLL2300DSeriesSevice

    @Autowired
    MunbynITPP905Service munbynITPP905Service

    @Autowired
    CheckoutHelper checkoutHelper


    void printReceipt(TransactionVO transactionVO) {
      //  munbynITPP905Service.print(data);
        munbynITPP905Service.printReceipt(transactionVO);
    }

    @Transactional
    void printInvoice(TransactionVO transactionVO, boolean printinvoice, boolean onlysavetofilesystem) {

        List<ProductVO> unmodifiedlist = transactionVO.product_list
        brotherHLL2300DSeriesSevice.printInvoiceApachePDFBOX(transactionVO, printinvoice, onlysavetofilesystem)
        transactionVO.product_list = unmodifiedlist
    }





}
