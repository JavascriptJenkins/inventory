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
import com.techvvs.inventory.service.transactional.CartDeleteService
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
    CartRepo cartRepo


    // method to get all customers from db
    void getAllCustomers(Model model){

        List<CustomerVO> customers = customerRepo.findAll()
        model.addAttribute("customers", customers)
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

    CartVO hydrateTransientQuantitiesForDisplay(CartVO cartVO){
        cartVO.displayquantitytotal = 0
        cartVO.product_cart_list.sort { a, b -> a.price <=> b.price }
        Map<Integer, ProductVO> productMap = new HashMap<>();
        // cycle thru here and if the productid is the same then update the quantity
        for(ProductVO productincart : cartVO.product_cart_list){

            if(productincart.displayquantity == null){
                productincart.displayquantity = 1
            } else {
                productincart.displayquantity = productincart.displayquantity + 1
            }

            productMap.put(productincart.getProduct_id(), productincart)
        }
        cartVO.product_cart_list = new ArrayList<>(productMap.values());
        for(ProductVO productincart : cartVO.product_cart_list) {
            cartVO.displayquantitytotal += productincart.displayquantity
        }
        return cartVO

    }



    TransactionVO hydrateTransientQuantitiesForTransactionDisplay(TransactionVO transactionVO) {
        int totalDisplayQuantity = 0

        // run a sort on the product list right here
        transactionVO.product_list.sort { a, b -> a.price <=> b.price }

        // Ensure product_list is not null or empty before processing
        if (transactionVO == null || transactionVO.product_list == null || transactionVO.product_list.isEmpty()) {
            return transactionVO; // Return early if no products to process
        }

        // Map to track product quantities by barcode
        Map<String, ProductVO> barcodeMap = new HashMap<>();

        // Loop through the products
        for (ProductVO productVO : transactionVO.product_list) {
            // If displayquantity is null, initialize it to 1
            if (productVO.displayquantity == null) {
                productVO.displayquantity = 1;
            }

            // Add the displayquantity to totalDisplayQuantity
            totalDisplayQuantity += productVO.displayquantity

            // Check if the product barcode already exists in the map
            if (barcodeMap.containsKey(productVO.barcode)) {
                // If the barcode is already in the map, increment the displayquantity of the stored product
                ProductVO existingProduct = barcodeMap.get(productVO.barcode);
                existingProduct.displayquantity += productVO.displayquantity;
            } else {
                // If it's a new barcode, add it to the map
                barcodeMap.put(productVO.barcode, productVO);
            }
        }

        // After processing, update the original product list quantities
        for (ProductVO productVO : transactionVO.product_list) {
            if (barcodeMap.containsKey(productVO.barcode)) {
                // Update the product's displayquantity with the accumulated value from the map
                productVO.displayquantity = barcodeMap.get(productVO.barcode).displayquantity;
            }
        }

        transactionVO.displayquantitytotal = totalDisplayQuantity

        return transactionVO;
    }

    TransactionVO bindtransients(TransactionVO transactionVO, String phone, String email, String action){
        transactionVO.phonenumber = phone.replace(",", "")
        transactionVO.email = email.replace(",", "")
        transactionVO.action = action.replace(",", "")
        transactionVO.filename = action.replace(",", "")
        return transactionVO
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
                cartVO.setTotal(0.00) // set total to 0 initially
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
                cartVO.setTotal(0.00) // set total to 0 initially
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
