package com.techvvs.inventory.metrc.map

import com.techvvs.inventory.service.metrc.constants.MetrcCallEnum
import com.techvvs.inventory.model.nonpersist.RequestMetaData
import com.techvvs.inventory.service.metrc.model.dto.MetrcFacilityDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
class MetrcUriMapping {

    @Autowired
    Environment env

    final Map<MetrcCallEnum, RequestMetaData> uriMap = new EnumMap<>(MetrcCallEnum)

    @PostConstruct
    void init() {
        boolean isDev1 = "dev1".equals(env.getProperty("spring.profiles.active"))


        /* START GET CALLS */
/* START GET CALLS */
        uriMap.put(
                MetrcCallEnum.GET_FACILITIES,
                new RequestMetaData(
                        uri: "/facilities/v2/",
                        httpMethod: "GET",
                        baseUri: isDev1
                                ? "https://sandbox-api-mn.metrc.com"
                                : "https://api-mn.metrc.com",
                        responseObjectType: MetrcFacilityDto[].class
                )
        )

        uriMap.put(
                MetrcCallEnum.GET_EMPLOYEES,
                new RequestMetaData(
                        uri: "/employees/v2/",
                        httpMethod: "GET",
                        baseUri: isDev1
                                ? "https://sandbox-api-mn.metrc.com"
                                : "https://api-mn.metrc.com",
                        responseObjectType: MetrcFacilityDto[].class
                )
        )


//
//        uriMap.put(MetrcCallEnum.GET_TRANSFERS_INCOMING, new RequestMetaData(
//                uri: "/transfers/v1/incoming",
//                httpMethod: "GET",
//                baseUri: isDev1 ? "https://sandbox-api-mn.metrc.com" : "https://api-mn.metrc.com",
//                responseObjectType: List<MetrcIncomingTransferDto>
//        ))
//        /* END GET CALLS */
//
//
//        /* START POST CALLS */
//        uriMap.put(MetrcCallEnum.POST_RECEIVE_TRANSFER, new RequestMetaData(
//            uri: "/transfers/v1/receive",
//            httpMethod: "POST",
//            baseUri: isDev1 ? "https://sandbox-api-mn.metrc.com" : "https://api-mn.metrc.com",
//            responseObjectType: MetrcReceiveTransferDto
//        ))
//
//        uriMap.put(MetrcCallEnum.POST_CREATE_TRANSFER_FOR_SANDBOX, new RequestMetaData(
//                uri: "/transfers/v1/createforsandbox",
//                httpMethod: "POST",
//                baseUri: isDev1 ? "https://sandbox-api-mn.metrc.com" : "https://api-mn.metrc.com",
//                responseObjectType: MetrcReceiveTransferDto
//        ))
//        /* END POST CALLS */

        // Add more mappings...
    }

    RequestMetaData getMetadata(MetrcCallEnum callEnum) {
        return uriMap.get(callEnum)
    }
}
