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
                if(cartcount >= productVO.get().quantity){
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
                !doesCartExist(cartVO)
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


    boolean doesCartExist(CartVO cartVO){
        if(cartVO == null || cartVO.cartid == null){
            return false
        }
        Optional<CartVO> existingcart = cartRepo.findById(cartVO.cartid)
        return !existingcart.empty
    }

    Optional<ProductVO> doesProductExist(String barcode){

        Optional<ProductVO> existingproduct = productRepo.findByBarcode(barcode)
        return existingproduct
    }

    boolean doesTransactionExist(CustomerVO customerVO, String barcode){


        List<TransactionVO> existingtransactions = transactionRepo.findAllByCustomervo(customerVO)




        // todo: remove this stupid check for a case that shoulndt happen
        // if the customer already has a transaction that exists
        for(TransactionVO transactionVO : existingtransactions){
            if(transactionVO.isprocessed == 0 && transactionVO?.cart?.product_cart_list?.size() == 0){

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

    // add product to cart and then update the cart and product associations
    CartVO  searchForProductByBarcode(CartVO cartVO, Model model, Optional<Integer> page, Optional<Integer> size){


        Optional<ProductVO> productVO = productRepo.findByBarcode(cartVO.barcode)

        // todo: on second time thru we need to fully hydrate the customer and product_set before saving

        if(!productVO.empty){

            // update the product cart list association
            if(productVO.get().cart_list == null){
                productVO.get().cart_list = new ArrayList<>()
            }
            productVO.get().cart_list.add(cartVO)

            productVO.get().updateTimeStamp = LocalDateTime.now()
            ProductVO savedProduct = productRepo.save(productVO.get())


            /* Cart code below */
            cartVO.total += Integer.valueOf(productVO.get().price) // add the product price to the total

            // handle quantity here (have to iterate thru all product cert list and update the quantity)

            // if it's the first time adding a product we need to create the set to hold them
            if(cartVO.product_cart_list == null){
                cartVO.product_cart_list = new ArrayList<ProductVO>()
            }
            // now save the cart side of the many to many
            cartVO.product_cart_list.add(savedProduct)
            cartVO.updateTimeStamp = LocalDateTime.now()
            cartVO = cartRepo.save(cartVO)
            model.addAttribute("successMessage","Product: "+productVO.get().name + " added successfully")
        } else {
            // need to bind the selected customer here otherwise the dropdown wont work
            cartVO.customer = customerRepo.findById(cartVO.customer.customerid).get()
            model.addAttribute("errorMessage","Product not found")
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





}
