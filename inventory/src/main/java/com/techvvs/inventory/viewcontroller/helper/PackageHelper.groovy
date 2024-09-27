package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.CrateRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.PackageRepo
import com.techvvs.inventory.jparepo.PackageTypeRepo
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CrateVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.PackageTypeVO
import com.techvvs.inventory.model.PackageVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.ui.Model

// helper class to make sure actual checkout controller code stays clean n tidy
@Component
class PackageHelper {

    @Autowired
    CustomerRepo customerRepo

    @Autowired
    PackageRepo packageRepo

    @Autowired
    CrateRepo crateRepo

    @Autowired
    PackageTypeRepo packageTypeRepo


    // method to get all customers from db
    void getAllCustomers(Model model){

        List<CustomerVO> customers = customerRepo.findAll()
        model.addAttribute("customers", customers)
    }

    void getAllPackageTypes(Model model){

        List<PackageTypeVO> packageTypeVOS = packageTypeRepo.findAll()
        model.addAttribute("packagetypes", packageTypeVOS)
    }


    PackageVO validatePackageReviewVO(PackageVO packageVO, Model model){
        if(packageVO?.packagetype?.packagetypeid == null){
            model.addAttribute("errorMessage","Please select a package type")
        }
        if(packageVO?.name == null || packageVO?.name == "" || packageVO.name.trim().length() < 3){
            model.addAttribute("errorMessage","Please enter a package name greater than 3 characters. ")
        }
        return packageVO
    }

    CartVO validateTransaction(CartVO cartVO, Model model){
        if(cartVO?.customer?.customerid == null){
            model.addAttribute("errorMessage","Please select a customer")
        }
        return cartVO
    }

    void findPendingPackages(Model model, Optional<Integer> page, Optional<Integer> size) {
        // START PAGINATION
        // pagination
        int currentPage = page.orElse(0);    // Default to first page
        int pageSize = size.orElse(5);       // Default page size to 5
        Pageable pageable = PageRequest.of(currentPage, pageSize);

        Page<PackageVO> pageOfPackage = packageRepo.findAllByIsprocessed(0,pageable);  // Fetch paginated results

        int totalPages = pageOfPackage.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNumbers.add(i);
        }


        // Add attributes to the model
        model.addAttribute("pageNumbers", pageNumbers);           // List of page numbers for pagination
        model.addAttribute("page", currentPage);                  // Current page number
        model.addAttribute("size", pageSize);                     // Page size (items per page)
        model.addAttribute("pageOfPackage", pageOfPackage);       // Paginated data
        // END PAGINATION
    }


    // this condenses down the list of products into a map of products with a displayquantity
    // i don't know if this is the right way to do this for packages but we'll see.....
    PackageVO hydrateTransientQuantitiesForDisplay(PackageVO packageVO){
        Map<Integer, ProductVO> productMap = new HashMap<>();
        packageVO.displayquantitytotal = 0
        packageVO.product_package_list.sort { a, b -> a.price <=> b.price }

        for(ProductVO productinpackage : packageVO.product_package_list){

            if(productinpackage.displayquantity == null){
                productinpackage.displayquantity = 1
            } else {
                productinpackage.displayquantity = productinpackage.displayquantity + 1
            }

            productMap.put(productinpackage.getProduct_id(), productinpackage)
        }
        packageVO.product_package_list = new ArrayList<>(productMap.values());
        for(ProductVO productinpackage : packageVO.product_package_list) {
            packageVO.displayquantitytotal += productinpackage.displayquantity
        }
        return packageVO
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


    PackageVO getExistingPackage(String packageid){

        Optional<PackageVO> packageVO = packageRepo.findById(Integer.valueOf(packageid))

        if(!packageVO.empty){
            return packageVO.get()
        } else {
            return new PackageVO(packageid: 0)
        }
    }

    PackageVO getExistingCrateByPackageId(String packageid){

        Optional<PackageVO> packageVO = crateRepo.findById(Integer.valueOf(packageid))

        if(!packageVO.empty){
            return packageVO.get()
        } else {
            return new PackageVO(packageid: 0)
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

    Model loadPackage(String packageid, Model model){

        // if packageid == 0 then load normally, otherwise load the existing transaction
        if(packageid == "0"){
            PackageVO packageVO = new PackageVO(packageid: 0)
            // do nothing
            // if it is the first time loading the page
            if(packageVO.product_package_list == null){
                packageVO.setTotal(0.00) // set total to 0 initially
                packageVO.product_package_list = new ArrayList<>()
            }
            model.addAttribute("package", packageVO);

        } else {
            PackageVO packageVO = new PackageVO()
            packageVO = getExistingPackage(packageid)
            packageVO = hydrateTransientQuantitiesForDisplay(packageVO)
            model.addAttribute("package", packageVO)
        }

    }




}
