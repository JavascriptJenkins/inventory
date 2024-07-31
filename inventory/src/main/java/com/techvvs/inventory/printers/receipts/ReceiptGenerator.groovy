package com.techvvs.inventory.printers.receipts

import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.service.controllers.TransactionService
import com.techvvs.inventory.util.FormattingUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.text.DecimalFormat
import java.time.format.DateTimeFormatter

@Component
class ReceiptGenerator {


    @Autowired
    TransactionService transactionService

    @Autowired
    FormattingUtil formattingUtil

    String generateTransactioonReciept(TransactionVO transactionVO) {
        return generateReceipt(transactionVO)
    }


    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // Define the time formatter
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

    String generateReceipt(TransactionVO transactionVO) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("          NORTHSTAR\n");
        receipt.append("       69th st\n");
        receipt.append("       Suite 420\n");
        receipt.append("      Minneapolis, MN, 55407\n");
        receipt.append("      Tel: (123) 456-7890\n");
        receipt.append("\n");
        receipt.append('Transaction #: '+transactionVO.transactionid+'\n');
        receipt.append('Date: '+transactionVO.updateTimeStamp.format(formatter)+'  Time: '+transactionVO.updateTimeStamp.format(timeFormatter)+'\n');
        receipt.append('--------------------------------\n');
        receipt.append(String.format("%-20s %5s %8s\n", "Item", "Qty", "Price"));
//        receipt.append('Item                Qty    Price\n');
        receipt.append('--------------------------------\n');

        transactionVO.cart.product_cart_list = transactionService.getAggregatedCartProductList(transactionVO.cart)

        for(ProductVO item : transactionVO.cart.product_cart_list) {
            receipt.append(formattingUtil.formatReceiptItem(item.name, item.displayquantity, Double.valueOf(item.price)));
//            receipt.append(item.name+'               '+item.quantity+'    $'+item.price+'\n');
        }
        receipt.append("--------------------------------\n");
        receipt.append('Subtotal                  $'+transactionVO.total+'\n');

        receipt.append('Tax ('+transactionVO.taxpercentage+'%)                 $'+formattingUtil.calculateTaxAmount(transactionVO.total, transactionVO.taxpercentage)+'\n');

        receipt.append('Total                     $'+formattingUtil.calculateTotalWithTax(transactionVO.total, transactionVO.taxpercentage)+'\n');
        receipt.append('\n');
        receipt.append('Thank you for shopping!\n');
        receipt.append('     www.northstar.com\n');
        receipt.append('                         \n');
        receipt.append('  INVENTORY VVS          \n');
        receipt.append('                         \n');
        receipt.append('                         \n');
        receipt.append('                         \n');
        receipt.append('                         \n');
        receipt.append("\n\n\n"); // Feed to cut
        return receipt.toString();
    }

//    private String formatReceiptItem(String name, int quantity, Double price) {
//        return String.format("%-20s %5d %8.2f\n", name, quantity, price);
//    }
//
//    public static int calculateTotalWithTax(int total, int taxPercentage) {
//        double taxAmount = total * (taxPercentage / 100.0);
//        return (int) Math.round(total + taxAmount);
//    }
//
//    public static int calculateTaxAmount(int total, int taxPercentage) {
//        return (int) Math.round(total * (taxPercentage / 100.0));
//    }

    String generateSampleReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("          STORE NAME\n");
        receipt.append("       Address Line 1\n");
        receipt.append("       Address Line 2\n");
        receipt.append("      City, State, ZIP\n");
        receipt.append("      Tel: (123) 456-7890\n");
        receipt.append("\n");
        receipt.append("Receipt #: 12345\n");
        receipt.append("Date: 01/01/2024  Time: 12:34 PM\n");
        receipt.append("--------------------------------\n");
        receipt.append("Item                Qty    Price\n");
        receipt.append("--------------------------------\n");
        receipt.append('Item 1               2    $10.00\n');
        receipt.append('Item 2               1    $20.00\n');
        receipt.append("--------------------------------\n");
        receipt.append('Subtotal                  $30.00\n');
        receipt.append('Tax (10%)                 $3.00\n');
        receipt.append('Total                     $33.00\n');
        receipt.append("\n");
        receipt.append("Thank you for shopping!\n");
        receipt.append("     www.storewebsite.com\n");
        receipt.append("                         \n");
        receipt.append("                         \n");
        receipt.append("                         \n");
        receipt.append("\n\n\n"); // Feed to cut
        return receipt.toString();
    }


}
