package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.service.controllers.CartService
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
    CartService cartService


    // method to get all customers from db
    void getAllCustomers(Model model){

        List<CustomerVO> customers = customerRepo.findAll()
        model.addAttribute("customers", customers)
    }

    CartVO validateCartVO(CartVO cartVO, Model model){
        if(cartVO?.customer?.customerid == null){
            model.addAttribute("errorMessage","Please select a customer")
        }
        if(cartVO?.barcode == null || cartVO?.barcode?.empty){
            model.addAttribute("errorMessage","Please enter a barcode")
        } else {
            // only run this database check if barcode is not null
            Optional<ProductVO> productVO = doesProductExist(cartVO.barcode)
            if(productVO.empty){
                model.addAttribute("errorMessage","Product does not exist")
            } else {
                int cartcount = getCountOfProductInCartByBarcode(cartVO)
                // check here if the quantity we are trying to add will exceed the quantity in stock
                if(cartcount >= productVO.get().quantityremaining){
                    model.addAttribute("errorMessage","Quantity exceeds quantity in stock")
                }
            }

        }

        return cartVO
    }

    int getCountOfProductInCartByBarcode(CartVO cartVO){
        int count = 0
        for(ProductVO productincart : cartVO.product_cart_list){
            if(productincart.barcode.equals(cartVO.barcode)){
                count = count + 1
            }
        }
        return count
    }

    CartVO validateCartReviewVO(CartVO cartVO, Model model){
        if(cartVO?.customer?.customerid == null){
            model.addAttribute("errorMessage","Please select a customer")
        }
        return cartVO
    }

    CartVO validateTransaction(CartVO cartVO, Model model){
        if(cartVO?.customer?.customerid == null){
            model.addAttribute("errorMessage","Please select a customer")
        }
        return cartVO
    }
    CartVO saveCartIfNew(CartVO cartVO){

        // need to check to make sure there isn't an existing transaction with the same customer and no objects
        String barcode = cartVO.barcode

        if((cartVO.cartid == 0 || cartVO.cartid == null
                && cartVO == null || cartVO?.product_cart_list?.size() == 0)
//        &&
//                !doesTransactionExist(transactionVO.customervo, barcode) // dont think we need to do this
        &&
                !cartService.doesCartExist(cartVO)
        ){

            CustomerVO customerVO = customerRepo.findById(cartVO.customer.customerid).get()

            cartVO.updateTimeStamp = LocalDateTime.now()
            cartVO.createTimeStamp = LocalDateTime.now()
            cartVO.isprocessed = 0
            cartVO.setCustomer(customerVO)

            cartVO = cartRepo.save(cartVO)
            cartVO.barcode = barcode // need to re-bind this so that on first save it will not be null
        }

        // todo: handle case where a cart does exist

        return cartVO
    }

    Optional<ProductVO> doesProductExist(String barcode){

        Optional<ProductVO> existingproduct = productRepo.findByBarcode(barcode)
        return existingproduct
    }

    void findPendingCarts(Model model, Optional<Integer> page, Optional<Integer> size){

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

        Page<CartVO> pageOfCart = cartRepo.findAll(pageable);

        int totalPages = pageOfCart.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }


        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfCart.getTotalPages());
        model.addAttribute("cartPage", pageOfCart);
        // END PAGINATION



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

    CartVO hydrateTransientQuantitiesForDisplay(CartVO cartVO){

        // cycle thru here and if the productid is the same then update the quantity
        ProductVO previous = new ProductVO(barcode: 0)
        for(ProductVO productVO : cartVO.product_cart_list){
            if(productVO.displayquantity == null){
                productVO.displayquantity = 1
            }
            if(productVO.barcode == previous.barcode){
                    productVO.displayquantity = productVO.displayquantity + 1
            }
            previous = productVO
        }

        return cartVO

    }



    CartVO getExistingCart(String cartid){

        Optional<CartVO> cartVO = cartRepo.findById(Integer.valueOf(cartid))

        if(!cartVO.empty){
            return cartVO.get()
        } else {
            return new CartVO(cartid: 0)
        }
    }


    void reviewCart(CartVO cartVO, Model model){
        model.addAttribute("cart", cartVO)
    }


    Model loadCart(String cartid, Model model, CartVO cartVO, String menuid){
        // if cartid == 0 then load normally, otherwise load the existing transaction
        if(cartid == "0"){
            // do nothing
            // if it is the first time loading the page
            if(cartVO.product_cart_list == null){
                cartVO.setTotal(0) // set total to 0 initially
                cartVO.product_cart_list = new ArrayList<>()
            }
            cartVO.menuid = menuid
            model.addAttribute("cart", cartVO);

        } else {
            cartVO.menuid = menuid
            cartVO = getExistingCart(cartid)
            cartVO = hydrateTransientQuantitiesForDisplay(cartVO)
            model.addAttribute("cart", cartVO)
        }

    }

    Model loadCartForCheckout(String cartid, Model model, CartVO cartVO){
        // if cartid == 0 then load normally, otherwise load the existing transaction
        if(cartid == "0"){
            // do nothing
            // if it is the first time loading the page
            if(cartVO.product_cart_list == null){
                cartVO.setTotal(0) // set total to 0 initially
                cartVO.product_cart_list = new ArrayList<>()
            }
            model.addAttribute("cart", cartVO);

        } else {
            cartVO = getExistingCart(cartid)
            cartVO = hydrateTransientQuantitiesForDisplay(cartVO)
            model.addAttribute("cart", cartVO)
        }

    }




}
