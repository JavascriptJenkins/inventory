package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.modelnonpersist.LockVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.lock.LockService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*


@RequestMapping("/lock")
@Controller
public class LockViewController {


    @Autowired
    LockService lockService

    @Autowired
    TechvvsAuthService techvvsAuthService


    //default home mapping
    @GetMapping
    String viewNewForm(
            Model model
    ){

        boolean locked = lockService.checkifdataislocked()
        locked ? model.addAttribute("locked", "locked") : model.addAttribute("locked", "unlocked")

        model.addAttribute("lock", new LockVO())

        techvvsAuthService.checkuserauth(model)
        return "lock/lock.html";
    }


    @PostMapping("/lockdata")
    String lockdata(
            @ModelAttribute( "lock" ) LockVO lockVO,
            Model model
    ){

        lockService.lockdata(lockVO)

        boolean locked = lockService.checkifdataislocked()
        locked ? model.addAttribute("locked", "locked") : model.addAttribute("locked", "unlocked")

        model.addAttribute("successMessage", "Data locked successfully.  Don't loose your key or it will be gone forever. ")
        model.addAttribute("lock", new LockVO())
        techvvsAuthService.checkuserauth(model)

        return "lock/lock.html";
    }

    @PostMapping("/unlockdata")
    String unlockdata(
            @ModelAttribute( "lock" ) LockVO lockVO,
            Model model
    ){

        lockService.unlockdata(lockVO)

        boolean locked = lockService.checkifdataislocked()
        locked ? model.addAttribute("locked", "locked") : model.addAttribute("locked", "unlocked")

        model.addAttribute("successMessage", "Data unlocked successfully.  Don't loose your key or it will be gone forever. ")
        model.addAttribute("lock", new LockVO())
        techvvsAuthService.checkuserauth(model)

        return "lock/lock.html";
    }


    @PostMapping("/generatekey")
    String generatekey(
            @ModelAttribute( "lock" ) LockVO lockVO,
            Model model
    ){

        // Slu/m9UwOfIjrAsOQQRftF41x4WdVppzHSU8PIJ6SPU=

        String key = lockService.generateSecretKey()
        boolean locked = lockService.checkifdataislocked()

        locked ? model.addAttribute("locked", "locked") : model.addAttribute("locked", "unlocked")

        model.addAttribute("successMessage", "Don't loose your key or your data will be gone FOREVER. ")
        model.addAttribute("lock", new LockVO(secretkey: key))
        techvvsAuthService.checkuserauth(model)
        model.addAttribute("keygenerated", "true");

        return "lock/lock.html";
    }





}
