package com.techvvs.inventory.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class WhiteListUriConstants {


    public static final String OAUTH_GOOGLE_CALLBACK = "/oauth2/callback/google";
    public static final String OAUTH_GOOGLE_LOGIN = "/oauth2/login/google";
    public static final String OAUTH_GOOGLE_AUTH = "/oauth2/authorization/google";
    public static final String OAUTH_VERIFY_LINKING = "/oauth2/verify-linking";
    public static final String LOGIN = "/login";
    public static final String LOGIN_REQUEST_LINK = "/login/requestlink";
    public static final String LOGIN_MAGIC_LINK_GATEWAY = "/login/magiclinkgateway";
    public static final String FAVICON = "/favicon.ico";
    public static final String CSS_TABLE = "/css/table.css";
    public static final String LOGIN_SYSTEMUSER_DOUBLE_WILDCARD = "/login/systemuser/**";
    public static final String LOGIN_SYSTEMUSER = "/login/systemuser";
    public static final String VERIFY_PHONE_TOKEN = "/login/verifyphonetoken";
    public static final String CREATE_ACCOUNT = "/login/createaccount";
    public static final String RESET_PASSWORD = "/login/resetpassword";
    public static final String CREATE_SYSTEM_USER = "/login/createSystemUser";
    public static final String VERIFY = "/login/verify";
    public static final String VERIFY_WILDCARD = "/login/verify/*";
    public static final String VERIFY_DOUBLE_WILDCARD = "/login/verify/**";
    public static final String QR = "/qr";
    public static final String PUBLIC_LANDING_PAGE_TULIP = "/landing";
    public static final String PUBLIC_CONFERENCE_PAGE_TULIP = "/conference";
    public static final String PUBLIC_CONFERENCE_PAGE_TULIP_POST = "/conference/**";
    public static final String CHECKOUT_VIEW_PDF = "/checkout/viewpdf";
    public static final String QR_PUBLIC_INFO = "/qr/publicinfo";
    public static final String LEGAL_PRIVACY_POLICY = "/legal/privacypolicy";
    public static final String LEGAL_TOS = "/legal/termsofservice";
    public static final String QR_PUBLIC_INFO_WILDCARD = "/qr/publicinfo/*";
    public static final String QR_PUBLIC_INFO_DOUBLE_WILDCARD = "/qr/publicinfo/**";
    public static final String FILE_SMS_DOWNLOAD = "/file/smsdownload/**";
    public static final String FILE_SMS_DOWNLOAD_2 = "/file/smsdownload2/**";
    public static final String FILE_SMS_DOWNLOAD_22 = "/file/smsdownload2";
    public static final String FILE_SMS_DOWNLOAD_3 = "/file/smsdownload3/**";
    public static final String FILE_SMS_DOWNLOAD_33 = "/file/smsdownload3";
    public static final String FILE_PUBLIC_DOWNLOAD = "/file/publicdownload/**";
    public static final String FILE_PUBLIC_DOWNLOAD_2 = "/file/publicdownload";
    public static final String FILE_QR_MEDIA_ZIP_DOWNLOAD = "/file/qrzipmediadownload/**";
    public static final String FILE_QR_MEDIA_ZIP_DOWNLOAD_33 = "/file/qrzipmediadownload";
    public static final String MENU_SHOP_URI = "/menu/shop/**";
    public static final String MENU_SHOP_URI_33 = "/menu/shop";
    public static final String MENU_URI = "/menu/**";
    public static final String MENU_URI_33 = "/menu/";
    public static final String MENU_SHOP_2_URI = "/menu2/shop/**";
    public static final String MENU_SHOP_URI_2_33 = "/menu2/shop";
    public static final String MENU_URI_2 = "/menu2/**";
    public static final String MENU_URI_2_33 = "/menu2/";
    public static final String MENU_SHOP_3_URI = "/menu3/shop/**";
    public static final String MENU_SHOP_URI_3_33 = "/menu3/shop";
    public static final String MENU_URI_3 = "/menu3/**";
    public static final String MENU_URI_3_33 = "/menu3/";
    public static final String MENU_URI_5 = "/menu5/**";
    public static final String MENU_URI_5_33 = "/menu5/";
    public static final String IMAGE_IMAGES = "/image/images/";
    public static final String IMAGE_IMAGES_33 = "/image/images/**";
    public static final String IMAGE_IMAGES_PHOTOS = "/image/images/photos/";
    public static final String IMAGE_IMAGES_PHOTOS_33 = "/image/images/photos/**";
    public static final String VIDEO_VIDEOS = "/video/videos/";
    public static final String VIDEO_VIDEOS_33 = "/video/videos/**";
    public static final String VIDEO_PRODUCT = "/video/product";
    public static final String VIDEO_PRODUCT_33 = "/video/product/**";
    public static final String PHOTO_PRODUCT = "/photo/product";
    public static final String PHOTO_PRODUCT_33 = "/photo/product/**";
    public static final String PHOTO_PHOTO = "/photo/photos";
    public static final String PHOTO_PHOTO_33 = "/photo/photos/**";
    public static final String DOCUMENT_PRODUCT = "/document/product";
    public static final String DOCUMENT_PRODUCT_33 = "/document/product/**";
    public static final String DOCUMENT_DOCUMENTS = "/document/documents";
    public static final String DOCUMENT_DOCUMENTS_33 = "/document/documents/**";
    public static final String DELIVERY_ITEM_33 = "/delivery/item/**";
    public static final String DELIVERY_ITEM = "/delivery/item"
    public static final String PAYPAL_JSON_API_1_33 = "/api/paypal/";
    public static final String PAYPAL_JSON_API_1 = "/api/paypal/**";
    public static final String PAYPAL_CANCEL_API_1_33 = "/payment/paypal/cancel/";
    public static final String PAYPAL_CANCEL_API_1 = "/payment/paypal/cancel/**";
    public static final String PAYPAL_RETURN_API_1_33 = "/payment/paypal/return/";
    public static final String PAYPAL_RETURN_API_1 = "/payment/paypal/return/**";
    public static final String PAYPAL_THANKYOU_API_1_33 = "/payment/paypal/thank-you/";
    public static final String PAYPAL_THANKYOU_API_1 = "/payment/paypal/thank-you/**";
    public static final String PAYPAL_CAPTURE_API_1 = "/api/paypal/orders/**/capture/";
    public static final String PAYPAL_CAPTURE_API_1_33 = "/api/paypal/orders/**/capture/**";

    public static final String KALE_MOVIE_33 = "/kalemovie/**";
    public static final String KALE_MOVIE = "/kalemovie"
    public static final String MCP_API = "/api/mcp/**";



}
