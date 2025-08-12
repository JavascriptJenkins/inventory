package com.techvvs.inventory.service.metrc.global

import com.techvvs.inventory.service.metrc.model.dto.MetrcFacilityDto
import com.techvvs.inventory.service.metrc.model.dto.MetrcIncomingTransferDto
import com.techvvs.inventory.service.metrc.model.entity.MetrcFacilityVO
import com.techvvs.inventory.service.metrc.model.entity.PagedEmployeesVO
import com.techvvs.inventory.service.metrc.model.entity.PagedLocationsVO
import com.techvvs.inventory.service.metrc.model.entity.LocationVO
import com.techvvs.inventory.service.metrc.model.entity.PagedLocationTypesVO
import com.techvvs.inventory.service.metrc.model.dto.LocationDto
import org.springframework.stereotype.Component


// this class has methods that will be used by all metrc license types
@Component
interface MetrcGlobal {


    List<MetrcIncomingTransferDto> getIncomingTransfers();

    List<MetrcFacilityVO> getFacilities() throws Exception;

    PagedEmployeesVO getEmployees(String licenseNumber, int pageNumber, int pageSize) throws Exception;

    PagedLocationsVO getActiveLocations(String licenseNumber) throws Exception;

    PagedLocationsVO getInactiveLocations(String licenseNumber) throws Exception;

    LocationVO createLocation(LocationDto locationDto) throws Exception;

    LocationVO updateLocation(LocationDto locationDto) throws Exception;

    void archiveLocation(Long locationId, String licenseNumber) throws Exception;

    PagedLocationTypesVO getLocationTypes(String licenseNumber) throws Exception;

}