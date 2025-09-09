package com.techvvs.inventory.accounting.service

import com.techvvs.inventory.jparepo.PaymentRepo
import com.techvvs.inventory.jparepo.TenantRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/*
This class will be used to fetch payments and transactions and other accounting related data and aggregate it for the
AccountingProcessor to process.
And other things like:
exporting data to quickbooks and other accounting software like xero
generating tax documents
*
*/
@Component
class AccountingService {


    @Autowired
    TransactionRepo transactionRepo

    @Autowired
    PaymentRepo paymentRepo

    @Autowired
    TenantRepo tenantRepo





}
