package com.techvvs.inventory.util

import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
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



    //
    public static double calculateTotalWithTaxBasedOnTotalDiscountAmount(double total, double taxPercentage, double totalDiscountAmount) {
        // Validate input values
        if (total < 0 || taxPercentage < 0 || totalDiscountAmount < 0) {
            throw new IllegalArgumentException("Total, tax percentage, and total discount amount must be non-negative.");
        }

        // Apply the total discount amount directly
        double discountedTotal = total - totalDiscountAmount;

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

    /*In most cases, if the total amount of a transaction is discounted to zero, no sales tax is due.
    Sales tax is typically calculated based on the amount actually paid by the customer.
    If the total is zero after applying discounts, there’s no taxable amount remaining, so no sales tax would apply.
    */
    // we input the original price into this method regardless of if we have a 100% discount
    public static double calculateTotalWithTax(double total, double taxPercentage, double discountPercentage) {
        // Validate input values
        if (total < 0 || taxPercentage < 0 || discountPercentage < 0) {
            throw new IllegalArgumentException("Total, tax percentage, and discount percentage must be non-negative.");
        }

        // Apply the discount to the total
        double discount = total * (discountPercentage / 100.0);
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

    public static double calculateTotalWithTaxUsingDiscountAmount(double total, double taxPercentage, double discountAmount) {
        // Validate input values
        if (total < 0 || taxPercentage < 0 || discountAmount < 0) {
            throw new IllegalArgumentException("Total, tax percentage, and discount amount must be non-negative.");
        }

        // Apply the discount amount to the total
        double discountedTotal = total - discountAmount;

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


    // calculates total before tax but with discount
    public static double calculateTotalWithDiscountPercentage(double total, double discountPercentage) {
        // Validate input values
        if (total < 0 || discountPercentage < 0) {
            throw new IllegalArgumentException("Total and discount percentage must be non-negative.");
        }

        // Apply the discount to the total
        double discount = total * (discountPercentage / 100.0);
        double discountedTotal = total - discount;

        // Ensure the discounted total is not less than zero
        if (discountedTotal < 0) {
            discountedTotal = 0;
        }

        // Round to 2 decimal places for currency precision
        return Math.round(discountedTotal * 100.0) / 100.0;
    }

    static double calculateTotalDiscountPercentage(CartVO cartVO) {
        double discountPercentage = 0.0;

        if (cartVO.discount != null) {
            // Use discount percentage if available
            if (cartVO.discount.discountpercentage > 0) {
                discountPercentage = cartVO.discount.discountpercentage;
            } else if (cartVO.discount.discountamount > 0) {
                return 0; // return 0 because this discount is an amount and not a percentage
            }
        }
        return discountPercentage
    }

    // calculates total before tax but with discount
    public static double calculateTotalWithDiscountAmount(double total, double discountAmount) {
        // Validate input values
        if (total < 0 || discountAmount < 0) {
            throw new IllegalArgumentException("Total and discount amount must be non-negative.");
        }

        // Apply the discount to the total
        double discountedTotal = total - discountAmount;

        // Ensure the discounted total is not less than zero
        if (discountedTotal < 0) {
            discountedTotal = 0;
        }

        // Round to 2 decimal places for currency precision
        return Math.round(discountedTotal * 100.0) / 100.0;
    }


    // calculates total before tax but with discount
    public static double calculateTotalWithDiscountAmountPerUnitByProductType(
            double total,
            double discountAmount,
            ProductTypeVO productTypeVO,
            List<ProductVO> product_list
    ) {

        Double totaldiscounttoapply = 0.00
        Double perunitdiscount = discountAmount

        for(ProductVO productVO : product_list){
            // check every product in the list, if it matches the producttype then increment the discount
            if(productVO.producttypeid.producttypeid == productTypeVO.producttypeid){
                totaldiscounttoapply += perunitdiscount
            }
        }

        return (total - totaldiscounttoapply) // apply the per unit discount to the total

    }


    // calculates total before tax but with discount
    public static double calculateTotalWithTaxWithDiscountAmountPerUnitByProductType(
            double totalwithtax,
            double discountAmount,
            ProductTypeVO productTypeVO,
            List<ProductVO> product_list
    ) {

        Double totaldiscounttoapply = 0.00
        Double perunitdiscount = discountAmount

        for(ProductVO productVO : product_list){
            // check every product in the list, if it matches the producttype then increment the discount
            if(productVO.producttypeid.producttypeid == productTypeVO.producttypeid){
                totaldiscounttoapply += perunitdiscount
            }
        }

        return (totalwithtax - totaldiscounttoapply) // apply the per unit discount to the total

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


    String getDateTimeForFileSystem() {

        def now = LocalDateTime.now()
        def formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss")
        def formattedDateTime = now.format(formatter)

        return formattedDateTime
    }


}
