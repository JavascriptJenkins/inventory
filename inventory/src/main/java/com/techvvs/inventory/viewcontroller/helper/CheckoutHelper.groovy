package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.DiscountVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.PackageVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.service.controllers.CartService
import com.techvvs.inventory.service.transactional.CartDeleteService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
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

    @Autowired
    BatchRepo batchRepo


    // method to get all customers from db
    void getAllCustomers(Model model){

        List<CustomerVO> customers = customerRepo.findAll()
        model.addAttribute("customers", customers)
    }

    // method to get all customers from db
    void getAllBatches(Model model){

        List<BatchVO> batchlist = batchRepo.findAll()
        model.addAttribute("batchlist", batchlist)
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
        // pagination
        int currentPage = page.orElse(0);    // Default to first page
        int pageSize = size.orElse(5);       // Default page size to 5


        if(
                currentPage > pageSize
        ){
            currentPage = 0;
        }

        pageSize = pageSize < 5 ? 5 : pageSize; // make sure it's not less than 5

        // run first page request
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.ASC, "createTimeStamp"));
        Page<CartVO> pageOfCart = cartRepo.findAllByIsprocessed(0,pageable,);  // Fetch paginated results

        int totalPages = pageOfCart.getTotalPages();
        int contentsize = pageOfCart.getContent().size()

        if(contentsize == 0){
            // we detect contentsize of 0 then we'll just take the first page of data and show it
            pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, "createTimeStamp"));
            pageOfCart = runPageRequest(pageable)
        }

        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNumbers.add(i);
        }

        if(currentPage > totalPages){
            currentPage = 0;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageSize);
        model.addAttribute("cartPage", pageOfCart);
        // END PAGINATION



    }

    Page<CartVO> runPageRequest(Pageable pageable) {
        return cartRepo.findAll(pageable);
    }

    CartVO hydrateTransientQuantitiesForDisplay(CartVO cartVO){
        cartVO.originalprice = 0
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

            cartVO.originalprice += productincart.price // hydrate the originalprice before any discounts

            // apply discount on by product_type to the total
            applyDiscountByProductTypeForCart(Optional.of(cartVO), productincart)

            // in the future, we can apply a per product discount here


            productMap.put(productincart.getProduct_id(), productincart)
        }
        cartVO.product_cart_list = new ArrayList<>(productMap.values());
        for(ProductVO productincart : cartVO.product_cart_list) {
            cartVO.displayquantitytotal += productincart.displayquantity
        }
        return cartVO

    }

    // NOTE: this using optional to process discounts is sweet.  Use this pattern again.
    void applyDiscountByProductTypeForCart(Optional<CartVO> cartVO,ProductVO productVO) {
        // need to check for product.menu, if it exists, cycle thru the discount list on menu and apply it to the total
        if (cartVO.present){
            for(DiscountVO discountVO : cartVO.get().menu.discount_list) {
                if (productVO.getProducttypeid().producttypeid == discountVO.producttype.producttypeid) {
                    cartVO.get().total = Math.max(0,cartVO.get().total - discountVO.discountamount)
                    // this will subtract the discount amount for every product in the cart
                }
            }
        }
    }

    // NOTE: this using optional to process discounts is sweet.  Use this pattern again.
    void applyDiscountByProductTypeForMenu(Optional<MenuVO> menuVO, ProductVO productVO) {
        // discount the individual product in the menu so the price displays to the user correctly
        if (menuVO.present){
            for(DiscountVO discountVO : menuVO.get().discount_list) {
                if (productVO.getProducttypeid().producttypeid == discountVO.producttype.producttypeid) {
                    //productVO.price = Math.max(0,productVO.price - discountVO.discountamount)
                    productVO.displayprice = Math.max(0,productVO.price - discountVO.discountamount)
                }
            }
        }
    }



    TransactionVO hydrateTransientQuantitiesForTransactionDisplay(TransactionVO transactionVO, Model model) {
        int totalDisplayQuantity = 0

        // run a sort on the product list right here
        transactionVO.product_list.sort { a, b -> a.price <=> b.price }


        // Map to track product quantities by barcode
        Map<String, ProductVO> barcodeMap = new HashMap<>();

        // Loop through the products
        for (ProductVO productVO : transactionVO.product_list) {
            // If displayquantity is null, initialize it to 1
            if (productVO.displayquantity == null) {
                productVO.displayquantity = 1;
            }

            // Check if the product barcode already exists in the map
            if (!barcodeMap.containsKey(productVO.barcode)) {
                // If the barcode is already in the map, increment the displayquantity of the stored product
                barcodeMap.put(productVO.barcode, productVO);
            } else {
                productVO.displayquantity += 1
            }
        }

        // After processing, update the original product list quantities
        for (Map.Entry<String, ProductVO> productVO : barcodeMap) {
            totalDisplayQuantity += productVO.getValue().displayquantity
        }

        transactionVO.displayquantitytotal = totalDisplayQuantity

        // now group the returns
        transactionVO =sortAndGroupReturns(transactionVO, model)

        return transactionVO;
    }


    TransactionVO sortAndGroupReturns(TransactionVO transactionVO, Model model){
        // Group and count returns by product
        def groupedReturns = transactionVO.return_list.groupBy { it.product.product_id }
                .collectEntries { key, value -> [(value[0].product): value.size()] }
        model.addAttribute("groupedReturns", groupedReturns);
        return transactionVO
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
