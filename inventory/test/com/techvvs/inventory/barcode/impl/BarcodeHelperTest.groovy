package com.techvvs.inventory.barcode.impl

import com.techvvs.inventory.barcode.service.BarcodeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

class BarcodeHelperTest extends Specification {

    // mock deps
    BarcodeService barcodeService

    //create real version of what you are testing
    BarcodeHelper barcodeHelper


    def setup(){
        // create mock inputs to the class
        barcodeService = Mock(BarcodeService)


        // create the real class
        barcodeHelper = new BarcodeHelper(
                barcodeService: barcodeService
        )

    }


    def "Generate Single Barcode"() {
        given:
        int row = 1
        int col = 1
        int batchnumber = 4873985
        int pagenumber = 1


        when:
        String result = barcodeHelper.generateBarcodeData(row, col, batchnumber, pagenumber)

        then:
        result == "087398101014"
        0*_._
    }

    def "Generate Barcode with page 10"() {
        given:
        int row = 1
        int col = 1
        int batchnumber = 4873985
        int pagenumber = 10

        when:
        String result = barcodeHelper.generateBarcodeData(row, col, batchnumber, pagenumber)

        then:
        result == "087391001014"
        0*_._
    }


    def "Generate Barcode with page 100"() {
        given:
        int row = 1
        int col = 1
        int batchnumber = 4873985
        int pagenumber = 100

        when:
        String result = barcodeHelper.generateBarcodeData(row, col, batchnumber, pagenumber)

        then:
        result == "087310001019"
        0*_._
    }


}
