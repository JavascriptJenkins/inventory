package com.techvvs.inventory.barcode.impl

import org.krysalis.barcode4j.impl.upcean.UPCEANBean
import org.krysalis.barcode4j.impl.upcean.UPCEANLogicImpl
import org.springframework.stereotype.Component

@Component
class UPCBean extends UPCEANBean{


    @Override
    UPCEANLogicImpl createLogicImpl() {
        return null
    }
}
