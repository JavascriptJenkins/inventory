package com.techvvs.inventory.util

import com.techvvs.inventory.model.DiscountVO
import com.techvvs.inventory.model.TransactionVO
import org.springframework.stereotype.Component

import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class FormattingUtil {

    // this returns a string after formatting but may be useful in the future
    DecimalFormat decimalFormat00 = new DecimalFormat("#.00");


    static calculateRemainingBalance(double total, double paid) {
        return total - paid;
    }

    static String formatReceiptItem(String name, int quantity, Double price) {
        return String.format("%-20s %5d %8.2f\n", name, quantity, price);
    }

    static String formatInvoiceItem(String name, int quantity, Double price) {
        return String.format("%-30s %5d %10.2f\n", name, quantity, price);
    }
    public static double calculateTotalWithTax(double total, double taxPercentage, double discount) {
        // Validate input values
        if (total < 0 || taxPercentage < 0 || discount < 0) {
            throw new IllegalArgumentException("Total, tax percentage, and discount must be non-negative.");
        }

        // Apply the discount to the total
        double discountedTotal = total - discount;

        // Ensure the discounted total is not less than zero
        if (discountedTotal < 0) {
            discountedTotal = 0;
        }

        // Calculate the tax amount on the discounted total
        double taxAmount = discountedTotal * (taxPercentage / 100.0);

        // Calculate the total with tax
        double totalWithTax = discountedTotal + taxAmount;

        // Round to 2 decimal places for currency precision
        return Math.round(totalWithTax * 100.0) / 100.0;
    }


//    public static int calculateTaxAmount(int total, int taxPercentage) {
//        return (int) Math.round(total * (taxPercentage / 100.0));
//    }

    public static double calculateTaxAmount(double total, double taxPercentage, double discount) {
        // Validate input values
        if (total < 0 || taxPercentage < 0 || discount < 0) {
            throw new IllegalArgumentException("Total, tax percentage, and discount must be non-negative.");
        }

        // Apply the discount to the total
        double discountedTotal = total - discount;

        // Ensure the discounted total is not less than zero
        if (discountedTotal < 0) {
            discountedTotal = 0;
        }

        // Calculate the tax amount on the discounted total
        double taxAmount = discountedTotal * (taxPercentage / 100.0);

        // Round to 2 decimal places
        return Math.round(taxAmount * 100.0) / 100.0;
    }



    double getTotalDiscount(List<DiscountVO> discount_list) {
        return discount_list ?
                discount_list.collect { item ->
                    item?.discountamount ?: 0.0
                }.sum() : 0.0 as double
    }

    String getDateTimeForFileSystem() {

        def now = LocalDateTime.now()
        def formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss")
        def formattedDateTime = now.format(formatter)

        return formattedDateTime
    }


}
