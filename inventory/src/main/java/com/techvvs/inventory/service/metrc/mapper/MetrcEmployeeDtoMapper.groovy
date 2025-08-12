package com.techvvs.inventory.service.metrc.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.techvvs.inventory.service.metrc.model.dto.EmployeeDto
import com.techvvs.inventory.service.metrc.model.dto.LicenseDto
import com.techvvs.inventory.service.metrc.model.dto.PagedEmployeesDto
import com.techvvs.inventory.service.metrc.model.entity.EmployeeVO
import com.techvvs.inventory.service.metrc.model.entity.LicenseVO
import com.techvvs.inventory.service.metrc.model.entity.PagedEmployeesVO
import org.springframework.stereotype.Component
import java.util.ArrayList
import java.util.List


@Component
class MetrcEmployeeDtoMapper {

    private static final ObjectMapper M = new ObjectMapper();

    public PagedEmployeesVO map(PagedEmployeesDto dto) {
        if (dto == null) return null;
        PagedEmployeesVO vo = new PagedEmployeesVO();

        // Pagination/meta
        vo.setTotal(dto.getTotal());
        vo.setTotalRecords(dto.getTotalRecords());
        vo.setPageSize(dto.getPageSize());
        vo.setRecordsOnPage(dto.getRecordsOnPage());
        vo.setPage(dto.getPage());
        vo.setCurrentPage(dto.getCurrentPage());
        vo.setTotalPages(dto.getTotalPages());

        // Data list
        vo.setData(mapEmployees(dto.getData()));
        return vo;
    }

    public List<EmployeeVO> mapEmployees(List<EmployeeDto> list) {
        if (list == null) return null;
        List<EmployeeVO> out = new ArrayList<>(list.size());
        for (EmployeeDto e : list) {
            out.add(mapEmployee(e));
        }
        return out;
    }

    public EmployeeVO mapEmployee(EmployeeDto e) {
        if (e == null) return null;
        EmployeeVO v = new EmployeeVO();
        v.setFullName(e.getFullName());
        v.setIndustryAdmin(e.isIndustryAdmin());
        v.setOwner(e.isOwner());
        v.setManager(e.isManager());
        v.setLicense(mapLicense(e.getLicense()));
        return v;
    }

    public LicenseVO mapLicense(LicenseDto l) {
        if (l == null) return null;
        LicenseVO v = new LicenseVO();
        v.setNumber(l.getNumber());
        v.setEmployeeLicenseNumber(l.getEmployeeLicenseNumber());
        v.setStartDate(l.getStartDate());
        v.setEndDate(l.getEndDate());
        v.setLicenseType(l.getLicenseType());
        return v;
    }




}
