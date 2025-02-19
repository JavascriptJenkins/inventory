package com.techvvs.inventory.metrc.service;

import com.techvvs.inventory.metrc.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MetrcService {
    void createProduct(MetrcProductDto productDto);
    void createItem(MetrcItemDto itemDto);
    void createPackage(MetrcPackageDto packageDto);
    List<MetrcProductDto> getProducts(int page, int size);
    List<MetrcPackageDto> getInventory(int page, int size);
    List<MetrcItemDto> getItems();
    void transferPackage(MetrcTransferDto transferDto);
    void sellPackage(MetrcSaleDto saleDto);
    List<MetrcIncomingTransferDto> getIncomingTransfers();
    void acceptIncomingTransfer(String transferId) throws Exception;
    void recordTransportLog(MetrcTransportLogDto transportLogDto) throws Exception;
    MetrcComplianceCheckDto checkCompliance(String entityId) throws Exception;





}