package com.techvvs.inventory.util

import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.DiscountVO
import spock.lang.Specification
import spock.lang.Unroll

class FormattingUtilTest extends Specification {

    @Unroll
    def "calculateRemainingBalance should return correct remaining balance"() {
        expect:
        FormattingUtil.calculateRemainingBalance(total, paid) == expected

        where:
        total | paid | expected
        100.0 | 40.0 | 60.0
        50.0  | 20.0 | 30.0
        0.0   | 0.0  | 0.0
    }

//    @Unroll
//    def "formatReceiptItem should format item correctly"() {
//        expect:
//        FormattingUtil.formatReceiptItem(name, quantity, price) == expected
//
//        where:
//        name        | quantity | price | expected
//        "Item1"     | 2        | 10.5  | "Item1                2    10.50\n"
//        "LongName"  | 1        | 5.25  | "LongName             1     5.25\n"
//        "Another"   | 0        | 0.0   | "Another              0     0.00\n"
//    }
//
//    @Unroll
//    def "formatInvoiceItem should format invoice item correctly"() {
//        expect:
//        FormattingUtil.formatInvoiceItem(name, quantity, price) == expected
//
//        where:
//        name        | quantity | price | expected
//        "Item1"     | 2        | 10.5  | "Item1                          2      10.50\n"
//        "LongName"  | 1        | 5.25  | "LongName                       1       5.25\n"
//        "Another"   | 0        | 0.0   | "Another                        0       0.00\n"
//    }

    @Unroll
    def "calculateTotalWithTax should calculate total with tax and discount percentage"() {
        when:
        def result = FormattingUtil.calculateTotalWithTax(total, taxPercentage, discountPercentage)

        then:
        result == expected

        where:
        total | taxPercentage | discountPercentage | expected
        100.0 | 10.0          | 10.0               | 99.0
        200.0 | 5.0           | 50.0               | 105.0
        0.0   | 10.0          | 10.0               | 0.0
    }

    @Unroll
    def "calculateTotalWithTaxUsingDiscountAmount should calculate total with tax and discount amount"() {
        when:
        def result = FormattingUtil.calculateTotalWithTaxUsingDiscountAmount(total, taxPercentage, discountAmount)

        then:
        result == expected

        where:
        total | taxPercentage | discountAmount | expected
        100.0 | 10.0          | 10.0           | 99.0
        200.0 | 5.0           | 50.0           | 157.5
        0.0   | 10.0          | 10.0           | 0.0
    }

    @Unroll
    def "calculateTotalWithDiscountPercentage should calculate total with discount percentage"() {
        when:
        def result = FormattingUtil.calculateTotalWithDiscountPercentage(total, discountPercentage)

        then:
        result == expected

        where:
        total | discountPercentage | expected
        100.0 | 10.0               | 90.0
        200.0 | 50.0               | 100.0
        0.0   | 10.0               | 0.0
    }

    def "calculateTotalDiscountPercentage should return discount percentage if available"() {
        given:
        def cartVO = new CartVO(discount: new DiscountVO(discountpercentage: discountPercentage, discountamount: discountAmount))

        expect:
        FormattingUtil.calculateTotalDiscountPercentage(cartVO) == expected

        where:
        discountPercentage | discountAmount | expected
        10.0               | 0.0            | 10.0
        0.0                | 20.0           | 0.0
        15.0               | 0.0            | 15.0
    }

    @Unroll
    def "calculateTotalWithDiscountAmount should calculate total with discount amount"() {
        when:
        def result = FormattingUtil.calculateTotalWithDiscountAmount(total, discountAmount)

        then:
        result == expected

        where:
        total | discountAmount | expected
        100.0 | 10.0           | 90.0
        200.0 | 50.0           | 150.0
        0.0   | 10.0           | 0.0
    }

    @Unroll
    def "calculateTaxAmount should calculate tax based on total, tax percentage, and discount"() {
        when:
        def result = FormattingUtil.calculateTaxAmount(total, taxPercentage, discount)

        then:
        result == expected

        where:
        total | taxPercentage | discount | expected
        100.0 | 10.0          | 10.0     | 9.0
        200.0 | 5.0           | 50.0     | 7.5
        0.0   | 10.0          | 10.0     | 0.0
    }

    def "getDateTimeForFileSystem should return formatted date and time"() {
        given:
        def formattingUtil = new FormattingUtil()

        when:
        def dateTime = formattingUtil.getDateTimeForFileSystem()

        then:
        dateTime ==~ /\d{2}-\d{2}-\d{4}-\d{2}-\d{2}-\d{2}/ // Matches format dd-MM-yyyy-HH-mm-ss
    }
}
