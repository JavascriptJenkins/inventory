package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ReturnRepo
import com.techvvs.inventory.jparepo.SystemUserRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.DiscountVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.ReturnVO
import com.techvvs.inventory.model.SystemUserDAO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.service.email.EmailService
import com.techvvs.inventory.service.transactional.CheckoutService
import com.techvvs.inventory.util.SendgridEmailUtil
import com.techvvs.inventory.util.TwilioTextUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.ui.Model

import javax.transaction.Transactional
import java.time.LocalDateTime

@Service
class TransactionHelper {

    @Autowired
    TransactionRepo transactionRepo

    @Autowired
    ReturnRepo returnRepo

    @Autowired
    ProductRepo productRepo

    @Autowired
    SystemUserRepo systemUserRepo

    @Autowired
    Environment env

    @Autowired
    TwilioTextUtil textUtil

    @Autowired
    AppConstants appConstants

    @Autowired
    EmailService emailService

    @Autowired
    CheckoutService checkoutService


    @Transactional
    void findAllTransactions(Model model,
                             Optional<Integer> page,
                             Optional<Integer> size,
                             Optional<Integer> customerid,
                             Optional<Integer> productinscope,
                             Optional<Integer> batchid

                             ) {

        // START PAGINATION
        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);    // Default to first page
        int pageSize = size.orElse(20);       // Default page size to 5
        int selectedproductid = productinscope.orElse(0);


        if(
                currentPage > pageSize ||
                customerid.isPresent() && currentPage > pageSize
        ){
            currentPage = 0;
        }

        pageSize = pageSize < 5 ? 5 : pageSize; // make sure it's not less than 5


