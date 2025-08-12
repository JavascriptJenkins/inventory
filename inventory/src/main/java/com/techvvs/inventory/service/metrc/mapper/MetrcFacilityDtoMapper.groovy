package com.techvvs.inventory.service.metrc.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.techvvs.inventory.service.metrc.model.dto.MetrcFacilityDto
import com.techvvs.inventory.service.metrc.model.entity.MetrcFacilityVO
import org.springframework.stereotype.Component

@Component
class MetrcFacilityDtoMapper {

    private static final ObjectMapper M = new ObjectMapper();

    public List<MetrcFacilityVO> map(List<MetrcFacilityDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return new ArrayList<>();

        List<MetrcFacilityVO> out = new ArrayList<>(dtos.size());
        for (MetrcFacilityDto dto : dtos) {
            MetrcFacilityVO vo = new MetrcFacilityVO();

            // Scalars
            vo.setFacilityId(dto.getFacilityId());
            vo.setHireDate(dto.getHireDate());
            vo.setIsOwner(dto.getIsOwner());
            vo.setIsManager(dto.getIsManager());
            vo.setIsFinancialContact(dto.getIsFinancialContact());
            vo.setEmail(dto.getEmail());
            vo.setName(dto.getName());
            vo.setAlias(dto.getAlias());
            vo.setDisplayName(dto.getDisplayName());
            vo.setCredentialedDate(dto.getCredentialedDate());
            vo.setSupportActivationDate(dto.getSupportActivationDate());
            vo.setSupportExpirationDate(dto.getSupportExpirationDate());

            // Collections (defensive copy)
            vo.setOccupations(dto.getOccupations() != null ? new ArrayList<>(dto.getOccupations()) : null);

            // Nested classes (simple, field-by-field via Jackson)
            vo.setFacilityType(dto.getFacilityType() == null ? null :
                    M.convertValue(dto.getFacilityType(), MetrcFacilityVO.FacilityType.class));

            vo.setLicense(dto.getLicense() == null ? null :
                    M.convertValue(dto.getLicense(), MetrcFacilityVO.License.class));

            out.add(vo);
        }
        return out;
    }





}
