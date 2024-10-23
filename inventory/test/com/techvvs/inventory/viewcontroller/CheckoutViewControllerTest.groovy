package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.dao.BatchDao
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.DiscountVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.printers.PrinterService
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.controllers.CartService
import com.techvvs.inventory.service.controllers.DiscountService
import com.techvvs.inventory.service.controllers.TransactionService
import com.techvvs.inventory.service.paging.FilePagingService
import com.techvvs.inventory.service.transactional.CartDeleteService
import com.techvvs.inventory.util.TechvvsFileHelper
import com.techvvs.inventory.validation.ValidateBatch
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper
import com.techvvs.inventory.viewcontroller.helper.CheckoutHelper
import com.techvvs.inventory.viewcontroller.helper.FileViewHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestParam
import spock.lang.Specification

import java.time.LocalDateTime

class CheckoutViewControllerTest extends Specification {

    CartDeleteService cartDeleteService = Mock(CartDeleteService)
    AppConstants appConstants = Mock(AppConstants)
    FilePagingService filePagingService = Mock(FilePagingService)
    TechvvsFileHelper techvvsFileHelper = Mock(TechvvsFileHelper)
    BatchRepo batchRepo = Mock(BatchRepo)
    BatchDao batchDao = Mock(BatchDao)
    ValidateBatch validateBatch = Mock(ValidateBatch)
    BatchControllerHelper batchControllerHelper = Mock(BatchControllerHelper)
    CheckoutHelper checkoutHelper = Mock(CheckoutHelper)
    CartService cartService = Mock(CartService)
    FileViewHelper fileViewHelper = Mock(FileViewHelper)
    TransactionService transactionService = Mock(TransactionService)
    PrinterService printerService = Mock(PrinterService)
    TechvvsAuthService techvvsAuthService = Mock(TechvvsAuthService)
    DiscountService discountService = Mock(DiscountService)

    CheckoutViewController checkoutViewController

    def setup(){
        checkoutViewController = new CheckoutViewController(
                cartDeleteService:cartDeleteService,
                appConstants:appConstants,
                filePagingService:filePagingService,
                techvvsFileHelper:techvvsFileHelper,
                batchRepo:batchRepo,
                batchDao:batchDao,
                validateBatch:validateBatch,
                batchControllerHelper:batchControllerHelper,
                checkoutHelper:checkoutHelper,
                cartService:cartService,
                fileViewHelper:fileViewHelper,
                transactionService:transactionService,
                printerService:printerService,
                techvvsAuthService:techvvsAuthService,
                discountService:discountService
        )


    }



    def "Scan - test all method calls are called"() {

        given:

        List<ProductVO> product_cart_list = new ArrayList<>()

        CartVO cartVO = new CartVO(
                barcode:"048739801017",
                product_cart_list: product_cart_list,
                customer: Mock(CustomerVO),
                cartid: 45,
                total: 1000
        )
        Model model = Mock(Model)
        Optional<Integer> page = Optional.of(1)
        Optional<Integer> size = Optional.of(5)

        when:
        checkoutViewController.scan(
                cartVO,
                model,
                page,
                size
        )
        then:
        1 * cartDeleteService.validateCartVO(cartVO, model) >> cartVO
        1 * model.getAttribute("errorMessage")
        1 * cartDeleteService.saveCartIfNew(cartVO) >> cartVO
        1 * cartDeleteService.searchForProductByBarcode(cartVO, model, page, size) >> cartVO
        1 * checkoutHelper.hydrateTransientQuantitiesForDisplay(cartVO) >> cartVO
        1 * model.addAttribute("cart", cartVO)
        1 * techvvsAuthService.checkuserauth(model)
        1 * checkoutHelper.getAllCustomers(model)
        0*_._

    }

    def "get - view checkout page - view pending carts"() {

        given:

        List<ProductVO> product_cart_list = new ArrayList<>()

        CartVO cartVO = new CartVO(
                barcode:"048739801017",
                product_cart_list: product_cart_list,
                customer: Mock(CustomerVO),
                cartid: 45,
                total: 1000
        )
        Model model = Mock(Model)

        String cartid = "45"

        Optional<Integer> page = Optional.of(1)
        Optional<Integer> size = Optional.of(5)

        when:
        checkoutViewController.viewPendingTransactions(
                cartVO,
                model,
                page,
                size
        )

        then:
        1 * checkoutHelper.findPendingCarts(model, page, size)
        1 * model.addAttribute("cart", cartVO);
        1 * techvvsAuthService.checkuserauth(model)
        1 * checkoutHelper.getAllCustomers(model)
        0*_._

    }

