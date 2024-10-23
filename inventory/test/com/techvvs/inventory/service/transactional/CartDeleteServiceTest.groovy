package com.techvvs.inventory.service.transactional

import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.DeliveryVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.PackageVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.service.controllers.CartService
import com.techvvs.inventory.service.controllers.ProductService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

class CartDeleteServiceTest extends Specification {

    CartDeleteService cartDeleteService

    CartRepo cartRepo = Mock(CartRepo)
    ProductRepo productRepo = Mock(ProductRepo)
    CustomerRepo customerRepo = Mock(CustomerRepo)
    CartService cartService = Mock(CartService)
    BarcodeHelper barcodeHelper = Mock(BarcodeHelper)
    ProductService productService = Mock(ProductService)

    def setup(){
        cartDeleteService = new CartDeleteService(
                cartRepo:cartRepo,
                productRepo:productRepo,
                customerRepo:customerRepo,
                cartService:cartService,
                barcodeHelper:barcodeHelper,
                productService:productService
        )
    }


    def "SaveCartIfNew - test saving new cart"() {

        setup:
        // 2. Create a specific LocalDateTime (e.g., 2024-10-19 at 14:30)
        LocalDateTime specificDateTime = LocalDateTime.of(2024, 10, 19, 14, 30);
        System.out.println("Specific Date and Time: " + specificDateTime);



        List productlist = new ArrayList<ProductVO>()
        productlist.add(new ProductVO(
                product_id: 34,
                name: "Gelato",
                description: "real good",
                batch: Mock(BatchVO),
                producttypeid: Mock(ProductTypeVO),
                cart_list: null,
                transaction_list: null,
                package_list: null,
                menu_list: null,
                productnumber: 7489333,
                quantity: 403,
                vendorquantity: 403,
                quantityremaining: 403,
                notes: "we love it",
                vendor: "Barry",
                bagcolor: "blue",
                crate: 5,
                crateposition: "CP05",
                barcode: "048739101018",
                price: 450.56,
                cost: 350.56,
                salePrice: 400.00,
                laborCostPricePerUnit: 100,
                marginPercent: 10,
                weight: 50.00,
                updateTimeStamp:specificDateTime,
                createTimeStamp: specificDateTime
        ))

        CartVO cartVO = new CartVO(
                cartid: 0,
                product_cart_list: productlist,
                discount: null,
                customer: Mock(CustomerVO),
                delivery: null,
                barcode: "048739101018",
                displayquantitytotal: 10,
                isprocessed: 0,
                quantityselected: 5,
                updateTimeStamp: null,
                createTimeStamp: null
        )

        CustomerVO customerVO = new CustomerVO(customerid: 3544)
        Optional<CustomerVO> customerOptional = Optional.of(customerVO)


        when:
        cartDeleteService.saveCartIfNew(cartVO)

        then:
        1 * cartService.doesCartExist(cartVO) >> false
        1 * cartVO.customer.getCustomerid() >> 3544
        1 * customerRepo.findById(3544) >> customerOptional
        1 * cartRepo.save(cartVO) >> cartVO

        0*_._


    }
}
