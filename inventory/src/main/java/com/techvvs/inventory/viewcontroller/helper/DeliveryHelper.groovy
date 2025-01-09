package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.CrateRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.DeliveryRepo
import com.techvvs.inventory.jparepo.MenuRepo
import com.techvvs.inventory.jparepo.PackageRepo
import com.techvvs.inventory.model.*
import com.techvvs.inventory.security.JwtTokenProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.ui.Model

// helper class to make sure actual checkout controller code stays clean n tidy
@Component
class DeliveryHelper {

    @Autowired
    CustomerRepo customerRepo

    @Autowired
    CrateRepo crateRepo

    @Autowired
    PackageRepo packageRepo

    @Autowired
    DeliveryRepo deliveryRepo

    @Autowired
    ProductHelper productHelper

    @Autowired
    JwtTokenProvider jwtTokenProvider

    @Autowired
    MenuRepo menuRepo

    @Autowired
    AppConstants appConstants

    // method to get all customers from db
    void getAllCustomers(Model model){

        List<CustomerVO> customers = customerRepo.findAll()
        model.addAttribute("customers", customers)
    }



    void findPendingDeliveries(Model model, Optional<Integer> page, Optional<Integer> size){

        // START PAGINATION
        // https://www.baeldung.com/spring-data-jpa-pagination-sorting
        //pagination
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
        Page<DeliveryVO> pageOfDelivery = runPageRequest(pageable)

        int totalPages = pageOfDelivery.getTotalPages();
        int contentsize = pageOfDelivery.getContent().size()

        if(contentsize == 0){
            // we detect contentsize of 0 then we'll just take the first page of data and show it
            pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, "createTimeStamp"));
            pageOfDelivery = runPageRequest(pageable)
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
        model.addAttribute("pageOfDelivery", pageOfDelivery);
        // END PAGINATION

    }

    Page<DeliveryVO> runPageRequest(Pageable pageable) {
        return deliveryRepo.findAllByIsprocessed(0,pageable);
    }

    // This will calculate display value for the total number of packages in a delivery
    DeliveryVO hydrateTransientQuantitiesForDisplay(DeliveryVO deliveryVO){

        sortPackages(deliveryVO)
        sortCrates(deliveryVO)
        // consolidate the product lists down for display

        deliveryVO.crateinscope != null ? deliveryVO.crateinscope.package_list.each {item -> productHelper.consolidateProductListForDisplay(item)} : null

        return deliveryVO
    }

    void sortPackages(DeliveryVO deliveryVO){
        if(deliveryVO.package_list != null) {

            Map<Integer, PackageVO> packageMap = new HashMap<>();
            deliveryVO.displayquantitytotalpackages = 0
            deliveryVO.package_list.sort { a, b -> a.total <=> b.total }

            for (PackageVO packageindelivery : deliveryVO.package_list) {

                if (deliveryVO.displayquantitytotalpackages == 0) {
               //     packageindelivery.displayquantitytotalpackages = 1
                    deliveryVO.displayquantitytotalpackages = 1
                } else {
                    deliveryVO.displayquantitytotalpackages = deliveryVO.displayquantitytotalpackages + 1
                    // packageindelivery.displayquantitytotal = packageindelivery.displayquantitytotal + 1
                }

                packageMap.put(packageindelivery.packageid, packageindelivery)
            }
            deliveryVO.package_list = new ArrayList<>(packageMap.values());
//        for(PackageVO packageindelivery : deliveryVO.package_list) {
//            deliveryVO.displayquantitytotal += productindelivery.displayquantity
//        }
        }
    }

    void sortCrates(DeliveryVO deliveryVO){

        if(deliveryVO.crate_list != null){
            Map<Integer, CrateVO> crateMap = new HashMap<>();
            deliveryVO.displayquantitytotalcrates = 0
            deliveryVO.crate_list.sort { a, b -> a.total <=> b.total }

            for(CrateVO crateindelivery : deliveryVO.crate_list){

                if(deliveryVO.displayquantitytotalcrates == 0){
                //    crateindelivery.displayquantitytotalcrates = 1
                    deliveryVO.displayquantitytotalcrates = 1
                } else {
                    deliveryVO.displayquantitytotalcrates = deliveryVO.displayquantitytotalcrates + 1
                    // crateindelivery.displayquantitytotal = crateindelivery.displayquantitytotal + 1
                }

                crateMap.put(crateindelivery.crateid, crateindelivery)
            }
            deliveryVO.crate_list = new ArrayList<>(crateMap.values());
//        for(CrateVO crateindelivery : deliveryVO.crate_list) {
//            deliveryVO.displayquantitytotal += productindelivery.displayquantity
//        }
        }

    }
    // todo: should do a check on here to see if delivery name already exists?
    DeliveryVO validateDeliveryReviewVO(DeliveryVO deliveryVO, Model model){
        if(deliveryVO?.name == null || deliveryVO?.name == "" || deliveryVO.name.trim().length() < 3){
            model.addAttribute("errorMessage","Please enter a delivery name greater than 3 characters. ")
        }
        return deliveryVO
    }


