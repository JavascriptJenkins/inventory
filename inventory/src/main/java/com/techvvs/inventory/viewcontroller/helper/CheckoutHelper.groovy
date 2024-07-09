package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.ui.Model

import java.time.LocalDateTime

// helper class to make sure actual checkout controller code stays clean n tidy
@Component
class CheckoutHelper {

    @Autowired
    CustomerRepo customerRepo

    @Autowired
    TransactionRepo transactionRepo

    @Autowired
    ProductRepo productRepo


    // method to get all customers from db
    void getAllCustomers(Model model){

        List<CustomerVO> customers = customerRepo.findAll()
        model.addAttribute("customers", customers)
    }

    TransactionVO validateTransactionVO(TransactionVO transactionVO, Model model){
        if(transactionVO?.customervo?.customerid == null){
            model.addAttribute("errorMessage","Please select a customer")
        }
        if(transactionVO?.barcode == null || transactionVO?.barcode?.empty){
            model.addAttribute("errorMessage","Please enter a barcode")
        }
        return transactionVO
    }

    TransactionVO saveTransactionIfNew(TransactionVO transactionVO){

        String barcode = transactionVO.barcode

        if(transactionVO.transactionid == 0 || transactionVO.transactionid == null
                && transactionVO.product_set == null || transactionVO?.product_set?.size() == 0){
            transactionVO.customervo = customerRepo.findById(transactionVO.customervo.customerid).get()
            transactionVO.isprocessed = 0
            transactionVO.createTimeStamp = LocalDateTime.now()
            transactionVO.updateTimeStamp = LocalDateTime.now()
            transactionVO = transactionRepo.save(transactionVO)
            transactionVO.barcode = barcode // need to re-bind this so that on first save it will not be null
        }


        return transactionVO
    }

    void findPendingTransactions(Model model, Optional<Integer> page, Optional<Integer> size){

        // START PAGINATION
        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
        int currentPage = page.orElse(0);
        int pageSize = 5;
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


    TransactionVO searchForProductByBarcode(TransactionVO transactionVO, Model model, Optional<Integer> page, Optional<Integer> size){


        Optional<ProductVO> productVO = productRepo.findByBarcode(transactionVO.barcode)

        // todo: on second time thru we need to fully hydrate the customer and product_set before saving

        if(!productVO.empty){

            // todo: possiblity will need to pass in existing fully hydrated transactionvo
            // this will bind the paginated products to the model
            transactionVO.product_set = bindExistingListOfProducts(transactionVO, model)
//            transactionVO.product_set = bindPaginatedListOfProducts(transactionVO, model, page, size)


            // if it's the first time adding a product we need to create the set to hold them
            if(transactionVO.product_set == null){
                transactionVO.product_set = new HashSet<ProductVO>()
            }

            transactionVO.product_set.add(productVO.get())

            transactionVO.total += Integer.valueOf(productVO.get().price) // add the product price to the total


//            // grab the customer object before updating
            transactionVO.customervo = customerRepo.findById(transactionVO.customervo.customerid).get()
            transactionVO.isprocessed = 0
          //  transactionVO = transactionRepo.save(transactionVO)


            transactionVO.updateTimeStamp = LocalDateTime.now()
            transactionVO = transactionRepo.save(transactionVO) // save the transaction with the new product associated
            model.addAttribute("successMessage","Product: "+productVO.get().name + " added successfully")
        } else {
            model.addAttribute("errorMessage","Product not found")
        }



        return transactionVO
    }

    Set<ProductVO> bindExistingListOfProducts(TransactionVO transactionVO, Model model){
        //            // bind the exsiting list of products
            Optional<TransactionVO> existingTransaction =transactionRepo.findById(transactionVO.transactionid)
            return existingTransaction?.get()?.product_set
    }

//    void bindPaginatedListOfProducts(TransactionVO transactionVO, Model model, Optional<Integer> page, Optional<Integer> size){
//
//                // START PAGINATION
//        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
//        //pagination
//        int currentPage = page.orElse(0);
//        int pageSize = 5;
//        Pageable pageable;
//        if(currentPage == 0){
//            pageable = PageRequest.of(0 , pageSize);
//        } else {
//            pageable = PageRequest.of(currentPage - 1, pageSize);
//        }
//
//        // todo: change this to find by transaction
////        Page<ProductVO> pageOfProduct = productRepo.findAll(pageable);
////        Page<ProductVO> pageOfProduct = productRepo.findAllByTransaction(transactionVO, pageable);
////        Page<ProductVO> pageOfProduct = transactionRepo.findByTransaction(transactionVO, pageable);
//
//        int totalPages = pageOfProduct.getTotalPages();
//
//        List<Integer> pageNumbers = new ArrayList<>();
//
//        while(totalPages > 0){
//            pageNumbers.add(totalPages);
//            totalPages = totalPages - 1;
//        }
//
//        model.addAttribute("pageNumbers", pageNumbers);
//        model.addAttribute("page", currentPage);
//        model.addAttribute("size", pageOfProduct.getTotalPages());
//        model.addAttribute("productPage", pageOfProduct);
//        // END PAGINATION
//    }


    void bindExistingTransaction(String transactionID, Model model){

        Optional<TransactionVO> transactionVO = transactionRepo.findById(Integer.valueOf(transactionID))

        if(!transactionVO.empty){
            model.addAttribute("transaction", transactionVO.get())
        }

    }





}
