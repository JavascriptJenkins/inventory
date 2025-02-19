package com.techvvs.inventory.metrc;

import com.techvvs.inventory.metrc.model.MetrcItemDto;
import com.techvvs.inventory.metrc.model.MetrcPackageDto;
import com.techvvs.inventory.metrc.model.MetrcProductDto;
import com.techvvs.inventory.metrc.service.MetrcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/metrc")
public class MetrcController {

    private final MetrcService metrcService;

    public MetrcController(MetrcService metrcService) {
        this.metrcService = metrcService;
    }

    @PostMapping("/products")
    public void createProduct(@RequestBody MetrcProductDto productDto) {
        metrcService.createProduct(productDto);
    }

    @PostMapping("/items")
    public void createItem(@RequestBody MetrcItemDto itemDto) {
        metrcService.createItem(itemDto);
    }

    @PostMapping("/packages")
    public void createPackage(@RequestBody MetrcPackageDto packageDto) {
        metrcService.createPackage(packageDto);
    }

    @GetMapping("/products")
    public List<MetrcProductDto> getProducts(@RequestParam int page, @RequestParam int size) {
        return metrcService.getProducts(page, size);
    }

    @GetMapping("/inventory")
    public List<MetrcPackageDto> getInventory(@RequestParam int page, @RequestParam int size) {
        return metrcService.getInventory(page, size);
    }

    @GetMapping("/items")
    public List<MetrcItemDto> getItems() {
        return metrcService.getItems();
    }
}
