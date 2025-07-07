package com.techvvs.inventory.service.metrc.license.impl

import com.techvvs.inventory.jparepo.MetrcLicenseRepo
import com.techvvs.inventory.model.metrc.MetrcLicenseVO
import com.techvvs.inventory.service.metrc.license.MetrcLicense
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MetrcLicenseImpl implements MetrcLicense{

    // Actual Logic for implementing the actions needed by the MetrcLicense Interface that are License Related will go here

    @Autowired
    MetrcLicenseRepo metrcLicenseRepo

    @Override
    MetrcLicenseVO createNewMetrcLicense(MetrcLicenseVO metrcLicenseVO) {



        return null
    }
}
