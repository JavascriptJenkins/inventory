package com.techvvs.inventory.service.invoice

import com.techvvs.inventory.model.TransactionVO
import org.springframework.stereotype.Component

@Component
class InvoiceService {



    void emailOrTextInvoice(TransactionVO transactionVO) {

        brotherHLL2300DSeriesSevice.printInvoiceApachePDFBOX(transactionVO)
    }



}
