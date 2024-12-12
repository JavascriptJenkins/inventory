package com.techvvs.inventory.qrcode.impl

import com.techvvs.inventory.jparepo.SystemUserRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.SystemUserDAO
import com.techvvs.inventory.security.JwtTokenProvider
import com.techvvs.inventory.security.Role
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

// responsibility of this class is to build the actual qr links
@Component
class QrCodeBuilder {

    @Autowired
    JwtTokenProvider jwtTokenProvider

    @Autowired
    SystemUserRepo systemUserRepo


    String buildMediaQrCodeForProduct(
            String baseqrdomain,
            ProductVO productVO
    ){


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SystemUserDAO systemUserDAO = systemUserRepo.findByEmail(authentication.getPrincipal().username)

        List<Role> roles = new ArrayList<>(1);
        roles.add(Role.ROLE_CLIENT);
        String mediadownloadtoken = jwtTokenProvider.createTokenForMediaDownloadLinks(systemUserDAO.getEmail(), roles);


        // ex. Yellow_Rose_100.MOV
        String filename = productVO.name+'_'+productVO.product_id+'.MOV'
        filename = filename.replaceAll(',',"")
        filename = filename.replaceAll(" ", "_")

        return baseqrdomain+"/file/smsdownload3?customJwtParameter="+mediadownloadtoken+"&filename="+filename+"&product_id="+productVO.product_id;
    }


    // todo: cleanup this method
    String buildMediaQrCodeForProductAsLink(
            String baseqrdomain,
            ProductVO productVO
    ){


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SystemUserDAO systemUserDAO = systemUserRepo.findByEmail(authentication.getPrincipal().username)

        List<Role> roles = new ArrayList<>(1);
        roles.add(Role.ROLE_CLIENT);
        String mediadownloadtoken = jwtTokenProvider.createTokenForMediaDownloadLinks(systemUserDAO.getEmail(), roles);


        // ex. Yellow_Rose_100.MOV
        String filename = productVO.name+'_'+productVO.product_id+'.MOV'
        filename = filename.replaceAll(',',"")
        filename = filename.replaceAll(" ", "_")

        return baseqrdomain+ "/file/qrzipmediadownload?productid=" + productVO.getProduct_id()
    }




}