//    CartVO hydrateTransientQuantitiesForDisplay(CartVO cartVO){
//        Map<Integer, ProductVO> productMap = new HashMap<>();
//        // cycle thru here and if the productid is the same then update the quantity
//        ProductVO previous = new ProductVO(barcode: 0)
//        for(ProductVO productVO : cartVO.product_cart_list){
//            if(productVO.displayquantity == null){
//                productVO.displayquantity = 1
//            }
//            if(productVO.barcode == previous.barcode){
//                    productVO.displayquantity = productVO.displayquantity + 1
//            }
//            productMap.put(productVO.getProduct_id(), productVO)
//            previous = productVO
//        }
//        cartVO.product_cart_list = new ArrayList<>(productMap.values());
//        return cartVO
//
//    }

    TransactionVO hydrateTransientQuantitiesForTransactionDisplay(TransactionVO transactionVO){

        // cycle thru here and if the productid is the same then update the quantity
        ProductVO previous = new ProductVO(barcode: 0)
        for(ProductVO productVO : transactionVO.product_list){
            if(productVO.displayquantity == null){
                productVO.displayquantity = 1
            }
            if(productVO.barcode == previous.barcode){
                productVO.displayquantity = productVO.displayquantity + 1
            }
            previous = productVO
        }

        return transactionVO

    }

    TransactionVO bindtransients(TransactionVO transactionVO, String phone, String email, String action){
        transactionVO.phonenumber = phone.replace(",", "")
        transactionVO.email = email.replace(",", "")
        transactionVO.action = action.replace(",", "")
        transactionVO.filename = action.replace(",", "")
        return transactionVO
    }


    DeliveryVO getExistingDelivery(String deliveryid){

        Optional<DeliveryVO> deliveryVO = deliveryRepo.findById(Integer.valueOf(deliveryid))

        if(!deliveryVO.empty){
            deliveryVO.get().crate_list = crateRepo.findAllByDelivery(deliveryVO.get())
            deliveryVO.get().package_list = packageRepo.findAllByDelivery(deliveryVO.get())
            deliveryVO.get().getCrate_list().size(); // initiate lazy list
            deliveryVO.get().getPackage_list().size(); // initiate lazy list
            return deliveryVO.get()
        } else {
            return new DeliveryVO(deliveryid: 0)
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

    Model loadDelivery(String deliveryid,
                    Model model,
                    Optional<Integer> page,
                    Optional<Integer> size){

        // if deliveryid == 0 then load normally, otherwise load the existing transaction
        if(deliveryid == "0"){
            DeliveryVO deliveryVO = new DeliveryVO(deliveryid: 0)
            // do nothing
            // if it is the first time loading the page
            if(deliveryVO.package_list == null){
                deliveryVO.package_list = new ArrayList<>()
            }
            model.addAttribute("delivery", deliveryVO);

        } else {


            DeliveryVO deliveryVO = new DeliveryVO()
            deliveryVO = getExistingDelivery(deliveryid)
            // todo: run pagination here to bind delivery
            bindPackagesInDelivery(model, page, size, deliveryVO) // this binds the pageOfPackageInDelivery
            bindCratesInDelivery(model, page, size, deliveryVO) // this binds the pageOfPackageInDelivery
            deliveryVO = hydrateTransientQuantitiesForDisplay(deliveryVO)
            model.addAttribute("delivery", deliveryVO)
        }

    }


    void bindUnprocessedPackages(Model model,
                      Optional<Integer> page,
                      Optional<Integer> size

    ){


        //pagination
        int currentPage = page.orElse(0);
        int pageSize = size.orElse(5);

        if(
                currentPage > pageSize
        ){
            currentPage = 0;
        }

        pageSize = pageSize < 5 ? 5 : pageSize; // make sure it's not less than 5

        // run first page request
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.ASC, "createTimeStamp"));
        Page<PackageVO> pageOfPackage = packageRepo.findAllByIsprocessed(0,pageable,);  // Fetch paginated results

        int totalPages = pageOfPackage.getTotalPages();
        int contentsize = pageOfPackage.getContent().size()

        if(contentsize == 0){
            // we detect contentsize of 0 then we'll just take the first page of data and show it
            pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, "createTimeStamp"));
            pageOfPackage = packageRepo.findAllByIsprocessed(0,pageable)
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
        model.addAttribute("packagePage", pageOfPackage);
    }

    void bindUnprocessedCrates(Model model,
                                 Optional<Integer> page,
                                 Optional<Integer> size

    ){


        //pagination
        int currentPage = page.orElse(0);
        int pageSize = size.orElse(5);

        if(
                currentPage > pageSize
        ){
            currentPage = 0;
        }

        pageSize = pageSize < 5 ? 5 : pageSize; // make sure it's not less than 5

        // run first page request
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.ASC, "createTimeStamp"));
        Page<CrateVO> pageOfCrate = crateRepo.findAllByIsprocessed(0,pageable,);  // Fetch paginated results

        int totalPages = pageOfCrate.getTotalPages();
        int contentsize = pageOfCrate.getContent().size()

        if(contentsize == 0){
            // we detect contentsize of 0 then we'll just take the first page of data and show it
            pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, "createTimeStamp"));
            pageOfCrate = crateRepo.findAllByIsprocessed(0,pageable)
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
        model.addAttribute("cratePage", pageOfCrate);
    }

    // todo: placeholder method here to bind the pages of packages inside the delivery
    void bindPackagesInDelivery(Model model,
                      Optional<Integer> page,
                      Optional<Integer> size,
                             DeliveryVO deliveryVO

    ){


        //pagination
        int currentPage = page.orElse(0);
        int pageSize = size.orElse(5);

        if(
                currentPage > pageSize
        ){
            currentPage = 0;
        }

        pageSize = pageSize < 5 ? 5 : pageSize; // make sure it's not less than 5


        // run first page request
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.ASC, "createTimeStamp"));
        Page<PackageVO> pageOfPackageInDelivery = packageRepo.findAllByDelivery(deliveryVO,pageable);  // Fetch paginated results

        int totalPages = pageOfPackageInDelivery.getTotalPages();
        int contentsize = pageOfPackageInDelivery.getContent().size()

        if(contentsize == 0){
            // we detect contentsize of 0 then we'll just take the first page of data and show it
            pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, "createTimeStamp"));
            pageOfPackageInDelivery = packageRepo.findAllByDelivery(deliveryVO,pageable);
        }

        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNumbers.add(i);
        }

        if(currentPage > totalPages){
            currentPage = 0;
        }

        model.addAttribute("deliverypageNumbers", pageNumbers);
        model.addAttribute("deliverypage", currentPage);
        model.addAttribute("deliverysize", pageSize);
        model.addAttribute("pageOfPackageInDelivery", pageOfPackageInDelivery);
    }

    void bindCratesInDelivery(Model model,
                                Optional<Integer> page,
                                Optional<Integer> size,
                                DeliveryVO deliveryVO

    ){


        //pagination
        int currentPage = page.orElse(0);
        int pageSize = size.orElse(5);

        if(
                currentPage > pageSize
        ){
            currentPage = 0;
        }

        pageSize = pageSize < 5 ? 5 : pageSize; // make sure it's not less than 5


        // run first page request
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.ASC, "createTimeStamp"));
        Page<CrateVO> pageOfCrateInDelivery = crateRepo.findAllByDelivery(deliveryVO,pageable);  // Fetch paginated results

        int totalPages = pageOfCrateInDelivery.getTotalPages();
        int contentsize = pageOfCrateInDelivery.getContent().size()

        if(contentsize == 0){
            // we detect contentsize of 0 then we'll just take the first page of data and show it
            pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, "createTimeStamp"));
            pageOfCrateInDelivery = crateRepo.findAllByDelivery(deliveryVO,pageable);
        }

        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNumbers.add(i);
        }

        if(currentPage > totalPages){
            currentPage = 0;
        }

        model.addAttribute("cratepageNumbers", pageNumbers);
        model.addAttribute("cratepage", currentPage);
        model.addAttribute("cratesize", pageSize);
        model.addAttribute("pageOfCrateInDelivery", pageOfCrateInDelivery);
    }


    void loadDeliveryByDeliveryToken(String deliverytoken, Model model){
        String deliveryid = jwtTokenProvider.getDeliveryIdFromToken(deliverytoken)
        String menuid = jwtTokenProvider.getMenuIdFromToken(deliverytoken)
        Optional<DeliveryVO> deliveryVO = deliveryRepo.findById(Integer.valueOf(deliveryid))

        // process the products here for display

        if(deliveryVO.present){
            model.addAttribute("delivery", deliveryVO.get())
            hydrateTransientQuantitiesForDisplayForClientStatusView(deliveryVO.get(), menuid)
        } else {
            // return empty object to ui if we don't find one
            model.addAttribute("delivery", new DeliveryVO(deliveryid:0))
        }


    }


    // we need to bind these because initially they come from the user navigating from a phone SMS message with them in the URI
    void bindHiddenValues(Model model, String shoppingtoken){
        model.addAttribute("shoppingtoken", shoppingtoken)
    }



    DeliveryVO hydrateTransientQuantitiesForDisplayForClientStatusView(DeliveryVO deliveryVO, String menuid){

        MenuVO menuVO = menuRepo.findById(Integer.valueOf(menuid)).get()

        deliveryVO.displayquantitytotal = 0
        deliveryVO.package_list.product_package_list.sort { a, b -> a.price <=> b.price }
        Map<Integer, ProductVO> productMap = new HashMap<>();

        for(PackageVO packageVO : deliveryVO.package_list){
            for(ProductVO productInDelivery : packageVO.product_package_list){
                if(productInDelivery.displayquantity == null){
                    productInDelivery.displayquantity = 1
                } else {
                    productInDelivery.displayquantity = productInDelivery.displayquantity + 1
                }

                // apply discount on by product_type to the total
                applyDiscountByProductTypeForDelivery(Optional.of(deliveryVO), productInDelivery, menuVO)

                // in the future, we can apply a per product discount here
                deliveryVO.displayquantitytotal += 1 // increment up the displayquantitytotal for every product
                productMap.put(productInDelivery.getProduct_id(), productInDelivery)
            }
        }

        deliveryVO.progressWidth = calculateProgressWidth(deliveryVO)
        deliveryVO.allproductsinpackages = new ArrayList<>(productMap.values());

        return deliveryVO
    }

    int calculateProgressWidth(DeliveryVO deliveryVO){
        switch (deliveryVO.status){
            case appConstants.DELIVERY_STATUS_CREATED:
                return 25
            case appConstants.DELIVERY_STATUS_PREPPING:
                return 50
            case appConstants.DELIVERY_STATUS_EN_ROUTE:
                return 75
            case appConstants.DELIVERY_STATUS_DELIVERED:
                return 100
            default:
                return 0
        }
    }

    // NOTE: this using optional to process discounts is sweet.  Use this pattern again.
    void applyDiscountByProductTypeForDelivery(Optional<DeliveryVO> deliveryVO,ProductVO productVO, MenuVO menuVO) {
        // need to check for product.menu, if it exists, cycle thru the discount list on menu and apply it to the total
        boolean priceset = false
        if (deliveryVO.present){
            for(DiscountVO discountVO : menuVO.discount_list) {
                if (productVO.getProducttypeid().producttypeid == discountVO.producttype.producttypeid) {
                    deliveryVO.get().total = Math.max(0,deliveryVO.get().total - discountVO.discountamount)
                    // this will subtract the discount amount for every product in the cart
                    productVO.displayprice = Math.max(0,productVO.price - discountVO.discountamount)
                    priceset = true
                }
                if(!priceset){
                    productVO.displayprice = productVO.price
                }
            }
        }
    }


}
