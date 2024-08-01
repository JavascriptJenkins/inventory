package com.techvvs.inventory.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.text.DecimalFormat

@Component
class FormattingUtil {

    // this returns a string after formatting but may be useful in the future
    DecimalFormat decimalFormat00 = new DecimalFormat("#.00");



    static String formatReceiptItem(String name, int quantity, Double price) {
        return String.format("%-20s %5d %8.2f\n", name, quantity, price);
    }

    static String formatInvoiceItem(String name, int quantity, Double price) {
        return String.format("%-30s %5d %10.2f\n", name, quantity, price);
    }
    public static double calculateTotalWithTax(double total, int taxPercentage) {
        // Validate input values
        if (total < 0 || taxPercentage < 0) {
            throw new IllegalArgumentException("Total and tax percentage must be non-negative.");
        }

        // Calculate tax amount
        double taxAmount = total * (taxPercentage / 100.0);

        // Calculate total with tax
        double totalWithTax = total + taxAmount;

        // Round to 2 decimal places for currency precision
        return Math.round(totalWithTax * 100.0) / 100.0;
    }


//    public static int calculateTaxAmount(int total, int taxPercentage) {
//        return (int) Math.round(total * (taxPercentage / 100.0));
//    }

    public static double calculateTaxAmount(double total, int taxPercentage) {
        // Validate input values
        if (total < 0 || taxPercentage < 0) {
            throw new IllegalArgumentException("Total and tax percentage must be non-negative.");
        }

        // Calculate tax amount
        double taxAmount = total * (taxPercentage / 100.0);
        return Math.round(taxAmount * 100.0) / 100.0; // rounding to 2 decimal places
    }


}
