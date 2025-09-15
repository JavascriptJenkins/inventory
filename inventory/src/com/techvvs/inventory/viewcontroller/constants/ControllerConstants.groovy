package com.techvvs.inventory.viewcontroller.constants

import com.techvvs.inventory.constants.AppConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import java.nio.file.Paths

@Component
class ControllerConstants {

    @Autowired
    AppConstants appConstants

    public final String MENU_OPTIONS = "menuoption" // the selected menu option coming in on a post
    public final String ALL = "All" // the selected menu option coming in on a post
    public final String SINGLE_MENU = "Single Menu" // the selected menu option coming in on a post
    public final String SINGLE_PAGE = "Single Page (50)" //  the selected menu option coming in on a post
    public final String MENU_OPTIONS_WEIGHT_LABELS = "menuoptionsWeightLabels" //  the selected menu option coming in on a post
    public final String MENU_OPTIONS_QR_CODES = "menuoptionsQrcode" //  the selected menu option coming in on a post
    public final String MENU_OPTIONS_BARCODE = "menuoptionsBarcode" //  the selected menu option coming in on a post
    public final String MENU_OPTIONS_TEXT_XLSX = "menuoptions" //  the selected menu option coming in on a post
    public final String MENU_OPTIONS_DIRECTORIES = "directoryoptions" //  the selected menu option coming in on a post
    public String[] DIRECTORIES_FOR_BATCH_UI = ""
    public String[] DIRECTORIES_FOR_EDIT_PRODUCT_UI = ""

    @PostConstruct
    void load(){
        DIRECTORIES_FOR_BATCH_UI = [appConstants.BARCODES_ALL_DIR,
                                    appConstants.BATCH_PRICE_SHEETS_DIR,
                                    appConstants.QR_ALL_DIR,
                                    appConstants.QR_MEDIA_DIR,
                                    appConstants.BARCODES_MANIFEST_DIR,
                                    appConstants.BARCODES_EPSON_64_DIR,
                                    appConstants.BARCODES_DYMNO_28mmx89mm_DIR
        ]
        DIRECTORIES_FOR_EDIT_PRODUCT_UI = [
                appConstants.UPLOAD_DIR_PRODUCT
        ]
    }

}
