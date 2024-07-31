package com.techvvs.inventory.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FormattingUtil {


    static String formatReceiptItem(String name, int quantity, Double price) {
        return String.format("%-20s %5d %8.2f\n", name, quantity, price);
    }

    static String formatInvoiceItem(String name, int quantity, Double price) {
        return String.format("%-30s %5d %10.2f\n", name, quantity, price);
    }
    public static int calculateTotalWithTax(int total, int taxPercentage) {
        double taxAmount = total * (taxPercentage / 100.0);
        return (int) Math.round(total + taxAmount);
    }

    public static int calculateTaxAmount(int total, int taxPercentage) {
        return (int) Math.round(total * (taxPercentage / 100.0));
    }



}
