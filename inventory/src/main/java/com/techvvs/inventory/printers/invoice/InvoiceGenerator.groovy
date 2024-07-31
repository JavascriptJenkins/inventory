package com.techvvs.inventory.printers.invoice

import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.PaymentVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.service.controllers.TransactionService
import com.techvvs.inventory.util.FormattingUtil
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class InvoiceGenerator {

    @Autowired
    TransactionService transactionService

    @Autowired
    CheckoutHelper checkoutHelper

    @Autowired
    FormattingUtil formattingUtil

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // Define the time formatter
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");



    String generateDefaultInvoice(TransactionVO transaction) {

        // this aggregates the products to their unique list
        transaction.product_list = getAggregatedTransactionProductList(transaction)


        def invoice = new StringBuilder()
        def customer = transaction.customervo

        invoice.append('Invoice Date: '+ LocalDateTime.now().format(formatter)+'  Time: '+LocalDateTime.now().format(timeFormatter)+'\n');
        invoice.append("========\n")
        invoice.append("\n")
        invoice.append("Invoice\n")
        invoice.append("========\n")
        invoice.append("Transaction ID: ${transaction.transactionid}\n")
        invoice.append('Original Transaction Date: '+transaction.createTimeStamp.format(formatter)+'\n');
        invoice.append('Original Transaction Time: '+transaction.createTimeStamp.format(timeFormatter)+'\n');


        invoice.append("Customer Details:\n")
        invoice.append("-----------------\n")
        invoice.append("Name: ${customer.name}\n")
        invoice.append("Email: ${customer.email}\n")
        invoice.append("Phone: ${customer.phone}\n\n")

        invoice.append("Products:\n")
        invoice.append("---------\n")
        for(ProductVO item : transaction.product_list) {
            invoice.append(formattingUtil.formatInvoiceItem(item.name, item.displayquantity, Double.valueOf(item.price)));
        }
        invoice.append("\n")

//        invoice.append("Payments:\n")
//        invoice.append("---------\n")
//        transaction.payment_set.each { payment ->
//            invoice.append("Payment ID: ${payment.paymentid}, Amount: ${payment.amount}, Method: ${payment.method}\n")
//        }
//        invoice.append("\n")

        invoice.append('Subtotal                  $'+transaction.total+'\n');
        invoice.append('Tax ('+transaction.taxpercentage+'%)                 $'+formattingUtil.calculateTaxAmount(transaction.total, transaction.taxpercentage)+'\n')
        invoice.append('Total                     $'+formattingUtil.calculateTotalWithTax(transaction.total, transaction.taxpercentage)+'\n')
        invoice.append("Paid: ${transaction.paid == null ? '0' : transaction.paid}\n")
        invoice.append("Notes: ${transaction.notes == null ? '' : transaction.notes}\n")
        invoice.append("Cashier: ${transaction.cashier == null ? '' : transaction.cashier}\n")
//        invoice.append("Processed: ${transaction.isprocessed == 1 ? 'Yes' : 'No'}\n")

        return invoice.toString()
    }

//// Example usage
//    def transaction = new TransactionVO(
//            transactionid: 12345,
//            transactionnumber: 67890,
//            createTimeStamp: LocalDateTime.now(),
//            customervo: new CustomerVO(name: "John Doe", email: "john.doe@example.com", phone: "123-456-7890"),
//            product_list: [new ProductVO(productid: 1, name: "Product 1", price: 100, quantity: 2)],
//            payment_set: [new PaymentVO(paymentid: 1, amount: 200, method: "Credit Card")],
//            total: 200,
//            paid: 200,
//            taxpercentage: 10,
//            notes: "Thank you for your purchase!",
//            cashier: "Jane Smith",
//            isprocessed: 1
//    )


    List<ProductVO> getAggregatedTransactionProductList(TransactionVO transactionVO){
        Set<String> seen = new HashSet<>()
        List<ProductVO> originallist = transactionVO.product_list
        List<ProductVO> newlist = new ArrayList<>()

        for(ProductVO productVO : originallist){

            if(seen.contains(productVO.barcode)){
                continue
            }
            seen.add(productVO.barcode)
            newlist.add(productVO)
        }

        return newlist


    }



}
