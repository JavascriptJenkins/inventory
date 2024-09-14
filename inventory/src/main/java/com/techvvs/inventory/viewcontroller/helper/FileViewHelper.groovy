package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.modelnonpersist.MenuOptionVO
import com.techvvs.inventory.util.TechvvsFileHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FileViewHelper {

    @Autowired
    AppConstants appConstants

    @Autowired
    TechvvsFileHelper techvvsFileHelper

    MenuOptionVO sanitizeTransients(MenuOptionVO menuOptionVO){
        menuOptionVO.phonenumber = menuOptionVO.phonenumber.replace(",", "")
        menuOptionVO.email = menuOptionVO.email.replace(",", "")
        menuOptionVO.action = menuOptionVO.action.replace(",", "")
        menuOptionVO.selected = menuOptionVO.selected.replace(",", "")
        menuOptionVO.filenametosend = menuOptionVO.filenametosend.replace(",", "")
        return menuOptionVO
    }

    String getBase64FileForViewing(String subdir, String filename){
        filename = filename.replaceAll(",", "")
        String dirandfilename = appConstants.PARENT_LEVEL_DIR+subdir+filename
        String base64contents = techvvsFileHelper.readPdfAsBase64String(dirandfilename)
        return base64contents;
    }

    String getBase64FileForViewingFromUploads(String subdir, String filename){
        filename = filename.replaceAll(",", "")
        String dirandfilename = appConstants.UPLOAD_DIR+subdir+filename
        String base64contents = techvvsFileHelper.readPdfAsBase64String(dirandfilename)
        return base64contents;
    }


}
