package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.jparepo.ProductRepo
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.techvvs.inventory.jparepo.ProductTypeRepo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/product")
class ProductController {

    @Autowired
    ProductRepo productRepository

    @Autowired
    ProductTypeRepo productTypeRepository

    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();;


    private final Logger logger = LoggerFactory.getLogger(this.getClass())


    @GetMapping("/getAll")
    String getAll() {

        logger.info("ProductController.getAll HIT!!!")
        String payload = executeGetAll()
        return payload
    }

    String executeGetAll(){
        return mapper.writeValueAsString(productRepository.findAll())
    }

    @GetMapping("/getAllProductTypes")
    String getAllProductTypes() {

        logger.info("ProductController.getAllProductTypes HIT!!!")
        String payload = executeGetAllProductTypes()
        return payload
    }

    String executeGetAllProductTypes(){
        return mapper.writeValueAsString(productTypeRepository.findAll())
    }

    @PostMapping("/edit")
    String edit(@RequestBody String json) {

        logger.info("ProductController.edit HIT!!!")

        logger.info("json: ",json.toString())

        JsonNode root = mapper.readTree(json);
        ProductVO productVO = mapper.treeToValue(root, ProductVO.class)

        String payload = executeEdit(productVO)

        return payload
    }

    String executeEdit(ProductVO productVO){
        // for creates
        if(productVO.createTimeStamp == null && productVO.updateTimeStamp == null){
            productVO.setUpdateTimeStamp(new Date())
            productVO.setCreateTimeStamp(new Date())
        }
        // for updates
        if(productVO.createTimeStamp == productVO.updateTimeStamp || productVO.updateTimeStamp > productVO.createTimeStamp){
            productVO.setUpdateTimeStamp(new Date())
        }
        return mapper.writeValueAsString(productRepository.save(productVO))
    }
    

}
