package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.MenuRepo
import com.techvvs.inventory.jparepo.PaymentRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.PaymentVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.service.controllers.TransactionService
import com.techvvs.inventory.service.transactional.CartDeleteService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.ui.Model

@Component
class PaymentHelper {

    @Autowired
    MenuRepo menuRepo

    @Autowired
    CartDeleteService cartDeleteService

    @Autowired
    PaymentRepo paymentRepo

    @Autowired
    CustomerRepo customerRepo

    @Autowired
    TransactionRepo transactionRepo

    @Autowired
    TransactionService transactionService

    void findMenus(Model model, Optional<Integer> page, Optional<Integer> size){

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

        Page<MenuVO> pageOfMenu = menuRepo.findAll(pageable);

        int totalPages = pageOfMenu.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }


        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfMenu.getTotalPages());
        model.addAttribute("menuPage", pageOfMenu);
        // END PAGINATION



    }


    PaymentVO getExistingPayment(String paymentid){

        Optional<PaymentVO> paymentVO = paymentRepo.findById(Integer.valueOf(paymentid))

        if(!paymentVO.empty){
            return paymentVO.get()
        } else {
            return new PaymentVO(paymentid: 0)
        }
    }

    CustomerVO getExistingCustomer(String customerid){

        Optional<CustomerVO> customerVO = customerRepo.findById(Integer.valueOf(customerid))

        if(!customerVO.empty){
            return customerVO.get()
        } else {
            return new CustomerVO(customerid: 0)
        }
    }


    MenuVO hydrateTransientQuantitiesForDisplay(MenuVO menuVO){

        // cycle thru here and if the productid is the same then update the quantity
        ProductVO previous = new ProductVO(barcode: 0)
        for(ProductVO productVO : menuVO.menu_product_list){
            if(productVO.displayquantity == null){
                productVO.displayquantity = 1
            }
            if(productVO.barcode == previous.barcode){
                productVO.displayquantity = productVO.displayquantity + 1
            }
            previous = productVO
        }

        return menuVO

    }

    TransactionVO loadTransaction(String transactionid, Model model){

        TransactionVO transactionVO =transactionService.getExistingTransaction(Integer.valueOf(transactionid))

        model.addAttribute("transaction", transactionVO)
        return transactionVO
    }

    PaymentVO loadPayment(String paymentid, Model model){

        PaymentVO paymentVO = new PaymentVO()
        // if cartid == 0 then load normally, otherwise load the existing transaction
        if(paymentid == "0"){
            // do nothing
            // if it is the first time loading the page

            model.addAttribute("payment", paymentVO);
            return paymentVO

        } else {
            paymentVO = getExistingPayment(paymentid)

            model.addAttribute("payment", paymentVO);
            return paymentVO
        }

    }

    CustomerVO loadCustomer(String customerid, Model model){

        CustomerVO customerVO = new CustomerVO()
        // if cartid == 0 then load normally, otherwise load the existing transaction
        if(customerid == "0"){
            // do nothing
            // if it is the first time loading the page

            model.addAttribute("customer", customerVO);
            return customerVO
        } else {
            customerVO = getExistingCustomer(customerid)

            model.addAttribute("customer", customerVO);
            return customerVO
        }

    }


    CartVO validateCartVO(CartVO cartVO, Model model){
        if(cartVO?.customer?.customerid == null){
            model.addAttribute("errorMessage","Please select a customer")
        }
        if(cartVO?.barcode == null || cartVO?.barcode?.empty){
            model.addAttribute("primaryMessage","Add a product to your cart")
        } else {
            // only run this database check if barcode is not null
            Optional<ProductVO> productVO = cartDeleteService.doesProductExist(cartVO.barcode)
            if(productVO.empty){
                model.addAttribute("errorMessage","Product does not exist")
            } else {
                int cartcount = cartDeleteService.getCountOfProductInCartByBarcode(cartVO)
                // check here if the quantity we are trying to add will exceed the quantity in stock
                if(cartcount >= productVO.get().quantityremaining){
                    model.addAttribute("errorMessage","Quantity exceeds quantity in stock")
                }
            }

        }

        return cartVO
    }

    PaymentVO validatePaymentVO(PaymentVO paymentVO, Model model, int transactionid){
        if(paymentVO?.amountpaid == null || paymentVO?.amountpaid == 0.00 || paymentVO?.amountpaid?.toString()?.isBlank()){
            model.addAttribute("errorMessage","Please enter an amount")
            return paymentVO
        }
        TransactionVO existingTransaction = transactionRepo.findById(transactionid).get()
        if(paymentVO?.amountpaid + existingTransaction.paid > existingTransaction.totalwithtax){
            model.addAttribute("errorMessage","Enter an amount less that the total of the transaction")
        }

        return paymentVO
    }
    
    
}
