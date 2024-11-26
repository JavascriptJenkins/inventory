package com.techvvs.inventory.constants

import org.apache.pdfbox.pdmodel.common.PDRectangle
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
    public String BARCODES_MANIFEST_DIR = "/barcodes/manifest/"
    public String BARCODES_EPSON_64_DIR = "/barcodes/epson6by4/"
    public String BARCODES_DYMNO_28mmx89mm_DIR = "/barcodes/dymmo28mmx89mm/"
    public String ADHOC_DIR = "/adhoc/"
    public String BATCH_PRICE_SHEETS_DIR = "/pricesheets/"
    public String COA_DIR = "/coa/"
    public String FILES_FOR_GLOBAL_USER_DOWNLOAD_DIR = "/globaluserfiles/"
    public String BARCODES_MENU_DIR = "/barcodes/menu/"
    String BARCODES_TRANSFER_DIR = "/adhoc/transfers/"
    String filenameprefix = "upca_"
    String filenameprefix_qr = "qr_"
    public String filenameprefix_qr_media = "qr_media_"
    String filenameprefix_adhoc_label = "adhoc_label_"
    String filenameprefix_manifest = "_manifest_"
    String filenameprefix_epson46 = "_epson4by6_"
    String filenameprefix_dymno_28mmx89mm = "_dymno28mmx89mm_"


    public static final PDRectangle FOUR_BY_SIX_POINT_FIVE = new PDRectangle(288.0F, 468.0F);
    public static final PDRectangle ONE_AND_ONE_EIGHTH_BY_THREE_AND_A_HALF = new PDRectangle(81.0F, 252.0F); // 28mmx89mm



    String DEV_1 = "dev1"

    String TEXT_INVOICE = "text"
String EMAIL_INVOICE = "email"
String VIEW_INVOICE = "view"

    String QR_CODE_PUBLIC_INFO_LINK_PROD = "http://inventory.techvvs.io/qr/publicinfo?productid="
    String QR_CODE_PUBLIC_INFO_LINK_DEV1 = "http://localhost:8080/qr/publicinfo?productid="
    String QR_CODE_URI_EXTENSION = "/qr/publicinfo?productid="
    String QR_CODE_URI_LEAFYLY = "https://www.leafly.com/search?q="


    int DEFAULT_TAX_PERCENTAGE = 0

    //delivery constants below
    int DELIVERY_STATUS_CREATED = 0
    int DELIVERY_STATUS_EN_ROUTE = 1
    int DELIVERY_STATUS_DELIVERED = 2
    int DELIVERY_STATUS_CANCELED = 3


    // tenants
    String TENANT_HIGHLAND = "highland"
    String TENANT_TEST1 = "test1"
    String TENANT_TEST = "test"

}
