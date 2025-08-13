package com.techvvs.inventory.service.metrc.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.techvvs.inventory.service.metrc.model.dto.LocationDto
import com.techvvs.inventory.service.metrc.model.dto.PagedLocationsDto
import com.techvvs.inventory.service.metrc.model.entity.LocationVO
import com.techvvs.inventory.service.metrc.model.entity.PagedLocationsVO
import org.springframework.stereotype.Component
import java.util.ArrayList
import java.util.List

@Component
class MetrcLocationDtoMapper {

    private static final ObjectMapper M = new ObjectMapper();

    public PagedLocationsVO map(PagedLocationsDto dto) {
        if (dto == null) return null;
        PagedLocationsVO vo = new PagedLocationsVO();

        // Pagination/meta
        vo.setTotal(dto.getTotal());
        vo.setTotalRecords(dto.getTotalRecords());
        vo.setPageSize(dto.getPageSize());
        vo.setRecordsOnPage(dto.getRecordsOnPage());
        vo.setPage(dto.getPage());
        vo.setCurrentPage(dto.getCurrentPage());
        vo.setTotalPages(dto.getTotalPages());

        // Data list
        vo.setData(mapLocations(dto.getData()));
        return vo;
    }

    public List<LocationVO> mapLocations(List<LocationDto> list) {
        if (list == null) return null;
        List<LocationVO> out = new ArrayList<>(list.size());
        for (LocationDto l : list) {
            out.add(mapLocation(l));
        }
        return out;
    }

    public LocationVO mapLocation(LocationDto l) {
        if (l == null) return null;
        LocationVO v = new LocationVO();
        v.setId(l.getId());
        v.setName(l.getName());
        v.setLocationTypeId(l.getLocationTypeId());
        v.setLocationTypeName(l.getLocationTypeName());
        v.setForPlantBatches(l.isForPlantBatches());
        v.setForPlants(l.isForPlants());
        v.setForHarvests(l.isForHarvests());
        v.setForPackages(l.isForPackages());
        v.setLicenseNumber(l.getLicenseNumber());
        return v;
    }
}
