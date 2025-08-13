// Unit Tests using Spock
package com.techvvs.inventory.metrc.adapter

import com.techvvs.inventory.metrc.impl.MetrcServiceImpl
import com.techvvs.inventory.service.metrc.model.dto.MetrcItemDto
import com.techvvs.inventory.service.metrc.model.dto.MetrcPackageDto
import com.techvvs.inventory.service.metrc.model.dto.MetrcProductDto
import com.techvvs.inventory.metrc.service.MetrcService
import com.techvvs.inventory.service.metrc.adapter.MetrcAdapter
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import spock.lang.Specification;

@SpringBootTest
class MetrcServiceSpec extends Specification {

    @Autowired
    MetrcService metrcService;
    @Autowired
    MetrcAdapter metrcAdapter;

    def setup() {
        metrcAdapter = Mock(MetrcAdapter)
        metrcService = new MetrcServiceImpl(metrcAdapter)
    }

    def "test create product"() {
        given: "A valid product DTO"
        def productDto = new MetrcProductDto(name: "Test Product", category: "Usable Marijuana", unit: "Each", strain: "Hybrid", thcContent: 18.5, cbdContent: 0.5)

        when: "createProduct is called"
        metrcService.createProduct(productDto)

        then: "MetrcAdapter createProduct should be called once"
        1 * metrcAdapter.createProduct(productDto)
    }

    def "test create item"() {
        given: "A valid item DTO"
        def itemDto = new MetrcItemDto(name: "Test Item", category: "Flower", unit: "Grams")

        when: "createItem is called"
        metrcService.createItem(itemDto)

        then: "MetrcAdapter createItem should be called once"
        1 * metrcAdapter.createItem(itemDto)
    }

    def "test create package"() {
        given: "A valid package DTO"
        def packageDto = new MetrcPackageDto(tag: "1A4FF000000002300000331", item: "Test Package", quantity: 100, unitOfMeasure: "Each")

        when: "createPackage is called"
        metrcService.createPackage(packageDto)

        then: "MetrcAdapter createPackage should be called once"
        1 * metrcAdapter.createPackage(packageDto)
    }
}