    def "get - view checkout page - with a cart id and cartvo"() {

        given:

        List<ProductVO> product_cart_list = new ArrayList<>()

        CartVO cartVO = new CartVO(
                barcode:"048739801017",
                product_cart_list: product_cart_list,
                customer: Mock(CustomerVO),
                cartid: 45,
                total: 1000
        )
        Model model = Mock(Model)

        String cartid = "45"

        when:
        checkoutViewController.viewNewForm(
                cartVO,
                model,
                cartid
        )
        then:
        1 * checkoutHelper.loadCartForCheckout(cartid, model, cartVO) >> model
        1 * techvvsAuthService.checkuserauth(model)
        1 * checkoutHelper.getAllCustomers(model)
        0*_._

    }


    def "post - checkout page - delete item from cart"() {

        given:

        List<ProductVO> product_cart_list = new ArrayList<>()

        CartVO cartVO = new CartVO(
                barcode:"048739801017",
                product_cart_list: product_cart_list,
                customer: Mock(CustomerVO),
                cartid: 45,
                total: 1000
        )
        Model model = Mock(Model)

        String barcode = "087310001019"
        String cartid = "45"
        Optional<Integer> page = Optional.of(1)
        Optional<Integer> size = Optional.of(5)

        when:
        checkoutViewController.delete(
                model,
                cartid,
                barcode,
                page,
                size
        )

        then:
        1 * checkoutHelper.getExistingCart(cartid) >> cartVO
        1 * cartDeleteService.deleteProductFromCart(cartVO, barcode) >> cartVO
        1 * checkoutHelper.hydrateTransientQuantitiesForDisplay(cartVO) >> cartVO
        1 * model.addAttribute("cart", cartVO)
        1 * techvvsAuthService.checkuserauth(model)
        1 * checkoutHelper.getAllCustomers(model)
        0*_._

    }

    def "post - checkout page - review cart"() {

        given:

        List<ProductVO> product_cart_list = new ArrayList<>()

        CartVO cartVO = new CartVO(
                barcode:"048739801017",
                product_cart_list: product_cart_list,
                customer: Mock(CustomerVO),
                cartid: 45,
                total: 1000
        )
        Model model = Mock(Model)

        String barcode = "087310001019"
        String cartid = "45"
        Optional<Integer> page = Optional.of(1)
        Optional<Integer> size = Optional.of(5)

        when:
        checkoutViewController.reviewcart(
                cartVO,
                model,
                page,
                size
        )

        then:
        1 * checkoutHelper.validateCartReviewVO(cartVO, model) >> cartVO
        1 * model.getAttribute('errorMessage') >> null
        1 * checkoutHelper.getExistingCart(cartid) >> cartVO
        1 * checkoutHelper.hydrateTransientQuantitiesForDisplay(cartVO) >> cartVO
        1 * model.addAttribute("cart", cartVO)
        1 * model.addAttribute("successMessage", "Review the cart")
        1 * discountService.bindAllDiscounts(model)
        1 * techvvsAuthService.checkuserauth(model)
        1 * checkoutHelper.getAllCustomers(model)
        0*_._

    }

    def "post - checkout page - apply discount to cart review page - WITH discount"() {

        given:

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

        CartVO cartVO = new CartVO(
                discount: discountVO,
                barcode:"048739801017",
                product_cart_list: product_cart_list,
                customer: Mock(CustomerVO),
                cartid: 45,
                total: 1000
        )
        Model model = Mock(Model)

        String barcode = "087310001019"
        String cartid = "45"
        Optional<Integer> page = Optional.of(1)
        Optional<Integer> size = Optional.of(5)

        when:
        checkoutViewController.applydiscount(
                cartVO,
                model,
                page,
                size
        )

        then:
        1 * checkoutHelper.validateCartReviewVO(cartVO, model) >> cartVO
        1 * model.getAttribute('errorMessage') >> null
        1 * cartService.applyAdhocDiscount(cartVO) >> cartVO
        1 * checkoutHelper.getExistingCart(cartid) >> cartVO
        1 * checkoutHelper.hydrateTransientQuantitiesForDisplay(cartVO) >> cartVO
        1 * model.addAttribute("cart", cartVO)
        1 * model.addAttribute("successMessage", "Review the cart")
        1 * discountService.bindAllDiscounts(model)
        1 * techvvsAuthService.checkuserauth(model)
        1 * checkoutHelper.getAllCustomers(model)
        0*_._

    }

