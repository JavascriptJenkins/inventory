package com.techvvs.inventory.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvvs.inventory.jparepo.TokenRepo;
import com.techvvs.inventory.model.SystemUserDAO;
import com.techvvs.inventory.model.TokenDAO;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;


// The purpose of this class is to send 6 digit verification tokens to phone numbers for account 2FA
@Component
public class SendgridEmailUtil {

    @Autowired
    Environment env;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    TokenRepo tokenRepo;

    SecureRandom secureRandom = new SecureRandom();


    TwilioRestClient client;



    public void generateAndSendEmail(String dataToSend, ArrayList<String> emailList, String subject) throws IOException {


        // Will loop thru and send for each email in the list
        for(String email: emailList){

            System.out.println("Sending email with sendgrid... ");
            Email from = new Email("admin@techvvs.io");
            Email to = new Email(email);
            Content content = new Content("text/plain", dataToSend);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(env.getProperty("sendgrid.api.key"));

            Request request = new Request();
            try {
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());
                Response response = sg.api(request);
                System.out.println(response.getStatusCode());
                System.out.println(response.getBody());
                System.out.println(response.getHeaders());
            } catch (IOException ex) {
                System.out.println("Caught Exception sending email: "+ex.getMessage());
                throw ex;
            }



        }

    }


    public void sendEmail(String dataToSend, String email, String subject) throws IOException {



            System.out.println("Sending email with sendgrid... ");
            Email from = new Email("admin@techvvs.io");
            Email to = new Email(email);
            Content content = new Content("text/plain", dataToSend);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(env.getProperty("sendgrid.api.key"));

            Request request = new Request();
            try {
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());
                Response response = sg.api(request);
                System.out.println(response.getStatusCode());
                System.out.println(response.getBody());
                System.out.println(response.getHeaders());
            } catch (IOException ex) {
                System.out.println("Caught Exception sending email: "+ex.getMessage());
                throw ex;
            }





    }


}
