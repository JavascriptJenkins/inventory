package com.techvvs.inventory.viewcontroller


import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.VendorRepo
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.vendor.VendorService
import com.techvvs.inventory.viewcontroller.helper.RewardConfigHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/metrc/facility")
@Controller
public class MetrcFacilityViewController {
    

    @Autowired
    TechvvsAuthService techvvsAuthService

    @GetMapping
    String viewPage(
            Model model
    ){
        techvvsAuthService.checkuserauth(model)
        return "metrc/facility/getview.html";
    }


}
