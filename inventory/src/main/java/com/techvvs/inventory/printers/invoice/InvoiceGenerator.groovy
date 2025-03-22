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



    // todo: make sure this can handle printing out more than 1 page ......
    String generateDefaultInvoice(TransactionVO transaction) {

        // Aggregate products to their unique list
        transaction.product_list = getAggregatedTransactionProductList(transaction)

        def invoice = new StringBuilder()
        def customer = transaction.customervo

        // Invoice header
        invoice.append('Invoice Date: ' + LocalDateTime.now().format(formatter) + '  Time: ' + LocalDateTime.now().format(timeFormatter) + '\n')
        invoice.append("========================================\n")
        invoice.append("                 INVOICE                \n")
        invoice.append("========================================\n\n")

        // Transaction details
        invoice.append(String.format("Transaction ID: %-20s\n", transaction.transactionid))
        invoice.append('Original Transaction Date: ' + transaction.createTimeStamp.format(formatter) + '\n')
        invoice.append('Original Transaction Time: ' + transaction.createTimeStamp.format(timeFormatter) + '\n')
        invoice.append("========================================\n\n")

        // Customer details
        invoice.append("Customer Details:\n")
        invoice.append("-----------------\n")
        invoice.append(String.format("Name:    %-20s\n", customer.name))
        invoice.append(String.format("Email:   %-20s\n", customer.email))
        invoice.append(String.format("Phone:   %-20s\n", customer.phone))
        invoice.append("========================================\n\n")

        // Product details
        invoice.append("Products:\n")
        invoice.append("-----------------\n")
        invoice.append(String.format("%-20s %-10s %-10s\n", "Product", "Quantity", "Price"))
        invoice.append("--------------------------------------------------\n")
        transaction.product_list.each { product ->
            // Ensure price is treated as a double for formatting
            double priceValue = Double.parseDouble(product.price.toString())

            // Format the product name, quantity, and price
            invoice.append(String.format("%-20s %-10s \$%-10.2f\n", product.name, product.displayquantity, priceValue))
        }
        invoice.append("--------------------------------------------------\n\n")


        // Payment details
        if (transaction.payment_list.size() > 0) {
            invoice.append("Payments:\n")
            invoice.append("-----------------\n")
            invoice.append(String.format("%-15s %-10s %-10s %-20s\n", "Payment ID", "Amount", "Method", "Date"))
            invoice.append("--------------------------------------------------\n")
            transaction.payment_list.each { payment ->
                invoice.append(String.format("%-15s \$%-10.2f %-10s %s\n", payment.paymentid, payment.amountpaid, payment.paymenttype, payment.createTimeStamp))
            }
            invoice.append("--------------------------------------------------\n\n")
        }

        // Return details (if any)
        if (transaction.return_list != null && transaction.return_list.size() > 0) {
            invoice.append("Returns:\n")
            invoice.append("-----------------\n")
            invoice.append(String.format("%-30s %-10s %-20s\n", "Product Name", "Price", "Date"))
            invoice.append("--------------------------------------------------\n")
            transaction.return_list.each { returnvo ->
                invoice.append(String.format("%-30s \$%-10.2f %s\n", returnvo.product.name, returnvo.product.price, returnvo.createTimeStamp))
            }
            invoice.append("--------------------------------------------------\n\n")
        }


        if(transaction.discount_list.size()>0){
            invoice.append("Discounts:\n")
            invoice.append("-----------------\n")
            transaction.discount_list.each { discountvo ->
                invoice.append(String.format("Discount: %-20s \$%-10.2f\n", discountvo.name, discountvo.discountamount))
            }
        }


        invoice.append("-----------------\n")
        if (transaction.customercredit > 0) {
            invoice.append(String.format("%-30s \$%-10.2f\n", "Customer Credit", transaction.customercredit))
        }
        invoice.append(String.format("%-30s \$%-10.2f\n", "Subtotal", transaction.originalprice))

        invoice.append(String.format("Total (with tax)          \$%-10.2f\n", formattingUtil.calculateTotalWithTax(transaction.total, transaction.taxpercentage, 0.00))) // passing 0.00 in here cuz discount was already applied to the total
        invoice.append(String.format("Paid:                     \$%-10s\n", transaction.paid == null ? '0' : transaction.paid))
        invoice.append(String.format("Remaining Balance:        \$%-10.2f\n", Math.max(0.00, formattingUtil.calculateRemainingBalance(transaction.total, transaction.paid))))
        invoice.append("========================================\n")
        invoice.append(String.format("Notes: %s\n", transaction.notes == null ? '' : transaction.notes))
        invoice.append(String.format("Cashier: %s\n", transaction.cashier == null ? '' : transaction.cashier))
        invoice.append("========================================\n")

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
