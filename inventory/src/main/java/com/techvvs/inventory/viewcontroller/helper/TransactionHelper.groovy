package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ReturnRepo
import com.techvvs.inventory.jparepo.SystemUserRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.ReturnVO
import com.techvvs.inventory.model.SystemUserDAO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.service.email.EmailService
import com.techvvs.inventory.util.SendgridEmailUtil
import com.techvvs.inventory.util.TwilioTextUtil
import org.attoparser.util.TextUtil
import org.hibernate.loader.custom.Return
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.ui.Model

import java.time.LocalDateTime

@Component
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
    SendgridEmailUtil sendgridEmailUtil

    @Autowired
    AppConstants appConstants

    @Autowired
    EmailService emailService

    void findAllTransactions(Model model, Optional<Integer> page, Optional<Integer> size){

        // START PAGINATION
        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);
        int pageSize = size.orElse(5);
        Pageable pageable;
        if(currentPage == 0){
            pageable = PageRequest.of(0 , pageSize);
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize);
        }

        Page<TransactionVO> pageOfTransaction = transactionRepo.findAll(pageable);

        int totalPages = pageOfTransaction.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }


        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfTransaction.getTotalPages());
        model.addAttribute("transactionPage", pageOfTransaction);
        // END PAGINATION



    }


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
    TransactionVO deleteProductFromTransaction(TransactionVO transactionVO, String barcode){

        Double amountToSubtract = 0.00

        TransactionVO transactiontoremove = new TransactionVO(transactionid: 0)
        // we are only removing one product at a time
        for(ProductVO productVO : transactionVO.product_list){
            if(productVO.barcode == barcode){
                transactionVO.product_list.remove(productVO)


                addProductToReturnList(transactionVO, productVO)


                productVO.quantityremaining = productVO.quantityremaining + 1
                amountToSubtract = amountToSubtract + productVO.price
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

        transactionVO.total = transactionVO.total - amountToSubtract
        transactionVO.totalwithtax = transactionVO.totalwithtax - amountToSubtract

        transactionVO.updateTimeStamp = LocalDateTime.now()
        transactionVO = transactionRepo.save(transactionVO)


        return transactionVO

    }

    // to keep this simple we are only returning one product at a time ....
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


}
