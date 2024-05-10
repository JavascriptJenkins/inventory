package com.techvvs.inventory.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvvs.inventory.jparepo.TokenRepo;
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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
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


    TwilioRestClient client;



    public String sendDownloadLink(SystemUserDAO systemUserDAO, String linkandtoken, boolean isDev1) {

        String message = "Download your file: "+linkandtoken;

        if(!isDev1){
            send(systemUserDAO.getPhone(), message);
        } else {
            System.out.println("NOT sending your file link because we are in dev1");
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
            // to and from respectively
            new MessageCreator(
                    new PhoneNumber(to),
                    new PhoneNumber("+1 866 720 6310"),
                    message
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
                result = sendValidationText(systemUserDAO, tokenval, isDev1);
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
            roles.add(Role.ROLE_CLIENT);
            cellphonetoken = jwtTokenProvider.createTokenForSmsDownloadLinks(systemUserDAO.getEmail(), roles);


        } catch(Exception ex){
            System.out.println("error inserting token into database");
        } finally{
            // only send the text message after everything else went smoothly
            // todo : check result of this
            if(!isDev1){
                result = sendDownloadLink(systemUserDAO, "https://inventory.techvvs.com/file/smsdownload?customJwtParameter="+cellphonetoken+"&filename="+filename, isDev1);
            } else {
                result = "success"; // set it to success if we are in dev1 and skipped sending the validation text
                System.out.println("Did NOT send validation text because we in dev1");
            }
            System.out.println("Send Download Text with result: "+result);
        }

        return "success";
    }
}
