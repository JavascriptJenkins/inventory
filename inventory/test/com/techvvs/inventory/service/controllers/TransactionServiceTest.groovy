package com.techvvs.inventory.service.controllers

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.PackageRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.DiscountVO
import com.techvvs.inventory.model.PackageVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.util.FormattingUtil
import com.techvvs.inventory.util.TechvvsAppUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.ui.Model
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

class TransactionServiceTest extends Specification {


    TransactionService transactionService

    TransactionRepo transactionRepo = Mock(TransactionRepo)
    CartRepo cartRepo = Mock(CartRepo)
    ProductService productService = Mock(ProductService)
    TechvvsAppUtil techvvsAppUtil = Mock(TechvvsAppUtil)
    FormattingUtil formattingUtil = Mock(FormattingUtil)
    AppConstants appConstants = Mock(AppConstants)
    Environment environment = Mock(Environment)
    PackageRepo packageRepo = Mock(PackageRepo)
    CartService cartService = Mock(CartService)
    DiscountService discountService = Mock(DiscountService)


    def setup(){
        transactionService = new TransactionService(
                transactionRepo:transactionRepo,
                cartRepo:cartRepo,
                productService:productService,
                techvvsAppUtil:techvvsAppUtil,
                formattingUtil:formattingUtil,
                appConstants:appConstants,
                environment:environment,
                packageRepo:packageRepo,
                cartService:cartService,
                discountService:discountService
        )
    }


    def "ProcessCartGenerateNewTransaction"() {


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


        TransactionVO newtransaction = new TransactionVO(
                transactionid: null,
                product_list: product_cart_list,
                cart: cartVO,
                updateTimeStamp: dateTime,
                createTimeStamp: dateTime,
                customervo: cartVO.customer,
                discount: cartVO.discount,
                total: cartVO.total,
                originalprice: 2377.32,
                totalwithtax: 2377.32,
//                totalwithtax: cartVO.total,
                paid: 0.00,
                taxpercentage: 0, // we are not going to set a tax percentage here in non dev environments
                isprocessed: 0
        )



        when:
        transactionService.processCartGenerateNewTransaction(cartVO)


        // todo: find out why the formatting util methods are not being called?
        // todo: I think this is because they are static methods??
        then:
        1 * environment.getProperty('tax.percentage', Double.class) >> 0.00
       // 1 * formattingUtil.calculateTotalDiscountPercentage(cartVO) >> 0
        1 * cartService.calculateTotalPriceOfProductList(cartVO.product_cart_list) >> 2377.32
        1 * techvvsAppUtil.isDev1() >> false
        1 * transactionRepo.save(_) >> newtransaction
        1 * productService.findProductByID(productVO) >> productVO
        1 * productService.saveProduct(productVO)
        1 * productService.findProductByID(productVO2) >> productVO2
        1 * productService.saveProduct(productVO2) >> productVO2
        1 * productService.saveProductAssociations(newtransaction)
        1 * cartRepo.save(newtransaction.cart)

        0* _._


    }


    @Unroll
    def "getAggregatedProductList should return a list with unique barcodes"() {
        given:
        TransactionVO transactionVO = new TransactionVO(product_list: products)

        expect:
        transactionService.getAggregatedProductList(transactionVO).size() == expectedSize

        where:
        products                                                          | expectedSize
        [new ProductVO(barcode: "123"), new ProductVO(barcode: "123")]    | 1
        [new ProductVO(barcode: "123"), new ProductVO(barcode: "456")]    | 2
        []                                                                | 0
    }

    @Unroll
    def "getAggregatedCartProductList should return a list with unique barcodes from cart"() {
        given:
        CartVO cartVO = new CartVO(product_cart_list: products)

        expect:
        transactionService.getAggregatedCartProductList(cartVO).size() == expectedSize

        where:
        products                                                          | expectedSize
        [new ProductVO(barcode: "123"), new ProductVO(barcode: "123")]    | 1
        [new ProductVO(barcode: "123"), new ProductVO(barcode: "456")]    | 2
        []                                                                | 0
    }

    @Unroll
    def "getAggregatedPackageProductList should return a list with unique barcodes from package"() {
        given:
        PackageVO packageVO = new PackageVO(product_package_list: products)

        expect:
        transactionService.getAggregatedPackageProductList(packageVO).size() == expectedSize

        where:
        products                                                          | expectedSize
        [new ProductVO(barcode: "123"), new ProductVO(barcode: "123")]    | 1
        [new ProductVO(barcode: "123"), new ProductVO(barcode: "456")]    | 2
        []                                                                | 0
    }

}
