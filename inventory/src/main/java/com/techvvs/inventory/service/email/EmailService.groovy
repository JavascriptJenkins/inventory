package com.techvvs.inventory.service.email

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.TokenRepo
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





}
