package com.techvvs.inventory.util

import com.techvvs.inventory.constants.AppConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.ui.Model

@Component
class TechvvsAppUtil {

    @Autowired Environment env
    @Autowired AppConstants appConstants


    boolean isDev1(){
        return appConstants.DEV_1.equals(env.getProperty("spring.profiles.active"))
    }



}
