package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.dao.BatchDao
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.ProductVO
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
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestParam
import spock.lang.Specification

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



}
