package com.techvvs.inventory.constants

import org.springframework.stereotype.Component

@Component
class AppConstants {

    /* dir structure is /topdir/$batchnumber/$otherdirs */


    // todo: depricate this upload dir?
    String UPLOAD_DIR = "./uploads/";

    // move to filesystemconstants
    String PARENT_LEVEL_DIR = "./topdir/"

    String TRANSACTION_INVOICE_DIR = "/transaction/invoice/"
    String QR_ALL_DIR = "/qrcodes/all/"
    String BARCODES_ALL_DIR = "/barcodes/all/"
    String BATCH_PRICE_SHEETS_DIR = "/pricesheets/"
    String COA_DIR = "/coa/"
    String BARCODES_MENU_DIR = "/barcodes/menu/"
    String BARCODES_TRANSFER_DIR = "/adhoc/transfers/"
    String filenameprefix = "upca_"
    String filenameprefix_qr = "qr_"


    String DEV_1 = "dev1"

    String TEXT_INVOICE = "text"
String EMAIL_INVOICE = "email"
String VIEW_INVOICE = "view"

    String QR_CODE_PUBLIC_INFO_LINK_PROD = "http://northstar1.techvvs.io/qr/publicinfo?productid="
    String QR_CODE_PUBLIC_INFO_LINK_DEV1 = "http://localhost:8080/qr/publicinfo?productid="
    String QR_CODE_URI_EXTENSION = "/qr/publicinfo?productid="
    String QR_CODE_URI_LEAFYLY = "https://www.leafly.com/search?q="


    int DEFAULT_TAX_PERCENTAGE = 0
}
