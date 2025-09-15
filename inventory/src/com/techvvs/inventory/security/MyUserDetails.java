package com.techvvs.inventory.security;

import com.techvvs.inventory.jparepo.SystemUserRepo;
import com.techvvs.inventory.model.SystemUserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetails implements UserDetailsService {

  @Autowired
  private SystemUserRepo userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    final SystemUserDAO user = userRepository.findByEmail(username);

    if (user == null) {
      throw new UsernameNotFoundException("Username '" + username + "' not found");
    }

    return org.springframework.security.core.userdetails.User//
        .withUsername(username)//
        .password(user.getPassword())//
        .authorities(user.getRoles())//
        .accountExpired(false)//
        .accountLocked(false)//
        .credentialsExpired(false)//
        .disabled(false)//
        .build();
  }


  public UserDetails loadUserForPublicToken(String username) throws UsernameNotFoundException {

    return org.springframework.security.core.userdetails.User//
            .withUsername(username)//
            .password("publicpassword")//
            .authorities("publicusername")//
            .accountExpired(false)//
            .accountLocked(false)//
            .credentialsExpired(false)//
            .disabled(false)//
            .build();
  }

}
