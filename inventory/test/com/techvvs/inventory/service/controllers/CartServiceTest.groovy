package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.DiscountVO
import com.techvvs.inventory.model.ProductVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.Model
import spock.lang.Specification

import java.time.LocalDateTime

class CartServiceTest extends Specification {

    ProductRepo productRepo = Mock(ProductRepo)
    CartRepo cartRepo= Mock(CartRepo)
    CustomerRepo customerRepo= Mock(CustomerRepo)
    DiscountService discountService= Mock(DiscountService)

    CartService cartService

    def setup(){
        cartService = new CartService(
                productRepo:productRepo,
                cartRepo:cartRepo,
                customerRepo:customerRepo,
                discountService:discountService

        )
    }


    def "ApplyAdhocDiscount"() {

        setup:

        List<ProductVO> product_cart_list = new ArrayList<>()
        LocalDateTime dateTime = LocalDateTime.of(2024, 10, 21, 10, 15, 30);

        DiscountVO discountVO = new DiscountVO(
                discountid: 232,
                discountamount: 100,
                discountpercentage: 0.0,
                name: "100 off discount",
                description: "oh we love discount",
                updateTimeStamp: dateTime,
                createTimeStamp:dateTime

        )

        CustomerVO customerVO = new CustomerVO(
                customerid: 343,
                name: "billy"
        )

        CartVO cartVO = new CartVO(
                discount: discountVO,
                barcode:"048739801017",
                product_cart_list: product_cart_list,
                customer: customerVO,
                cartid: 45,
                total: 1000
        )


        Optional<CartVO> existingcartoptional = Optional.of(cartVO)
        CartVO existingcart = existingcartoptional.get()



        when:
        cartService.applyAdhocDiscount(cartVO)

        then:
        1 * cartRepo.findById(45) >> existingcartoptional
        1 * discountService.createAdhocDiscount(cartVO.discount) >> discountVO
        1 * discountService.applyDiscountToCart(cartVO) >> existingcart
        1 * cartRepo.save(existingcart)

        0*_._


    }


    def "removeDiscount"() {

        setup:
        LocalDateTime dateTime = LocalDateTime.of(2024, 10, 21, 10, 15, 30);


        ProductVO productVO = new ProductVO(
                product_id: 34,
                name:"Gelato 41",
                price: 550.00,
                quantity: 50,
                quantityremaining: 40,
                vendor: "Lily Farms,",
                vendorquantity: 50,
                createTimeStamp: dateTime,
                updateTimeStamp: dateTime

        )
        ProductVO productVO2 = new ProductVO(
                product_id: 68,
                name:"Sour Donkey",
                price: 1827.32,
                quantity: 50,
                quantityremaining: 40,
                vendor: "Lily Farms,",
                vendorquantity: 50,
                createTimeStamp: dateTime,
                updateTimeStamp: dateTime
        )
        List<ProductVO> product_cart_list = new ArrayList<>()
        product_cart_list.add(productVO)
        product_cart_list.add(productVO2)



        DiscountVO discountVO = new DiscountVO(
                discountid: 232,
                discountamount: 100,
                discountpercentage: 0.0,
                name: "100 off discount",
                description: "oh we love discount",
                updateTimeStamp: dateTime,
                createTimeStamp:dateTime

        )

        CustomerVO customerVO = new CustomerVO(
                customerid: 343,
                name: "billy"
        )

        CartVO cartVO = new CartVO(
                discount: discountVO,
                barcode:"048739801017",
                product_cart_list: product_cart_list,
                customer: customerVO,
                cartid: 45,
                total: 1000
        )


        Optional<CartVO> existingcartoptional = Optional.of(cartVO)
        CartVO existingcart = existingcartoptional.get()



        when:
        cartService.removeDiscount(cartVO)

        then:
        1 * cartRepo.findById(45) >> existingcartoptional
        1 * cartRepo.save(existingcart)

        0*_._


    }

    def "searchForProductByBarcode"() {

        setup:
        LocalDateTime dateTime = LocalDateTime.of(2024, 10, 21, 10, 15, 30);

        ProductVO productVO = new ProductVO(
                product_id: 34,
                name:"Gelato 41",
                price: 550.00,
                quantity: 50,
                quantityremaining: 40,
                vendor: "Lily Farms,",
                vendorquantity: 50,
                createTimeStamp: dateTime,
                updateTimeStamp: dateTime

        )
        ProductVO productVO2 = new ProductVO(
                product_id: 68,
                name:"Sour Donkey",
                price: 1827.32,
                quantity: 50,
                quantityremaining: 40,
                vendor: "Lily Farms,",
                vendorquantity: 50,
                createTimeStamp: dateTime,
                updateTimeStamp: dateTime
        )
        List<ProductVO> product_cart_list = new ArrayList<>()
        product_cart_list.add(productVO)
        product_cart_list.add(productVO2)



        DiscountVO discountVO = new DiscountVO(
                discountid: 232,
                discountamount: 100,
                discountpercentage: 0.0,
                name: "100 off discount",
                description: "oh we love discount",
                updateTimeStamp: dateTime,
                createTimeStamp:dateTime

        )

        CustomerVO customerVO = new CustomerVO(
                customerid: 343,
                name: "billy"
        )

        CartVO cartVO = new CartVO(
                discount: discountVO,
                barcode:"048739801017",
                product_cart_list: product_cart_list,
                customer: customerVO,
                cartid: 45,
                total: 1000
        )


        Optional<CartVO> existingcartoptional = Optional.of(cartVO)
        Optional<ProductVO> productoptional = Optional.of(productVO)
        ProductVO savedProduct = new ProductVO(
                product_id: 34,
                name:"Gelato 41",
                price: 550.00,
                quantity: 50,
                quantityremaining: 40,
                vendor: "Lily Farms,",
                vendorquantity: 50,
                createTimeStamp: dateTime,
                updateTimeStamp: dateTime)
        CartVO existingcart = existingcartoptional.get()

        Model model = Mock(Model)
        Optional<Integer> page = Optional.of(0)
        Optional<Integer> size = Optional.of(5)

        when:
        cartService.searchForProductByBarcode(
                cartVO,
                model,
                page,
                size
        )

        then:
        1 * productRepo.findByBarcode(cartVO.barcode) >> productoptional
        1 * productRepo.save(productoptional.get()) >> savedProduct
        1 * cartRepo.save(cartVO) >> cartVO
        1 * model.addAttribute('successMessage', 'Product: Gelato 41 added successfully')

        0*_._


    }
}
