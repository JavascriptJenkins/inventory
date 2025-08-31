package com.techvvs.inventory.service.metrc.adapter;

import com.techvvs.inventory.service.metrc.constants.MetrcCallEnum;
import com.techvvs.inventory.metrc.map.MetrcUriMapping;
import com.techvvs.inventory.model.nonpersist.RequestMetaData;
import com.techvvs.inventory.service.metrc.model.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import com.techvvs.inventory.service.metrc.model.dto.LocationDto;

@Component
public class MetrcAdapter {


    private static final Logger logger = LoggerFactory.getLogger(MetrcAdapter.class);

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    MetrcUriMapping metrcUriMapping;

    public void createProduct(MetrcProductDto productDto) {
        try {
            String apiUrl = "https://api-mn.metrc.com/products/v1/create";
            restTemplate.postForObject(apiUrl, productDto, String.class);
        } catch (HttpClientErrorException e) {
            logger.error("Error creating product: {}", e.getMessage());
        }
    }

    public void createItem(MetrcItemDto itemDto) {
        try {
            String apiUrl = "https://api-mn.metrc.com/items/v1/create";
            restTemplate.postForObject(apiUrl, itemDto, String.class);
        } catch (HttpClientErrorException e) {
            logger.error("Error creating item: {}", e.getMessage());
        }
    }

    public void createPackage(MetrcPackageDto packageDto) {
        try {
            String apiUrl = "https://api-mn.metrc.com/packages/v1/create";
            restTemplate.postForObject(apiUrl, packageDto, String.class);
        } catch (HttpClientErrorException e) {
            logger.error("Error creating package: {}", e.getMessage());
        }
    }

    public List<MetrcProductDto> getProducts(int page, int size) {
        try {
            String apiUrl = "https://api-mn.metrc.com/products/v1?page=" + page + "&size=" + size;
            MetrcProductDto[] products = restTemplate.getForObject(apiUrl, MetrcProductDto[].class);
            return Arrays.asList(products);
        } catch (HttpClientErrorException e) {
            logger.error("Error fetching products: {}", e.getMessage());
            return List.of();
        }
    }

    public List<MetrcPackageDto> getInventory(int page, int size) {
        try {
            String apiUrl = "https://api-mn.metrc.com/packages/v1/active?page=" + page + "&size=" + size;
            MetrcPackageDto[] inventory = restTemplate.getForObject(apiUrl, MetrcPackageDto[].class);
            return Arrays.asList(inventory);
        } catch (HttpClientErrorException e) {
            logger.error("Error fetching inventory: {}", e.getMessage());
            return List.of();
        }
    }

    public List<MetrcItemDto> getItems() {
        try {
            String apiUrl = "https://api-mn.metrc.com/items/v1";
            MetrcItemDto[] items = restTemplate.getForObject(apiUrl, MetrcItemDto[].class);
            return Arrays.asList(items);
        } catch (HttpClientErrorException e) {
            logger.error("Error fetching items: {}", e.getMessage());
            return List.of();
        }
    }


    public void transferPackage(MetrcTransferDto transferDto) {
        try {
            String apiUrl = "https://api-mn.metrc.com/transfers/v1/external/outgoing";
            restTemplate.postForObject(apiUrl, transferDto, String.class);
        } catch (HttpClientErrorException e) {
            logger.error("Error transferring package: {}", e.getResponseBodyAsString());
        }
    }

    public void sellPackage(MetrcSaleDto saleDto) {
        try {
            String apiUrl = "https://api-mn.metrc.com/sales/v1/receipts";
            restTemplate.postForObject(apiUrl, saleDto, String.class);
        } catch (HttpClientErrorException e) {
            logger.error("Error selling package: {}", e.getResponseBodyAsString());
        }
    }

    public List<MetrcIncomingTransferDto> getIncomingTransfers() {
        String apiUrl = "https://api-mn.metrc.com/transfers/v1/external/incoming";
        return Arrays.asList(restTemplate.getForObject(apiUrl, MetrcIncomingTransferDto[].class));
    }

    public void acceptIncomingTransfer(String transferId) throws Exception {
        String apiUrl = "https://api-mn.metrc.com/transfers/v1/external/incoming/" + transferId + "/accept";
        restTemplate.postForObject(apiUrl, null, String.class);
    }

    public void recordTransportLog(MetrcTransportLogDto transportLogDto) {
        String apiUrl = "https://api-mn.metrc.com/transports/v1/logs";
        restTemplate.postForObject(apiUrl, transportLogDto, String.class);
    }

    public MetrcComplianceCheckDto checkCompliance(String entityId) {
        String apiUrl = "https://sandbox-api-mn.metrc.com/compliance/v1/check?entityId=" + entityId;
        return restTemplate.getForObject(apiUrl, MetrcComplianceCheckDto.class);
    }

    public List<MetrcFacilityDto> getFacilities() {
        RequestMetaData meta = metrcUriMapping.getMetadata(MetrcCallEnum.GET_FACILITIES);
        String fullUrl = meta.getBaseUri() + meta.getUri();
        return Arrays.asList(restTemplate.getForObject(fullUrl, MetrcFacilityDto[].class));
    }

    public PagedEmployeesDto getEmployees() {
        RequestMetaData meta = metrcUriMapping.getMetadata(MetrcCallEnum.GET_EMPLOYEES);
        String fullUrl = meta.getBaseUri() + meta.getUri();
        return restTemplate.getForObject(fullUrl, PagedEmployeesDto.class);
    }

//    public MetrcReceiveTransferDto receiveTransfer(List<MetrcReceiveTransferDto> transferDtos, String licenseNumber) {
//        RequestMetaData meta = metrcUriMapping.getMetadata(MetrcCallEnum.POST_RECEIVE_TRANSFER);
//        String fullUrl = meta.getBaseUri() + meta.getUri();
//
//        return restTemplate.postForObject(fullUrl, meta.getResponseObjectType(), String.class);
//    }


}
