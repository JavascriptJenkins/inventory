package com.techvvs.inventory.service.metrc.license

import com.techvvs.inventory.model.metrc.MetrcLicenseVO
import org.springframework.stereotype.Component

@Component
interface MetrcLicense {

    /* list all the actions for METRC License here, which will be used in the Service Class */

    /* This creates a new MetrcLicense record in the system database (not the METRC api).
    * This will be used to compare against the records that the system has access to administrate using the METRC api credentials we have.
    *
    *
    */
    MetrcLicenseVO createNewMetrcLicense(MetrcLicenseVO metrcLicenseVO)

}