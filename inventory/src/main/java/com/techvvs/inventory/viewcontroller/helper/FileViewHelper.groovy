package com.techvvs.inventory.viewcontroller.helper
import com.techvvs.inventory.modelnonpersist.MenuOptionVO
import org.springframework.stereotype.Component

@Component
class FileViewHelper {

    MenuOptionVO sanitizeTransients(MenuOptionVO menuOptionVO){
        menuOptionVO.phonenumber = menuOptionVO.phonenumber.replace(",", "")
        menuOptionVO.email = menuOptionVO.email.replace(",", "")
        menuOptionVO.action = menuOptionVO.action.replace(",", "")
        menuOptionVO.selected = menuOptionVO.selected.replace(",", "")
        menuOptionVO.filenametosend = menuOptionVO.filenametosend.replace(",", "")
        return menuOptionVO
    }

}