    def "post - checkout page - remove discount on cart review page"() {

        given:

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

        CartVO cartVO = new CartVO(
                discount: discountVO,
                barcode:"048739801017",
                product_cart_list: product_cart_list,
                customer: Mock(CustomerVO),
                cartid: 45,
                total: 1000
        )
        Model model = Mock(Model)

        String barcode = "087310001019"
        String cartid = "45"
        Optional<Integer> page = Optional.of(1)
        Optional<Integer> size = Optional.of(5)

        when:
        checkoutViewController.removediscount(
                cartVO,
                model,
                page,
                size
        )

        then:
        1 * checkoutHelper.validateCartReviewVO(cartVO, model) >> cartVO
        1 * model.getAttribute('errorMessage') >> null
        1 * cartService.removeDiscount(cartVO) >> cartVO
        1 * checkoutHelper.getExistingCart(cartid) >> cartVO
        1 * checkoutHelper.hydrateTransientQuantitiesForDisplay(cartVO) >> cartVO
        1 * model.addAttribute("cart", cartVO)
        1 * model.addAttribute('successMessage', 'Review the cart')
        1 * model.addAttribute('warningMessage', 'Discount has been removed. ')
        1 * discountService.bindAllDiscounts(model)
        1 * techvvsAuthService.checkuserauth(model)
        1 * checkoutHelper.getAllCustomers(model)
        0*_._

    }

    def "post - checkout page - submit transaction from review cart page"() {

        given:

        List<ProductVO> product_cart_list = new ArrayList<>()
        LocalDateTime dateTime = LocalDateTime.of(2024, 10, 21, 10, 15, 30);
        TransactionVO transactionVO = new TransactionVO(transactionid: 4532)
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
        Model model = Mock(Model)

        String barcode = "087310001019"
        String cartid = "45"
        Optional<Integer> page = Optional.of(1)
        Optional<Integer> size = Optional.of(5)

        String dir = "dirdir"

        List<FileVO> files = Arrays.asList(
                new FileVO(filename: "file1.txt"),
                new FileVO(filename: "file2.txt")
        );
        Pageable pageable = PageRequest.of(0, 5);
        Page<FileVO> filePage = new PageImpl<>(files, pageable, files.size());

        when:
        checkoutViewController.transaction(
                cartVO,
                model,
                page,
                size
        )

        then:
        1 * appConstants.getPARENT_LEVEL_DIR() >> "./topdir/"
        1 * appConstants.getTRANSACTION_INVOICE_DIR() >> "/transaction/invoice/"
        1 * checkoutHelper.validateCartReviewVO(cartVO, model) >> cartVO
        1 * model.getAttribute('errorMessage') >> null
        1 * cartService.getExistingCart(cartVO) >> cartVO
        1 * transactionService.processCartGenerateNewTransaction(cartVO) >> transactionVO
        1 * model.getAttribute('errorMessage') >> null
        1 * model.addAttribute("successMessage", "Successfully completed transaction! Thanks "+customerVO.name+"!")
        1 * filePagingService.getFilePageFromDirectory(1, 5, './topdir//transaction/invoice/4532/')  >> filePage
        1 * filePagingService.bindPageAttributesToModel(model, filePage, page, size)
        1 * checkoutHelper.hydrateTransientQuantitiesForTransactionDisplay(transactionVO) >> transactionVO
        1 * printerService.printInvoice(transactionVO, false, true)
        1 * model.addAttribute("transaction", transactionVO)
        1 * techvvsAuthService.checkuserauth(model)
        1 * checkoutHelper.getAllCustomers(model)
        0*_._

    }

}
