package com.techvvs.inventory.util;

import org.springframework.stereotype.Component;

// cache to get all user data by passing in a user object
@Component
public class SimpleCache {
    
    // making a cache of the users and their roles so we don't have to go to database every time
    public void refreshCache(){
    }

}