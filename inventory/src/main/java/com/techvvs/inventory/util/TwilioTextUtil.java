package com.techvvs.inventory.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvvs.inventory.jparepo.SystemUserRepo;
import com.techvvs.inventory.jparepo.TokenRepo;
import com.techvvs.inventory.model.CustomerVO;
import com.techvvs.inventory.model.SystemUserDAO;
import com.techvvs.inventory.model.TokenDAO;
import com.techvvs.inventory.security.JwtTokenProvider;
import com.techvvs.inventory.security.Role;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


// The purpose of this class is to send 6 digit verification tokens to phone numbers for account 2FA
@Component
public class TwilioTextUtil {

    @Autowired
    Environment env;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    TokenRepo tokenRepo;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    SecureRandom secureRandom = new SecureRandom();

    @Autowired
    SystemUserRepo systemUserRepo;


    TwilioRestClient client;



    public String sendDownloadLink(SystemUserDAO systemUserDAO, String linkandtoken, boolean isDev1) {

        String message = linkandtoken;

        if(!isDev1){
            send(systemUserDAO.getPhone(), message);
        } else {
            System.out.println("NOT sending your file link because we are in dev1");
        }

        return "success";
    }

    public String sendDownloadLinkCustomPhoneNumber(String phone, String linkandtoken, boolean isDev1) {

        String message = linkandtoken;

        if(!isDev1){
            send(phone, message);
        } else {
            System.out.println("NOT sending your file link because we are in dev1");
        }

        return "success";
    }

    public String sendCustomerInfoFromConferenceToMyPhone(String phone, String message, boolean isDev1) {

        if(!isDev1){
            send(phone, message);
        } else {
            System.out.println("NOT sending your customer info because we are in dev1");
        }

        return "success";
    }

    public String sendCustomerInfoFromConferenceWelcomeMessage(String phone, String message, boolean isDev1) {

        if(!isDev1){
            send(phone, message);
        } else {
            System.out.println("NOT sending welcome text because we are in dev1");
        }

        return "success";
    }


    // todo: enforce country code
    public String sendValidationText(SystemUserDAO systemUserDAO, String token, boolean isDev1) {

        String message = "Validate your techvvs account with token: "+token;

        if(!isDev1){
            send(systemUserDAO.getPhone(), message);
        } else {
            System.out.println("NOT sending validation text because we are in dev1");
        }



        return "success";
    }

    public void send(String to, String message) {

        // SID and Auth Token respectively
        client = new TwilioRestClient.Builder(env.getProperty("twilio.api.username"), env.getProperty("twilio.api.password")).build();

        try{
            System.out.println("sending twilio sms: "+to+"||"+message);
            // to and from respectively
            new MessageCreator(
                    new PhoneNumber(to),
                    new PhoneNumber("+1 866 720 6310"),
                    message+" | Reply STOP to opt out."
            ).create(client);

        } catch(Exception ex){
            System.out.println("Caught Exception sending twilio sms: "+ex.getMessage() );
        }

    }

    public String createAndSendNewPhoneToken(SystemUserDAO systemUserDAO, boolean isDev1){
        // save a token value to a username and then send a text message
        String tokenval = String.valueOf(secureRandom.nextInt(1000000));
        String result = "";
        try {
            TokenDAO tokenDAO = new TokenDAO();
            tokenDAO.setUsermetadata(systemUserDAO.getEmail());
            tokenDAO.setToken(tokenval);
            tokenDAO.setTokenused(0);
            tokenDAO.setCreatetimestamp(LocalDateTime.now());
            tokenDAO.setUpdatedtimestamp(LocalDateTime.now());
            tokenRepo.save(tokenDAO);

        } catch(Exception ex){
            System.out.println("error inserting token into database");
        } finally{
            // only send the text message after everything else went smoothly
            // todo : check result of this
            if(!isDev1){
                SystemUserDAO user = systemUserRepo.findByEmail(systemUserDAO.getEmail());
                result = sendValidationText(user, tokenval, isDev1);
            } else {
                result = "success"; // set it to success if we are in dev1 and skipped sending the validation text
                System.out.println("Did NOT send validation text because we in dev1");
            }
            System.out.println("Send Validation with result: "+result);
        }


        return "success";


    }


