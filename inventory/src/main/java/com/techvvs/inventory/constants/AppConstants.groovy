package com.techvvs.inventory.constants

import org.springframework.stereotype.Component

@Component
class AppConstants {


    String UPLOAD_DIR = "./uploads/";

    // move to filesystemconstants
    String PARENT_LEVEL_DIR = "./topdir/"
    String QR_ALL_DIR = "/qrcodes/all/"
    String BARCODES_ALL_DIR = "/barcodes/all/"
    String BARCODES_MENU_DIR = "/barcodes/menu/"
    String filenameprefix = "upca_"
    String filenameprefix_qr = "qr_"


}
