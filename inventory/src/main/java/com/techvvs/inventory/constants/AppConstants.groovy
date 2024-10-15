package com.techvvs.inventory.constants

import org.springframework.stereotype.Component

@Component
class AppConstants {

    /* dir structure is /topdir/$batchnumber/$otherdirs */


    public String UPLOAD_DIR_MEDIA = "./uploads/media/";
    public String UPLOAD_DIR_PRODUCT = "/product/";

    // todo: depricate this upload dir?
    String UPLOAD_DIR = "./uploads/";

    // move to filesystemconstants
    String PARENT_LEVEL_DIR = "./topdir/"

    String TRANSACTION_INVOICE_DIR = "/transaction/invoice/"
    String PACKAGE_DIR = "/package/"
    String CRATE_DIR = "/crate/"
    String LABEL_DIR = "/label/"
    public String DELIVERY_DIR = "/delivery/"
    String QR_ALL_DIR = "/qrcodes/all/"
    public String QR_MEDIA_DIR = "/qrcodes/media/"
    public String BARCODES_ALL_DIR = "/barcodes/all/"
    String ADHOC_DIR = "/adhoc/"
    String BATCH_PRICE_SHEETS_DIR = "/pricesheets/"
    public String COA_DIR = "/coa/"
    String BARCODES_MENU_DIR = "/barcodes/menu/"
    String BARCODES_TRANSFER_DIR = "/adhoc/transfers/"
    String filenameprefix = "upca_"
    String filenameprefix_qr = "qr_"
    public String filenameprefix_qr_media = "qr_media_"
    String filenameprefix_adhoc_label = "adhoc_label_"


    String DEV_1 = "dev1"

    String TEXT_INVOICE = "text"
String EMAIL_INVOICE = "email"
String VIEW_INVOICE = "view"

    String QR_CODE_PUBLIC_INFO_LINK_PROD = "http://alpha.techvvs.io/qr/publicinfo?productid="
    String QR_CODE_PUBLIC_INFO_LINK_DEV1 = "http://localhost:8080/qr/publicinfo?productid="
    String QR_CODE_URI_EXTENSION = "/qr/publicinfo?productid="
    String QR_CODE_URI_LEAFYLY = "https://www.leafly.com/search?q="


    int DEFAULT_TAX_PERCENTAGE = 0

    //delivery constants below
    int DELIVERY_STATUS_CREATED = 0
    int DELIVERY_STATUS_EN_ROUTE = 1
    int DELIVERY_STATUS_DELIVERED = 2
    int DELIVERY_STATUS_CANCELED = 3

}
