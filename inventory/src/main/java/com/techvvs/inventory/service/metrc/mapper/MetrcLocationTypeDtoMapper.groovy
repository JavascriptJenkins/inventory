package com.techvvs.inventory.service.metrc.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.techvvs.inventory.service.metrc.model.dto.LocationTypeDto
import com.techvvs.inventory.service.metrc.model.dto.PagedLocationTypesDto
import com.techvvs.inventory.service.metrc.model.entity.LocationTypeVO
import com.techvvs.inventory.service.metrc.model.entity.PagedLocationTypesVO
import org.springframework.stereotype.Component
import java.util.ArrayList
import java.util.List

@Component
class MetrcLocationTypeDtoMapper {

    private static final ObjectMapper M = new ObjectMapper();

    public PagedLocationTypesVO map(PagedLocationTypesDto dto) {
        if (dto == null) return null;
        PagedLocationTypesVO vo = new PagedLocationTypesVO();

        // Pagination/meta
        vo.setTotal(dto.getTotal());
        vo.setTotalRecords(dto.getTotalRecords());
        vo.setPageSize(dto.getPageSize());
        vo.setRecordsOnPage(dto.getRecordsOnPage());
        vo.setPage(dto.getPage());
        vo.setCurrentPage(dto.getCurrentPage());
        vo.setTotalPages(dto.getTotalPages());

        // Data list
        vo.setData(mapLocationTypes(dto.getData()));
        return vo;
    }

    public List<LocationTypeVO> mapLocationTypes(List<LocationTypeDto> list) {
        if (list == null) return null;
        List<LocationTypeVO> out = new ArrayList<>(list.size());
        for (LocationTypeDto l : list) {
            out.add(mapLocationType(l));
        }
        return out;
    }

    public LocationTypeVO mapLocationType(LocationTypeDto l) {
        if (l == null) return null;
        LocationTypeVO v = new LocationTypeVO();
        v.setId(l.getId());
        v.setName(l.getName());
        return v;
    }
}
