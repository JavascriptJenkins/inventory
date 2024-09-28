package com.techvvs.inventory.viewcontroller.helper



import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.CrateRepo
import com.techvvs.inventory.jparepo.PackageRepo
import com.techvvs.inventory.model.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.ui.Model

// helper class to make sure actual checkout controller code stays clean n tidy
@Component
class CrateHelper {

    @Autowired
    CustomerRepo customerRepo

    @Autowired
    CrateRepo crateRepo

    @Autowired
    PackageRepo packageRepo

    // method to get all customers from db
    void getAllCustomers(Model model){

        List<CustomerVO> customers = customerRepo.findAll()
        model.addAttribute("customers", customers)
    }



    void findPendingCrates(Model model, Optional<Integer> page, Optional<Integer> size){

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
        Page<CrateVO> pageOfCrate = runPageRequest(pageable)

        int totalPages = pageOfCrate.getTotalPages();
        int contentsize = pageOfCrate.getContent().size()

        if(contentsize == 0){
            // we detect contentsize of 0 then we'll just take the first page of data and show it
            pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, "createTimeStamp"));
            pageOfCrate = runPageRequest(pageable)
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
        model.addAttribute("pageOfCrate", pageOfCrate);
        // END PAGINATION

    }

    Page<CrateVO> runPageRequest(Pageable pageable) {
        return crateRepo.findAllByIsprocessed(0,pageable);
    }

    // This will calculate display value for the total number of packages in a crate
    CrateVO hydrateTransientQuantitiesForDisplay(CrateVO crateVO){
        Map<Integer, PackageVO> packageMap = new HashMap<>();
        crateVO.displayquantitytotal = 0
        crateVO.package_list.sort { a, b -> a.total <=> b.total }

        for(PackageVO packageincrate : crateVO.package_list){

            if(crateVO.displayquantitytotal == 0){
                packageincrate.displayquantitytotal = 1
                crateVO.displayquantitytotal = 1
            } else {
                crateVO.displayquantitytotal = crateVO.displayquantitytotal + 1
               // packageincrate.displayquantitytotal = packageincrate.displayquantitytotal + 1
            }

            packageMap.put(packageincrate.packageid, packageincrate)
        }
        crateVO.package_list = new ArrayList<>(packageMap.values());
//        for(PackageVO packageincrate : crateVO.package_list) {
//            crateVO.displayquantitytotal += productincrate.displayquantity
//        }
        return crateVO
    }

    // todo: should do a check on here to see if crate name already exists?
    CrateVO validateCrateReviewVO(CrateVO crateVO, Model model){
        if(crateVO?.name == null || crateVO?.name == "" || crateVO.name.trim().length() < 3){
            model.addAttribute("errorMessage","Please enter a crate name greater than 3 characters. ")
        }
        return crateVO
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


    CrateVO getExistingCrate(String crateid){

        Optional<CrateVO> crateVO = crateRepo.findById(Integer.valueOf(crateid))

        if(!crateVO.empty){
            return crateVO.get()
        } else {
            return new CrateVO(crateid: 0)
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

    Model loadCrate(String crateid,
                    Model model,
                    Optional<Integer> page,
                    Optional<Integer> size){

        // if crateid == 0 then load normally, otherwise load the existing transaction
        if(crateid == "0"){
            CrateVO crateVO = new CrateVO(crateid: 0)
            // do nothing
            // if it is the first time loading the page
            if(crateVO.package_list == null){
                crateVO.setTotal(0.00) // set total to 0 initially
                crateVO.setWeight(0.00) // set total to 0 initially
                crateVO.package_list = new ArrayList<>()
            }
            model.addAttribute("crate", crateVO);

        } else {


            CrateVO crateVO = new CrateVO()
            crateVO = getExistingCrate(crateid)
            // todo: run pagination here to bind crate
            bindPackagesInCrate(model, page, size, crateVO) // this binds the pageOfPackageInCrate
            crateVO = hydrateTransientQuantitiesForDisplay(crateVO)
            model.addAttribute("crate", crateVO)
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

    // todo: placeholder method here to bind the pages of packages inside the crate
    void bindPackagesInCrate(Model model,
                      Optional<Integer> page,
                      Optional<Integer> size,
                             CrateVO crateVO

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
        Page<PackageVO> pageOfPackageInCrate = packageRepo.findAllByCrate(crateVO,pageable);  // Fetch paginated results

        int totalPages = pageOfPackageInCrate.getTotalPages();
        int contentsize = pageOfPackageInCrate.getContent().size()

        if(contentsize == 0){
            // we detect contentsize of 0 then we'll just take the first page of data and show it
            pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, "createTimeStamp"));
            pageOfPackageInCrate = packageRepo.findAllByCrate(crateVO,pageable);
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
        model.addAttribute("pageOfPackageInCrate", pageOfPackageInCrate);
    }


}