        // run first page request
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC, "createTimeStamp"));
        Page<TransactionVO> pageOfTransaction = runPageRequest(pageable, customerid, productinscope, batchid);


        int totalPages = pageOfTransaction.getTotalPages();
        int contentsize = pageOfTransaction.getContent().size()


        if(contentsize == 0){
            // we detect contentsize of 0 then we'll just take the first page of data and show it
            pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createTimeStamp"));
            pageOfTransaction = runPageRequest(pageable, customerid, productinscope, batchid);
        }

        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNumbers.add(i);
        }


        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageSize);
        model.addAttribute("transactionPage", pageOfTransaction);
        model.addAttribute("customerid", customerid.orElse(0));
        // END PAGINATION



    }

    @Transactional
    Page<TransactionVO> runPageRequest(
            Pageable pageable,
            Optional<Integer> customerid,
            Optional<Integer> selectedproductid,
            Optional<Integer> selectedbatchid
    ) {
        Integer custId = (customerid.present && customerid.get() > 0) ? customerid.get() : null
        Integer prodId = (selectedproductid.present && selectedproductid.get() > 0) ? selectedproductid.get() : null
        Integer batchId = (selectedbatchid.present && selectedbatchid.get() > 0) ? selectedbatchid.get() : null

        return transactionRepo.findFilteredTransactions(custId, prodId, batchId, pageable)
    }



    @Transactional
    void sendTextMessageWithDownloadLink(String phonenumber, String filename){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        SystemUserDAO systemUserDAO = systemUserRepo.findByEmail(authentication.getPrincipal().username)


        boolean isDev1 = appConstants.DEV_1.equals(env.getProperty("spring.profiles.active"));

        // SystemUserDAO systemUserDAO, String token, boolean isDev1
        // send a text message with a download link
        textUtil.sendOutDownloadLinkWithTokenSMS(phonenumber, filename, systemUserDAO, false)
    }


    void sendEmailWithDownloadLink(String email, String filename){
        emailService.sendDownloadLinkEmail(email, filename)
    }

    void viewInvoice(String email, String filename){
       // emailService.sendDownloadLinkEmail(email, filename)
    }


    @Transactional
    TransactionVO deleteProductFromTransactionForProductTypeDiscount(
            TransactionVO transactionVO,
            String barcode,
            Optional<Integer> quantity
    ){

        Double amountToSubtract = 0.00
        double amountofdiscount = 0.00
        TransactionVO transactiontoremove = new TransactionVO(transactionid: 0)
        int amountOfProductsToReturn = 0
        if(quantity.isPresent()){
            amountOfProductsToReturn = quantity.get()
        }
        // This is a stupid and inefficient way to do this, but whatever .......
        for(int i = 0; i < amountOfProductsToReturn; i++) {
            runRemovalLogic(transactionVO, barcode, amountofdiscount, amountToSubtract, transactiontoremove)
        }

        transactionVO.updateTimeStamp = LocalDateTime.now()
        transactionVO = transactionRepo.save(transactionVO)

        return transactionVO

    }

    // we are only deleting it from the transaction - the original cart association is not removed.
    @Transactional
    TransactionVO deleteProductFromTransaction(
            TransactionVO transactionVO,
            String barcode,
            Optional<Integer> quantity
    ){

        Double amountToSubtract = 0.00
        double amountofdiscount = 0.00
        TransactionVO transactiontoremove = new TransactionVO(transactionid: 0)
        int amountOfProductsToReturn = 0
        if(quantity.isPresent()){
            amountOfProductsToReturn = quantity.get()
        }

        // we should only have one active discount per product at any given time, but doing this to code defensively
        List<DiscountVO> discountlist = getListOfAllActiveDiscountsForProduct(transactionVO, barcode)


        DiscountVO latestDiscount = discountlist?.max { it.createTimeStamp }
        int amountofdiscountedproducts = latestDiscount.quantity

        // This is a stupid and inefficient way to do this, but whatever .......
        for(int i = 0; i < amountOfProductsToReturn; i++) {
            runRemovalLogicForPerProductDiscount(transactionVO, barcode, amountofdiscount, amountToSubtract, transactiontoremove, amountofdiscountedproducts)
        }
        transactionVO.updateTimeStamp = LocalDateTime.now()
        transactionVO = transactionRepo.save(transactionVO)


        return transactionVO

    }

    List<DiscountVO> getListOfAllActiveDiscountsForProduct(TransactionVO transactionVO, String barcode){
        int productid = 0
        // find the latest discount that has been applied and take the item amount from that
        // todo: this needs to not just take the latest discount, it needs to check the barcode being returned and match that with the latest discount for that barcode
        for(ProductVO existingproduct : transactionVO.product_list){
            if(barcode.equals(existingproduct.barcode)){
                productid = existingproduct.product_id
            }
        }

        List<DiscountVO> discountlist = new ArrayList<>()
        for(DiscountVO existingdiscount : transactionVO.discount_list){
            if(productid == existingdiscount.product.product_id && existingdiscount.isactive == 1){
                discountlist.add(existingdiscount) // make a list of the active discounts for this product (should be just 1 but we are being safe here...)
            }
        }
        return discountlist
    }

    @Transactional
    void runRemovalLogic(TransactionVO transactionVO,
                         String barcode,
                         double amountofdiscount,
                         double amountToSubtract,
                         TransactionVO transactiontoremove

    ) {
        // we are only removing one product at a time
        for (ProductVO productVO : transactionVO.product_list) {
            if (productVO.barcode == barcode) {


                // before doing anything, check to see if there is a discount active on this transaction that needs to be
                // applied back to the total and totalwithtax fields
                amountofdiscount = checkoutService.calculateTotalsForRemovingExistingProductFromTransaction(transactionVO, productVO)

                transactionVO.product_list.remove(productVO)

                addProductToReturnList(transactionVO, productVO)

                productVO.quantityremaining = productVO.quantityremaining + 1
                amountToSubtract = Math.max(0.00, productVO.price - amountofdiscount)
                // remove the transaction association from the product
                for (TransactionVO existingTransaction : productVO.transaction_list) {
                    if (existingTransaction.transactionid == transactionVO.transactionid) {
                        transactiontoremove = existingTransaction
                    }
                }
                productVO.transaction_list.remove(transactiontoremove)
                productVO.updateTimeStamp = LocalDateTime.now()
                productRepo.save(productVO)
                break
            }
        }



        checkForCustomerCredit(transactionVO, amountToSubtract)


        transactionVO.total = Math.max(0, transactionVO.total - amountToSubtract)
        transactionVO.totalwithtax = Math.max(0, transactionVO.totalwithtax - amountToSubtract)
    }

    @Transactional
    void runRemovalLogicForPerProductDiscount(TransactionVO transactionVO,
                         String barcode,
                         double amountofdiscount,
                         double amountToSubtract,
                         TransactionVO transactiontoremove,
            int amountofdiscountedproducts

    ) {

        // we are only removing one product at a time
        for (ProductVO productVO : transactionVO.product_list) {
            if (productVO.barcode == barcode) {


                // before doing anything, check to see if there is a discount active on this transaction that needs to be
                // applied back to the total and totalwithtax fields
                amountofdiscount = checkoutService.calculateTotalsForRemovingExistingProductFromTransactionForProductDiscount(transactionVO, productVO, amountofdiscountedproducts)

                transactionVO.product_list.remove(productVO)

                addProductToReturnList(transactionVO, productVO)

                productVO.quantityremaining = productVO.quantityremaining + 1
                amountToSubtract = Math.max(0.00, productVO.price - amountofdiscount)
                // remove the transaction association from the product
                for (TransactionVO existingTransaction : productVO.transaction_list) {
                    if (existingTransaction.transactionid == transactionVO.transactionid) {
                        transactiontoremove = existingTransaction
                    }
                }
                productVO.transaction_list.remove(transactiontoremove)
                productVO.updateTimeStamp = LocalDateTime.now()
                productRepo.save(productVO)
                break
            }
        }



        checkForCustomerCredit(transactionVO, amountToSubtract)

        transactionVO.total = Math.max(0, transactionVO.total - amountToSubtract)
        transactionVO.totalwithtax = Math.max(0, transactionVO.totalwithtax - amountToSubtract)

    }

    void checkForCustomerCredit(TransactionVO transactionVO, double amountToSubtract) {

        // at this point, the amount to credit customer is the amountToSubtract - remaining balance, if that value is negative
        Double balanceAfterSubtract = (transactionVO.total - transactionVO.paid) - amountToSubtract

        // we need to account for any discounts that were applied to the products in the transaction
        // here we add up all the discounts applied to the products in the transaction.
        Double totaldiscountfromallproducts = calculateAllDiscountsThatHaveBeenAppliedToTransaction(transactionVO)

        balanceAfterSubtract = (balanceAfterSubtract + totaldiscountfromallproducts) // add back the discounts to get to correct number again

        if (balanceAfterSubtract < 0) {
            // This means we have a negative balance → customer should get credit back
            transactionVO.customercredit += Math.abs(balanceAfterSubtract)
        }

    }


    Double calculateAllDiscountsThatHaveBeenAppliedToTransaction(TransactionVO transactionVO){
        double totaldiscountfromallproducts = 0.00
        for(DiscountVO discountVO : transactionVO.discount_list) {
            if(discountVO.isactive == 1){
                totaldiscountfromallproducts += (discountVO.discountamount * discountVO.quantity)
            }
        }

        return Math.max(0,totaldiscountfromallproducts)
    }



    // to keep this simple we are only returning one product at a time ....
    @Transactional
    void addProductToReturnList(TransactionVO transactionVO, ProductVO productVO){

        List<ReturnVO> existingreturnlist = returnRepo.findAllByTransaction(transactionVO) // need to explicitly query for this list because it's lazy loaded

        ReturnVO returnVO = new ReturnVO()

        if(transactionVO.return_list == null && existingreturnlist == null){
            transactionVO.return_list = new ArrayList<>()
        } else {
            transactionVO.return_list = existingreturnlist
        }


        returnVO.product = productVO
        returnVO.customer = transactionVO.customervo
        returnVO.transaction = transactionVO
        returnVO.updateTimeStamp = LocalDateTime.now()
        returnVO.createTimeStamp = LocalDateTime.now()

        ReturnVO savedReturn = returnRepo.save(returnVO)

        transactionVO.return_list.add(savedReturn)


    }



    @Transactional
    void findAllUnpaidProductsInTransactionsByBatchId(Model model,
                             Optional<Integer> page,
                             Optional<Integer> size,
                             Optional<Integer> batchid
    ) {

        // START PAGINATION
        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);    // Default to first page
        int pageSize = size.orElse(5);       // Default page size to 5


        if(
                currentPage > pageSize ||
                        batchid.isPresent() && currentPage > pageSize
        ){
            currentPage = 0;
        }

        pageSize = pageSize < 5 ? 5 : pageSize; // make sure it's not less than 5


        // run first page request
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.ASC, "create_time_stamp"));
        Page<ProductVO> pageOfProduct = runPageRequestForUnPaidProducts(pageable, batchid)

        int totalPages = pageOfProduct.getTotalPages();
        int contentsize = pageOfProduct.getContent().size()


        if(contentsize == 0){
            // we detect contentsize of 0 then we'll just take the first page of data and show it
            pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, "create_time_stamp"));
            pageOfProduct = runPageRequestForUnPaidProducts(pageable, batchid)
        }

        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNumbers.add(i);
        }


        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageSize);
        model.addAttribute("productPage", pageOfProduct);
        model.addAttribute("batchid", batchid.orElse(0));
        // END PAGINATION



    }


    // do not copy this method
    @Transactional
    Page<ProductVO> runPageRequestForUnPaidProducts(Pageable pageable, Optional<Integer> batchid) {
        Page<ProductVO> pageOfProduct
        // this means someone selected a value on the ui and we need to run a filtered query
        if(batchid.isPresent() && batchid.get() > 0) {
            // need a custom query here to let user filter by batchid and see all unpaid items
            pageOfProduct = productRepo.findUnpaidProductsByBatchId(batchid.get(),pageable);
        } else {
            return Page.empty() // returning empty here becuase there is a bug.. for some reason spring is looking for property of "create" that doesnt exist on the productVO
//            pageOfProduct = productRepo.findAll(pageable);
        }
        return pageOfProduct
    }




    @Transactional
    void findAllUnpaidTransactionsByBatchIdAndProduct_id(Model model,
                                                      Optional<Integer> page,
                                                      Optional<Integer> size,
                                                      Optional<Integer> batchid,
                                                      Optional<Integer> productid
    ) {

        // START PAGINATION
        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);    // Default to first page
        int pageSize = size.orElse(5);       // Default page size to 5


        if(
                currentPage > pageSize ||
                        batchid.isPresent() && productid.isPresent() && currentPage > pageSize
        ){
            currentPage = 0;
        }

        pageSize = pageSize < 5 ? 5 : pageSize; // make sure it's not less than 5


        // run first page request
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.ASC, "create_time_stamp"));
        Page<TransactionVO> pageOfTransaction = runPageRequestForUnPaidTransactions(pageable, batchid, productid)

        int totalPages = pageOfTransaction.getTotalPages();
        int contentsize = pageOfTransaction.getContent().size()


        if(contentsize == 0){
            // we detect contentsize of 0 then we'll just take the first page of data and show it
            pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, "create_time_stamp"));
            pageOfTransaction = runPageRequestForUnPaidTransactions(pageable, batchid, productid)
        }

        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNumbers.add(i);
        }


        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageSize);
        model.addAttribute("transactionPage", pageOfTransaction);
        model.addAttribute("batchid", batchid.orElse(0));
        model.addAttribute("product_id", productid.orElse(0));
        // END PAGINATION



    }


    @Transactional
    Page<TransactionVO> runPageRequestForUnPaidTransactions(
            Pageable pageable,
            Optional<Integer> batchid,
            Optional<Integer> product_id

    ) {
        Page<TransactionVO> pageOfTransaction
        // this means someone selected a value on the ui and we need to run a filtered query
        if(batchid.isPresent() && batchid.get() > 0 && product_id.isPresent() && product_id.get() > 0) {
            // need a custom query here to let user filter by batchid and see all unpaid items
            pageOfTransaction = transactionRepo.findDistinctTransactionsByProductIdAndBatchId(product_id.get(),batchid.get(),pageable);
        } else {
            return Page.empty() // returning empty here becuase there is a bug.. for some reason spring is looking for property of "create" that doesnt exist on the productVO
//            pageOfProduct = productRepo.findAll(pageable);
        }
        return pageOfTransaction
    }
}
