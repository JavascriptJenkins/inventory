package com.techvvs.inventory.metrc.map

import com.techvvs.inventory.service.metrc.constants.MetrcCallEnum
import com.techvvs.inventory.model.nonpersist.RequestMetaData
import com.techvvs.inventory.service.metrc.model.dto.MetrcFacilityDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import com.techvvs.inventory.service.metrc.model.dto.PagedEmployeesDto
import com.techvvs.inventory.service.metrc.model.dto.LocationDto
import com.techvvs.inventory.service.metrc.model.dto.PagedLocationsDto
import com.techvvs.inventory.service.metrc.model.dto.PagedLocationTypesDto

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
                        responseObjectType: PagedEmployeesDto.class
                )
        )

        uriMap.put(
                MetrcCallEnum.GET_LOCATIONS_ACTIVE,
                new RequestMetaData(
                        uri: "/locations/v2/active",
                        httpMethod: "GET",
                        baseUri: isDev1
                                ? "https://sandbox-api-mn.metrc.com"
                                : "https://api-mn.metrc.com",
                        responseObjectType: PagedLocationsDto.class
                )
        )

        uriMap.put(
                MetrcCallEnum.GET_LOCATIONS_INACTIVE,
                new RequestMetaData(
                        uri: "/locations/v2/inactive",
                        httpMethod: "GET",
                        baseUri: isDev1
                                ? "https://sandbox-api-mn.metrc.com"
                                : "https://api-mn.metrc.com",
                        responseObjectType: PagedLocationsDto.class
                )
        )

        uriMap.put(
                MetrcCallEnum.GET_LOCATION_TYPES,
                new RequestMetaData(
                        uri: "/locations/v2/types",
                        httpMethod: "GET",
                        baseUri: isDev1
                                ? "https://sandbox-api-mn.metrc.com"
                                : "https://api-mn.metrc.com",
                        responseObjectType: PagedLocationTypesDto.class
                )
        )

        uriMap.put(
                MetrcCallEnum.POST_LOCATION_CREATE,
                new RequestMetaData(
                        uri: "/locations/v2/",
                        httpMethod: "POST",
                        baseUri: isDev1
                                ? "https://sandbox-api-mn.metrc.com"
                                : "https://api-mn.metrc.com",
                        responseObjectType: LocationDto.class
                )
        )

        uriMap.put(
                MetrcCallEnum.PUT_LOCATION_UPDATE,
                new RequestMetaData(
                        uri: "/locations/v2/",
                        httpMethod: "PUT",
                        baseUri: isDev1
                                ? "https://sandbox-api-mn.metrc.com"
                                : "https://api-mn.metrc.com",
                        responseObjectType: LocationDto.class
                )
        )

        uriMap.put(
                MetrcCallEnum.DELETE_LOCATION_ARCHIVE,
                new RequestMetaData(
                        uri: "/locations/v2/",
                        httpMethod: "DELETE",
                        baseUri: isDev1
                                ? "https://sandbox-api-mn.metrc.com"
                                : "https://api-mn.metrc.com",
                        responseObjectType: String.class
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
