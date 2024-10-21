package com.techvvs.inventory.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class WhiteListUriConstants {


    public static final String LOGIN = "/login";
    public static final String FAVICON = "/favicon.ico";
    public static final String CSS_TABLE = "/css/table.css";
    public static final String LOGIN_SYSTEMUSER_DOUBLE_WILDCARD = "/login/systemuser/**";
    public static final String LOGIN_SYSTEMUSER = "/login/systemuser";
    public static final String VERIFY_PHONE_TOKEN = "/login/verifyphonetoken";
    public static final String CREATE_ACCOUNT = "/login/createaccount";
    public static final String CREATE_SYSTEM_USER = "/login/createSystemUser";
    public static final String VERIFY = "/login/verify";
    public static final String VERIFY_WILDCARD = "/login/verify/*";
    public static final String VERIFY_DOUBLE_WILDCARD = "/login/verify/**";
    public static final String QR = "/qr";
    public static final String CHECKOUT_VIEW_PDF = "/checkout/viewpdf";
    public static final String QR_PUBLIC_INFO = "/qr/publicinfo";
    public static final String QR_PUBLIC_INFO_WILDCARD = "/qr/publicinfo/*";
    public static final String QR_PUBLIC_INFO_DOUBLE_WILDCARD = "/qr/publicinfo/**";
    public static final String FILE_SMS_DOWNLOAD = "/file/smsdownload/**";
    public static final String FILE_SMS_DOWNLOAD_2 = "/file/smsdownload2/**";
    public static final String FILE_SMS_DOWNLOAD_22 = "/file/smsdownload2";
    public static final String FILE_SMS_DOWNLOAD_3 = "/file/smsdownload3/**";
    public static final String FILE_SMS_DOWNLOAD_33 = "/file/smsdownload3";
    public static final String FILE_PUBLIC_DOWNLOAD = "/file/publicdownload/**";
    public static final String FILE_PUBLIC_DOWNLOAD_2 = "/file/publicdownload";





}
