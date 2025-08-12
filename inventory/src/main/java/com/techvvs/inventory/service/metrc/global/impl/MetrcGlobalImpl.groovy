package com.techvvs.inventory.service.metrc.global.impl

import com.techvvs.inventory.service.metrc.adapter.MetrcAdapter
import com.techvvs.inventory.service.metrc.global.MetrcGlobal
import com.techvvs.inventory.service.metrc.mapper.MetrcEmployeeDtoMapper
import com.techvvs.inventory.service.metrc.mapper.MetrcFacilityDtoMapper
import com.techvvs.inventory.service.metrc.mapper.MetrcLocationDtoMapper
import com.techvvs.inventory.service.metrc.mapper.MetrcLocationTypeDtoMapper
import com.techvvs.inventory.service.metrc.model.dto.MetrcFacilityDto
import com.techvvs.inventory.service.metrc.model.dto.MetrcIncomingTransferDto
import com.techvvs.inventory.service.metrc.model.dto.PagedEmployeesDto
import com.techvvs.inventory.service.metrc.model.dto.LocationDto
import com.techvvs.inventory.service.metrc.model.dto.PagedLocationsDto
import com.techvvs.inventory.service.metrc.model.dto.PagedLocationTypesDto
import com.techvvs.inventory.service.metrc.model.entity.MetrcFacilityVO
import com.techvvs.inventory.service.metrc.model.entity.PagedEmployeesVO
import com.techvvs.inventory.service.metrc.model.entity.LocationVO
import com.techvvs.inventory.service.metrc.model.entity.PagedLocationsVO
import com.techvvs.inventory.service.metrc.model.entity.PagedLocationTypesVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MetrcGlobalImpl implements MetrcGlobal{

    @Autowired
    MetrcAdapter metrcAdapter

    @Autowired
    MetrcFacilityDtoMapper metrcFacilityDtoMapper

    @Autowired
    MetrcEmployeeDtoMapper metrcEmployeeDtoMapper

    @Autowired
    MetrcLocationDtoMapper metrcLocationDtoMapper

    @Autowired
    MetrcLocationTypeDtoMapper metrcLocationTypeDtoMapper

    @Override
    List<MetrcIncomingTransferDto> getIncomingTransfers() {
        metrcAdapter.getIncomingTransfers()
    }

    @Override
    List<MetrcFacilityVO> getFacilities() throws Exception {

        // call the metrc api to get the facilities
        List<MetrcFacilityDto> metrcFacilityDtos = metrcAdapter.getFacilities()

        // map the metrcFacilityDtos to metrcFacilityVOs
        List<MetrcFacilityVO> metrcFacilityVOs = metrcFacilityDtoMapper.map(metrcFacilityDtos)
        return metrcFacilityVOs
    }

    @Override
    PagedEmployeesVO getEmployees(String licenseNumber, int pageNumber, int pageSize) throws Exception {
        // call the metrc api to get the employees with pagination
        PagedEmployeesDto pagedEmployeesDto = metrcAdapter.getEmployees(licenseNumber, pageNumber, pageSize)

        // map the pagedEmployeesDto to pagedEmployeesVO
        PagedEmployeesVO pagedEmployeesVO = metrcEmployeeDtoMapper.map(pagedEmployeesDto)
        return pagedEmployeesVO

    }

    @Override
    PagedLocationsVO getActiveLocations(String licenseNumber) throws Exception {
        // call the metrc api to get the active locations
        PagedLocationsDto pagedLocationsDto = metrcAdapter.getActiveLocations(licenseNumber)

        // map the pagedLocationsDto to pagedLocationsVO
        PagedLocationsVO pagedLocationsVO = metrcLocationDtoMapper.map(pagedLocationsDto)
        return pagedLocationsVO
    }

    @Override
    PagedLocationsVO getInactiveLocations(String licenseNumber) throws Exception {
        // call the metrc api to get the inactive locations
        PagedLocationsDto pagedLocationsDto = metrcAdapter.getInactiveLocations(licenseNumber)

        // map the pagedLocationsDto to pagedLocationsVO
        PagedLocationsVO pagedLocationsVO = metrcLocationDtoMapper.map(pagedLocationsDto)
        return pagedLocationsVO
    }

    @Override
    LocationVO createLocation(LocationDto locationDto) throws Exception {
        // call the metrc api to create a location
        LocationDto createdLocationDto = metrcAdapter.createLocation(locationDto)

        // map the locationDto to locationVO
        LocationVO locationVO = metrcLocationDtoMapper.mapLocation(createdLocationDto)
        return locationVO
    }

    @Override
    LocationVO updateLocation(LocationDto locationDto) throws Exception {
        // call the metrc api to update a location
        LocationDto updatedLocationDto = metrcAdapter.updateLocation(locationDto)

        // map the locationDto to locationVO
        LocationVO locationVO = metrcLocationDtoMapper.mapLocation(updatedLocationDto)
        return locationVO
    }

    @Override
    void archiveLocation(Long locationId, String licenseNumber) throws Exception {
        // call the metrc api to archive a location
        metrcAdapter.archiveLocation(locationId, licenseNumber)
    }

    @Override
    PagedLocationTypesVO getLocationTypes(String licenseNumber) throws Exception {
        // call the metrc api to get the location types with pagination
        PagedLocationTypesDto pagedLocationTypesDto = metrcAdapter.getLocationTypes(licenseNumber, 1, 20)

        // map the pagedLocationTypesDto to pagedLocationTypesVO
        PagedLocationTypesVO pagedLocationTypesVO = metrcLocationTypeDtoMapper.map(pagedLocationTypesDto)
        return pagedLocationTypesVO
    }
}
