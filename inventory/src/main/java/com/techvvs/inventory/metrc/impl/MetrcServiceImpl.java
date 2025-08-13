package com.techvvs.inventory.metrc.impl;

import com.techvvs.inventory.service.metrc.adapter.MetrcAdapter;
import com.techvvs.inventory.metrc.service.MetrcService;
import com.techvvs.inventory.service.metrc.model.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetrcServiceImpl implements MetrcService {

    @Autowired
    MetrcAdapter metrcAdapter;

    @Override
    public void createProduct(MetrcProductDto productDto) {
        metrcAdapter.createProduct(productDto);
    }

    @Override
    public void createItem(MetrcItemDto itemDto) {
        metrcAdapter.createItem(itemDto);
    }

    @Override
    public void createPackage(MetrcPackageDto packageDto) {
        metrcAdapter.createPackage(packageDto);
    }

    @Override
    public List<MetrcProductDto> getProducts(int page, int size) {
        return metrcAdapter.getProducts(page, size);
    }

    @Override
    public List<MetrcPackageDto> getInventory(int page, int size) {
        return metrcAdapter.getInventory(page, size);
    }

    @Override
    public List<MetrcItemDto> getItems() {
        return metrcAdapter.getItems();
    }

    @Override
    public void transferPackage(MetrcTransferDto transferDto) {
        metrcAdapter.transferPackage(transferDto);
    }

    @Override
    public void sellPackage(MetrcSaleDto saleDto) {
        metrcAdapter.sellPackage(saleDto);
    }

    @Override
    public void acceptIncomingTransfer(String transferId) throws Exception {
        metrcAdapter.acceptIncomingTransfer(transferId);
    }

    @Override
    public List<MetrcIncomingTransferDto> getIncomingTransfers() {
        return metrcAdapter.getIncomingTransfers();
    }

    @Override
    public void recordTransportLog(MetrcTransportLogDto transportLogDto) throws Exception {
        metrcAdapter.recordTransportLog(transportLogDto);
    }

    @Override
    public MetrcComplianceCheckDto checkCompliance(String entityId) throws Exception {
        return metrcAdapter.checkCompliance(entityId);
    }

    @Override
    public List<MetrcFacilityDto> getFacilities() throws Exception {
        return metrcAdapter.getFacilities();
    }

}
