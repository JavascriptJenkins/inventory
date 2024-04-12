package com.techvvs.inventory.security;

import com.techvvs.inventory.exception.CustomException;
import com.techvvs.inventory.jparepo.SystemUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class UserService {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SystemUserRepo systemUserRepo;

    public String signin(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

//      return jwtTokenProvider.createTokenForLogin(username,
//              userRepository.findByUsername(username).getRoles(),
//              organizationRepository.findByUser()
//      );



            return jwtTokenProvider.createToken(username, (List<Role>) Arrays.asList(systemUserRepo.findByEmail(username).getRoles()));
        } catch (AuthenticationException e) {
            throw new CustomException("Invalid username/password supplied", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }


}
