package com.techvvs.inventory.util

import com.techvvs.inventory.jparepo.TokenRepo
import com.techvvs.inventory.model.SystemUserDAO
import com.techvvs.inventory.model.TokenDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.security.SecureRandom
import java.time.LocalDateTime

@Component
class TokenUtil {


    @Autowired
    TokenRepo tokenRepo

    SecureRandom secureRandom = new SecureRandom();



    void createRandomToken(SystemUserDAO systemUserDAO){

         // save a token value to a username and then send a text message
         String tokenval = String.valueOf(secureRandom.nextInt(1000000));


         TokenDAO tokenDAO = new TokenDAO();
         tokenDAO.setUsermetadata(systemUserDAO.getEmail());
         tokenDAO.setToken(tokenval);
         tokenDAO.setTokenused(0);
         tokenDAO.setCreatetimestamp(LocalDateTime.now());
         tokenDAO.setUpdatedtimestamp(LocalDateTime.now());
         tokenRepo.save(tokenDAO);
         System.out.println("new token saved successfully for user with no existing token. ");

     }


}
