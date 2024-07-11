package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.CartVO
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

    @Autowired
    CartRepo cartRepo

    @Autowired
    CheckoutHelper checkoutHelper

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
        } else {
            // only run this database check if barcode is not null
            if(!checkoutHelper.doesProductExist(transactionVO.barcode)){
                model.addAttribute("errorMessage","Product does not exist")
            }
        }

        return transactionVO
    }

    TransactionVO saveTransactionIfNew(TransactionVO transactionVO){

        // need to check to make sure there isn't an existing transaction with the same customer and no objects
        String barcode = transactionVO.barcode

        if((transactionVO.transactionid == 0 || transactionVO.transactionid == null
                && transactionVO?.cart == null || transactionVO?.cart?.product_list?.size() == 0)
        &&
            !doesTransactionExist(transactionVO.customervo, barcode)

            &&

                !doesCartExist(transactionVO)
        ){

            CustomerVO customerVO = customerRepo.findById(transactionVO.customervo.customerid).get()

            def myList = transactionVO.product_set as List
            CartVO cart = new CartVO(
                    product_list : myList,
                    customer : customerVO,
                    updateTimeStamp: LocalDateTime.now(),
                    createTimeStamp: LocalDateTime.now()
            )

           CartVO savedcart = cartRepo.save(cart)

            transactionVO.cart = savedcart

            transactionVO.customervo = customerVO
            transactionVO.isprocessed = 0
            transactionVO.createTimeStamp = LocalDateTime.now()
            transactionVO.updateTimeStamp = LocalDateTime.now()
            transactionVO = transactionRepo.save(transactionVO)
            transactionVO.barcode = barcode // need to re-bind this so that on first save it will not be null
        }

        // todo: handle case where a cart does exist

        return transactionVO
    }


    boolean doesCartExist(TransactionVO transactionVO){
        if(transactionVO?.cart == null){
            return false
        }
        Optional<CartVO> existingcart = cartRepo.findById(transactionVO?.cartid)
        return !existingcart.empty
    }

    boolean doesProductExist(String barcode){

        Optional<ProductVO> existingproduct = productRepo.findByBarcode(barcode)
        return !existingproduct.empty
    }

    boolean doesTransactionExist(CustomerVO customerVO, String barcode){


        List<TransactionVO> existingtransactions = transactionRepo.findAllByCustomervo(customerVO)




        // todo: remove this stupid check for a case that shoulndt happen
        // if the customer already has a transaction that exists
        for(TransactionVO transactionVO : existingtransactions){
            if(transactionVO.isprocessed == 0 && transactionVO?.cart?.product_list?.size() == 0){

                // this covers a situation where customer has an existing transaction with no products
                // instead of creating another redundant record in the database we are are just updating it
                transactionVO.customervo = customerRepo.findById(transactionVO.customervo.customerid).get()
                transactionVO.updateTimeStamp = LocalDateTime.now()
                transactionVO.barcode = barcode // need to re-bind this so that on first save it will not be null
                transactionVO = transactionRepo.save(transactionVO)



                return true
            }
        }

        return false
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


            // todo: do we have a cart here??
            // if it's the first time adding a product we need to create the set to hold them
            if(transactionVO.cart?.product_list == null){
                transactionVO.cart?.product_list = new ArrayList<ProductVO>()
            }

            // todo: pass in the cartid from ui into this
            // this will bind the paginated products to the model
//            transactionVO.cart?.product_list = bindExistingCart(transactionVO, model)
            transactionVO = bindExistingListOfProductsAndCustomer(transactionVO, model)
//            transactionVO.product_set = bindPaginatedListOfProducts(transactionVO, model, page, size)


            transactionVO?.cart?.product_list?.add(productVO.get())

            transactionVO.total += Integer.valueOf(productVO.get().price) // add the product price to the total


//            // grab the customer object before updating
            transactionVO.customervo = customerRepo.findById(transactionVO.customervo.customerid).get()
            transactionVO.isprocessed = 0
          //  transactionVO = transactionRepo.save(transactionVO)

            if(productVO.cart_list == null){
                productVO.cart_list = new ArrayList<>()
            }
            productVO.cart_list.add(transactionVO.cart)
            productRepo.save(productVO)

            transactionVO.cart.updateTimeStamp = LocalDateTime.now()
            transactionVO.cart = cartRepo.save(transactionVO.cart)

            transactionVO.updateTimeStamp = LocalDateTime.now()
            transactionVO = transactionRepo.save(transactionVO) // save the transaction with the new product associated
            model.addAttribute("successMessage","Product: "+productVO.get().name + " added successfully")
        } else {
            // need to bind the selected customer here otherwise the dropdown wont work
            transactionVO.customervo = customerRepo.findById(transactionVO.customervo.customerid).get()
            model.addAttribute("errorMessage","Product not found")
        }



        return transactionVO
    }

    // todo: question -= are the products save dhwile we are here??
    // being lazy here and binding the customer each time the user enters a barcode
    TransactionVO bindExistingListOfProductsAndCustomer(TransactionVO transactionVO, Model model){
        //            // bind the exsiting list of products

        // Convert the Set to a List


            Optional<TransactionVO> existingTransaction =transactionRepo.findById(transactionVO.transactionid)
            transactionVO.customervo = existingTransaction?.get()?.customervo
            transactionVO.cart = existingTransaction.get().cart
            def myList = existingTransaction.get().product_set as List
            transactionVO.cart.product_list = myList// todo: convert this set to list
            return transactionVO
    }

//    public static <T> LinkedHashSet<T> convertListToLinkedHashSet(List<T> originalList) {
//        if (originalList == null) {
//            throw new NullPointerException("The original list cannot be null");
//        }
//        return new LinkedHashSet<>(originalList);
//    }

//    CartVO bindExistingCart(TransactionVO transactionVO, Model model){
//        Optional<TransactionVO> existingTransaction =transactionRepo.findById(transactionVO.transactionid)
//        transactionVO.customervo = existingTransaction?.get()?.customervo
//        return existingTransaction?.get()?.cart?.product_list
//    }

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
