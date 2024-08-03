package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.PaymentRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.PaymentVO
import com.techvvs.inventory.model.TransactionVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.LocalDateTime

@Component
class PaymentService {

    @Autowired
    TransactionRepo transactionRepo

    @Autowired
    CustomerRepo customerRepo

    @Autowired
    PaymentRepo paymentRepo

    TransactionVO submitPaymentForTransaction(int transactionid, int customerid, PaymentVO paymentVO) {

        CustomerVO existingCustomer = customerRepo.findById(customerid).get()
        if(existingCustomer != null){
            paymentVO.customer = existingCustomer
        }

        TransactionVO existingTransaction = transactionRepo.findById(transactionid).get()
        if(existingTransaction != null){
            paymentVO.transaction = existingTransaction

            paymentVO = savePayment(paymentVO)

            // add up the amount paid onto the transaction
            existingTransaction.paid = existingTransaction.paid == null ? 0 : existingTransaction.paid

            if(existingTransaction.paid + paymentVO.amountpaid > existingTransaction.totalwithtax){
                System.out.println("this should never happen, but the amount paid is greater than the total")
                System.out.println("rejecting the transaction")
                return existingTransaction
            }

            existingTransaction.paid = existingTransaction.paid + paymentVO.amountpaid

            // check for null and create if needed
            existingTransaction.payment_list == null ? existingTransaction.payment_list = new ArrayList<>() : existingTransaction.payment_list
            existingTransaction.payment_list.add(paymentVO)

            existingTransaction = transactionRepo.save(existingTransaction)

            return existingTransaction
        }

        return new TransactionVO()
    }

    PaymentVO savePayment(PaymentVO paymentVO){

        paymentVO.createTimeStamp = LocalDateTime.now()
        paymentVO.updateTimeStamp = LocalDateTime.now()
        paymentVO.notes = "Payment for transaction " + paymentVO.transaction.transactionid
        paymentVO.paymenttype = "CASH"

        paymentVO = paymentRepo.save(paymentVO)
        return paymentVO
    }





}