    // instead of random token this needs to send a token made from jwt
    public String actuallySendOutDownloadLinkWithToken(String filename, SystemUserDAO systemUserDAO, boolean isDev1){
        // save a token value to a username and then send a text message

        String cellphonetoken = "";
        String result = "";
        try {


            List<Role> roles = new ArrayList<>(1);
            roles.add(Role.ROLE_DOWNLOAD_LINK);
            cellphonetoken = jwtTokenProvider.createTokenForSmsDownloadLinks(systemUserDAO.getEmail(), roles);


        } catch(Exception ex){
            System.out.println("error inserting token into database");
        } finally{
            // only send the text message after everything else went smoothly
            // todo : check result of this
            if(!isDev1){
                result = sendDownloadLink(systemUserDAO,"http://localhost:8080/file/smsdownload2?customJwtParameter="+cellphonetoken+"&filename="+filename.replace(" ", "_"), isDev1);
            } else {
                result = "success"; // set it to success if we are in dev1 and skipped sending the validation text
                System.out.println("Did NOT send validation text because we in dev1");
            }
            System.out.println("Send Download Text with result: "+result);
        }

        return "success";
    }

    public String sendOutDownloadLinkWithTokenSMS(String phonenumber, String filename,SystemUserDAO systemUserDAO, boolean isDev1){
        // save a token value to a username and then send a text message

        String cellphonetoken = "";
        String result = "";
        try {


            List<Role> roles = new ArrayList<>(1);
            roles.add(Role.ROLE_CLIENT);
            cellphonetoken = jwtTokenProvider.createTokenForSmsDownloadLinks(systemUserDAO.getEmail(), roles);


        } catch(Exception ex){
            System.out.println("error inserting token into database");
        } finally{
            // only send the text message after everything else went smoothly
            if(!isDev1){
                result = sendDownloadLinkCustomPhoneNumber(phonenumber, "http://localhost:8080/file/smsdownload2?customJwtParameter="+cellphonetoken+"&filename="+filename.replace(" ", "_"), isDev1);
            } else {
                result = "success"; // set it to success if we are in dev1 and skipped sending the validation text
                System.out.println("Did NOT send validation text because we in dev1");
            }
            System.out.println("Send Download Text with result: "+result);
        }

        return "success";
    }

    public String sendShoppingTokenLinkSMS(String phonenumber,
                                           boolean isDev1,
                                           String menuid,
                                           String shoppingtoken
    ) {
        // Save a token value to a username and then send a text message

        String result = "";
        try {
            List<Role> roles = new ArrayList<>(1);
            roles.add(Role.ROLE_CLIENT);
            roles.add(Role.ROLE_SHOPPING_TOKEN);

        } catch (Exception ex) {
            System.out.println("Error inserting token into database: " + ex.getMessage());
        } finally {
            // Only send the text message after everything else went smoothly
            if (!isDev1) {

                String baseuri = env.getProperty("base.qr.domain");

                // need to encode the jwt for links over sms so the periods don't screw things up
                String encodedSmsToken = encodeJwt(shoppingtoken);

                // Construct the URL including custom JWT and filename
                String smsUrl = baseuri+"/menu2/shop?shoppingtoken=" + encodedSmsToken + "&menuid=" + menuid;

                result = sendDownloadLinkCustomPhoneNumber(phonenumber, smsUrl, isDev1);
            } else {
                String baseuri = env.getProperty("base.qr.domain");
                String smsUrl = baseuri+"/menu2/shop?shoppingtoken=" + shoppingtoken + "&menuid=" + menuid;
                System.out.println("------------------------------------------------------------------------------");
                System.out.println("SHOPPING TOKEN HERE FOR DEVELOPMENT PURPOSES: " + shoppingtoken);
                System.out.println("URI LINK FOR DEVELOPMENT PURPOSES: " + smsUrl);
                System.out.println("------------------------------------------------------------------------------");
                result = "success"; // Set to success in dev mode
                System.out.println("Did NOT send validation text because we are in dev1");
            }
            System.out.println("Send Download Text with result: " + result);
        }

        return "success";
    }

