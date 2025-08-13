package com.techvvs.inventory.service.email

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.TokenRepo
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.SystemUserDAO
import com.techvvs.inventory.model.TokenDAO
import com.techvvs.inventory.security.JwtTokenProvider
import com.techvvs.inventory.security.Role
import com.techvvs.inventory.util.SendgridEmailUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

import java.security.SecureRandom
import java.time.LocalDateTime

@Component
class EmailService {

    @Autowired
    SendgridEmailUtil emailUtil

    @Autowired
    Environment env;

    @Autowired
    TokenRepo tokenRepo;

    SecureRandom secureRandom = new SecureRandom();

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    AppConstants appConstants




    boolean sendDownloadLinkEmail(String email, String filename) {

        List<Role> roles = new ArrayList<>(1);
        roles.add(Role.ROLE_CLIENT);
        String emailtoken = jwtTokenProvider.createTokenForEmailValidation(email, roles);


//        if(env.getProperty(appConstants.DEV_1)){
//            System.out.println("NOT sending your file link because we are in dev1");
//        } else {
            emailUtil.sendEmail("http://localhost:8080/file/smsdownload?customJwtParameter="+emailtoken+"&filename="+filename,
                    email,
                    "Download your file from InventoryVVS");
//        }

        return true
    }




    boolean sendConferenceEmailToMeAndCustomer(CustomerVO customerVO) {


        boolean hasphone = false
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



        String messagetext = sb.toString();



        // send email to me with the info
        emailUtil.sendEmail(messagetext,
                "admin@techvvs.io",
                "New Tulip Conference Signup");


        messagetext = "Hey great to meet you "+customerVO.getName()+"! " + "Email me anytime to gain access to the " +
                "Tulip METRC compliance platform, or to stock your dispensary in MN! "

        // send email to the customer
        emailUtil.sendEmail(messagetext,
                customerVO.getEmail(),
                "Tulip Wholesale and METRC compliance platform for MN");

        return true
    }


    boolean sendConferenceEmailToMeAndCustomerBottleneck(CustomerVO customerVO) {


        boolean hasphone = false
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



        String messagetext = sb.toString();



        // send email to me with the info
        emailUtil.sendEmail(messagetext,
                "admin@techvvs.io",
                "New Bottleneck Signup");


        messagetext = "Hey great to meet you "+customerVO.getName()+"! " + "Now you will be informed of any Bottleneck Movie Production Updates. " +
                "My name is Kale Eickhof and it was great to have you at the movie theater in Crookston! "

        // send email to the customer
        emailUtil.sendEmail(messagetext,
                customerVO.getEmail(),
                "Bottleneck Movie Production and Investor Updates");

        return true
    }

}
