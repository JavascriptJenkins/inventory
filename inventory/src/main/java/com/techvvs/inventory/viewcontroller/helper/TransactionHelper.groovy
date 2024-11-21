package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ReturnRepo
import com.techvvs.inventory.jparepo.SystemUserRepo
import com.techvvs.inventory.jparepo.TransactionRepo
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
                             Optional<Integer> customerid
                             ) {

        // START PAGINATION
        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);    // Default to first page
        int pageSize = size.orElse(5);       // Default page size to 5


        if(
                currentPage > pageSize ||
                customerid.isPresent() && currentPage > pageSize
        ){
            currentPage = 0;
        }

        pageSize = pageSize < 5 ? 5 : pageSize; // make sure it's not less than 5


        // run first page request
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.ASC, "createTimeStamp"));
        Page<TransactionVO> pageOfTransaction = runPageRequest(pageable, customerid)

        int totalPages = pageOfTransaction.getTotalPages();
        int contentsize = pageOfTransaction.getContent().size()


        if(contentsize == 0){
            // we detect contentsize of 0 then we'll just take the first page of data and show it
            pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, "createTimeStamp"));
            pageOfTransaction = runPageRequest(pageable, customerid)
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
    Page<TransactionVO> runPageRequest(Pageable pageable, Optional<Integer> customerid) {
        Page<TransactionVO> pageOfTransaction
                // this means someone selected a value on the ui and we need to run a filtered query
        if(customerid.isPresent() && customerid.get() > 0) {
            pageOfTransaction = transactionRepo.findByCustomervo_customerid(customerid.get(),pageable);
        } else {
             pageOfTransaction = transactionRepo.findAll(pageable);
        }
        return pageOfTransaction
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


    // we are only deleting it from the transaction - the original cart association is not removed.
    @Transactional
    TransactionVO deleteProductFromTransaction(TransactionVO transactionVO, String barcode){

        Double amountToSubtract = 0.00
        double amountofdiscount = 0.00
        TransactionVO transactiontoremove = new TransactionVO(transactionid: 0)
        // we are only removing one product at a time
        for(ProductVO productVO : transactionVO.product_list){
            if(productVO.barcode == barcode){


                // before doing anything, check to see if there is a discount active on this transaction that needs to be
                // applied back to the total and totalwithtax fields
                amountofdiscount = checkoutService.calculateTotalsForRemovingExistingProductFromTransaction(transactionVO, productVO)

                transactionVO.product_list.remove(productVO)


                addProductToReturnList(transactionVO, productVO)
                // set the values back to original price before applying the product return math
//                transactionVO.totalwithtax = Math.max(0,transactionVO.originalprice - amountofdiscount)
//                transactionVO.total = Math.max(0,transactionVO.originalprice - amountofdiscount)

                productVO.quantityremaining = productVO.quantityremaining + 1
                amountToSubtract = Math.max(0.00, productVO.price - amountofdiscount)
                // remove the transaction association from the product
                for(TransactionVO existingTransaction : productVO.transaction_list){
                    if(existingTransaction.transactionid == transactionVO.transactionid){
                        transactiontoremove = existingTransaction
                    }
                }
                productVO.transaction_list.remove(transactiontoremove)
                productVO.updateTimeStamp = LocalDateTime.now()
                productRepo.save(productVO)
                break
            }
        }

        // check to see if the transaction is fully paid, if so then we don't subtract from the total or totalwithtax
        // todo: could there be a scenario here where the transactionVO.totalwithtax - transactionVO.paid
        if(
                Math.max(0, transactionVO.totalwithtax - transactionVO.paid) == 0 ||
                transactionVO.isprocessed == 1 // if we are returning something from an already paid transaction
        ){
            // this means that someone is returning a product from an already paid transaction
            // This means we need to capture the credit somewhere so the customer can get credit.
            System.out.println("line 202: transactionVO.customercredit: "+transactionVO.customercredit)
            transactionVO.customercredit = Math.max(0,transactionVO.customercredit + amountToSubtract)

        } else {


            // here we check to make sure that there is no potential remainder credit for customer that wasn't caught by the
            // if statement above.  i dont even know if this scenario is possible/valid, but it might be.
            transactionVO = checkPotentialForPartialProductCustomerCredit(transactionVO)

            transactionVO.total = Math.max(0,transactionVO.total - amountToSubtract)
            transactionVO.totalwithtax = Math.max(0,transactionVO.totalwithtax - amountToSubtract)


            //. todo:  test this with the discount functionality and figure out if we need to modify the original price here
            // todo: i am thinking that we want to keep it reflected here as origal price.
            // todo:  even if someone returns a product on a partially paid transaction, the orginal price should be reflected
            // todo: after that product is returned.  If we are applying a discount on on a partially paid transaction,
            //. todo: then the discount should only apply to the products in the current product list.

            // todo:  the discount when applied should take into account any returns in the Returns table
            // todo: and subtract the price of the returns from the original price at time of applying discount based on the original price
            //transactionVO.originalprice = Math.max(0,transactionVO.originalprice - amountToSubtract)

        }

        transactionVO.updateTimeStamp = LocalDateTime.now()
        transactionVO = transactionRepo.save(transactionVO)


        return transactionVO

    }

    @Transactional
    TransactionVO checkPotentialForPartialProductCustomerCredit(TransactionVO transactionVO) {
        // Calculate the amount left to subtract
        double amountToSubtractInScope = transactionVO.total - transactionVO.paid;

        // Log the calculation for debugging purposes
        System.out.println("Calculated amount to subtract: " + amountToSubtractInScope);

        // Check if the resulting value is negative
        if (amountToSubtractInScope < 0) {
            // Credit back the customer for the overpayment
            double creditToApply = Math.abs(amountToSubtractInScope);
            transactionVO.customercredit += creditToApply;

            // Log the adjustment for auditing
            System.out.println("Negative balance detected. Crediting customer: " + creditToApply);
        }

        // Return the updated TransactionVO
        return transactionVO;
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