    public static String encodeJwt(String jwt) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(jwt.getBytes(StandardCharsets.UTF_8));
    }

    public String sendShoppingTokenLinkSMSWithCustomMessage(String phonenumber,
                                           boolean isDev1,
                                           String menuid,
                                           String shoppingtoken,
                                           String message
    ) {
        // Save a token value to a username and then send a text message

        String result = "";
        try {
            List<Role> roles = new ArrayList<>(1);
            roles.add(Role.ROLE_CLIENT);
            roles.add(Role.ROLE_SHOPPING_TOKEN);

        } catch (Exception ex) {
            System.out.println("Error inserting token into database: " + ex.getMessage());
        } finally {
            // Only send the text message after everything else went smoothly
            if (!isDev1) {

                String baseuri = env.getProperty("base.qr.domain");

                // need to encode the jwt for links over sms so the periods don't screw things up
                String encodedSmsToken = encodeJwt(shoppingtoken);

                // Construct the URL including custom JWT and filename
                String smsUrl = baseuri+"/menu2/shop?shoppingtoken=" + encodedSmsToken + "&menuid=" + menuid;

                result = sendDownloadLinkCustomPhoneNumber(phonenumber, message+smsUrl, isDev1);
            } else {
                String baseuri = env.getProperty("base.qr.domain");
                String smsUrl = baseuri+"/menu2/shop?shoppingtoken=" + shoppingtoken + "&menuid=" + menuid;
                System.out.println("------------------------------------------------------------------------------");
                System.out.println("SHOPPING TOKEN HERE FOR DEVELOPMENT PURPOSES: " + shoppingtoken);
                System.out.println("URI LINK FOR DEVELOPMENT PURPOSES: " + smsUrl);
                System.out.println("------------------------------------------------------------------------------");
                result = "success"; // Set to success in dev mode
                System.out.println("Did NOT send validation text because we are in dev1");
            }
            System.out.println("Send Download Text with result: " + result);
        }

        return "success";
    }


    public String sendEmployeeDeliveryViewTokenLinkSMSWithCustomMessage(String phonenumber,
                                                            boolean isDev1,
                                                            String menuid,
                                                            String deliverytoken,
                                                            String message
    ) {
        // Save a token value to a username and then send a text message

        String result = "";
        try {

            // do nothing

        } catch (Exception ex) {
            System.out.println("Error inserting token into database: " + ex.getMessage());
        } finally {
            // Only send the text message after everything else went smoothly
            if (!isDev1) {

                String baseuri = env.getProperty("base.qr.domain");
                // Construct the URL including custom JWT and filename
                String smsUrl = baseuri+"/delivery/item?deliverytoken=" + deliverytoken;

                result = sendDownloadLinkCustomPhoneNumber(phonenumber, message+smsUrl, isDev1);
            } else {
                String baseuri = env.getProperty("base.qr.domain");
                String smsUrl = baseuri+"/delivery/item?deliverytoken=" + deliverytoken;
                System.out.println("------------------------------------------------------------------------------");
                System.out.println("DELIVERY TOKEN HERE FOR DEVELOPMENT PURPOSES: " + deliverytoken);
                System.out.println("URI LINK FOR DEVELOPMENT PURPOSES: " + smsUrl);
                System.out.println("------------------------------------------------------------------------------");
                result = "success"; // Set to success in dev mode
                System.out.println("Did NOT send validation text because we are in dev1");
            }
            System.out.println("Send Download Text with result: " + result);
        }

        return "success";
    }



    public static String encodeStringForURL(String input) {
        try {
            // Encode the input string using UTF-8 encoding
            String encodedString = URLEncoder.encode(input, "UTF-8");
            return encodedString;
        } catch (UnsupportedEncodingException e) {
            // Handle the exception if UTF-8 encoding is not supported
            e.printStackTrace();
            return null;
        }
    }



    public String sendOutCustomerInfoFromConferenceSMS(String phonenumber, CustomerVO customerVO, boolean isDev1){
        // save a token value to a username and then send a text message

        String cellphonetoken = "";
        String result = "";
        String messagetext = "";
        boolean hasphone = false;
        try {

            // get the customer info and put it in a message here

            StringBuilder sb = new StringBuilder();

            sb.append("Newsletter Signup Info: ");

            sb.append("\nName: ");
            sb.append(customerVO.getName());
            sb.append("\nEmail: ");
            sb.append(customerVO.getEmail());

            if (customerVO.getPhone() != null && !customerVO.getPhone().trim().isEmpty()) {
                sb.append("\nPhone: ");
                sb.append(customerVO.getPhone());
                hasphone = true;
            }

            if (customerVO.getNotes() != null && !customerVO.getNotes().trim().isEmpty()) {
                sb.append("\nNotes: ");
                sb.append(customerVO.getNotes());
            }



            messagetext = sb.toString();



        } catch(Exception ex){
            System.out.println("error inserting token into database");
        } finally{
            // only send the text message after everything else went smoothly
            if(!isDev1){
                result = sendCustomerInfoFromConferenceToMyPhone(phonenumber, messagetext, isDev1);

                System.out.println("Hasphone: "+hasphone);

                if(hasphone){
                    messagetext = "Hey "+customerVO.getName()+" it was great to meet you at the conference.  Call or text me anytime to talk about stocking your dispensary and getting access to my METRC compliance POS system! " +
                            "My name is Peter McMahon and my email is admin@techvvs.io!";
                    result = sendCustomerInfoFromConferenceWelcomeMessage(customerVO.getPhone(), messagetext, isDev1);
                }


            } else {
                result = "success"; // set it to success if we are in dev1 and skipped sending the validation text
                System.out.println("Did NOT send customer info text because we in dev1");
            }
            System.out.println("Send Customer Info Text with result: "+result);
        }

        return "success";
    }
}
