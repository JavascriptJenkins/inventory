package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchTypeVO
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.modelnonpersist.FileVO
import com.techvvs.inventory.util.TechvvsFileHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.ui.Model


/* This class exists to keep the main batch controller code clean and avoid duplication. */
@Component
class BatchControllerHelper {

    @Autowired
    TechvvsFileHelper techvvsFileHelper

    @Autowired
    BatchTypeRepo batchTypeRepo

    @Autowired
    AppConstants appConstants

    @Autowired
    ProductRepo productRepo

    @Autowired
    ProductTypeRepo productTypeRepo

    @Autowired
    BatchRepo batchRepo


    // method for combining logic of editform and filtereditform
    Model processModel(Model model,
                       String customJwtParameter,
                       String batchnumber,
                       String editmode,
                       Optional<Integer> page,
                       ProductTypeVO productTypeVO,
                       boolean isFilteredView,
                       boolean isProductNameLikeSearch

    ){

        bindCommonElements(model, customJwtParameter, batchnumber, editmode);

        boolean hasProductTypeId = productTypeVO?.producttypeid != null

        if (isFilteredView) {
            if (isProductNameLikeSearch) {
                bindFilterProductsLikeSearch(model, page, productTypeVO.name, productTypeVO);
            } else if (hasProductTypeId) {
                bindFilterProducts(model, page, productTypeVO);
            } else {
                bindProducts(model, page, productTypeVO);
            }
            model.addAttribute("searchproducttype", productTypeVO); // use the existing object for submitting a search term
        } else {
            bindProducts(model, page, productTypeVO);
            model.addAttribute("searchproducttype", new ProductTypeVO()); // create a new object for submitting a search term
        }

    }


    void calculateStaticPageData(Model model, BatchVO batchVO){

        // todo: map these id's better?
        int INDOOR_PRODUCT_TYPE_ID = 35;
        int totalIndoorQuantityRemaining = 0;
        int totalIndoorQuantity = 0;
        int batchValueTotal = 0;
        int batchValueRemainingTotal = 0;
        int totalCartQuantityRemaining = 0;
        int totalEdibleQuantityRemaining = 0;
        for(ProductVO productVO : batchVO.product_set){

            // todo: move this to database inserts instead
            // this is assuming quantity is not boxes of product but individual products
            if(productVO.price == null){
                productVO.price = "0"
            }

            if(productVO.sellPrice == null){
                productVO.sellPrice = 0
            }

            if(productVO.quantityremaining == null){
                productVO.quantityremaining = 0
            }


            // calculate indoor quantity
            if(INDOOR_PRODUCT_TYPE_ID == productVO.producttypeid.producttypeid){
                totalIndoorQuantityRemaining = productVO.quantity + totalIndoorQuantityRemaining
                totalIndoorQuantity = productVO.quantityremaining + totalIndoorQuantity
            }


            batchValueTotal = (Integer.valueOf(productVO?.sellPrice) * Integer.valueOf(productVO.quantity)) + batchValueTotal
            batchValueRemainingTotal = (Integer.valueOf(productVO.sellPrice) * productVO.quantityremaining) + batchValueRemainingTotal
        }


        model.addAttribute("totalIndoorQuantity", totalIndoorQuantity)
        model.addAttribute("totalIndoorQuantityRemaining", totalIndoorQuantityRemaining)
        model.addAttribute("batchValueTotal", batchValueTotal)
        model.addAttribute("batchValueRemainingTotal", batchValueRemainingTotal)

    }


    void bindCommonElements(Model model, String customJwtParameter, String batchnumber, String editmode){

        System.out.println("customJwtParam on batch controller: "+customJwtParameter);

        List<BatchVO> results = new ArrayList<BatchVO>();
        if(batchnumber != null){
            System.out.println("Searching data by batchnumber");
            results = batchRepo.findAllByBatchnumber(Integer.valueOf(batchnumber));
        }

        // check to see if there are files uploaded related to this batchnumber
        List<FileVO> filelist = techvvsFileHelper.getFilesByFileNumber(Integer.valueOf(batchnumber), appConstants.UPLOAD_DIR);

        if(filelist.size() > 0){
            model.addAttribute("filelist", filelist);
        } else {
            model.addAttribute("filelist", null);
        }

        if("yes".equals(editmode)){
            model.addAttribute("editmode",editmode)
        } else {
            model.addAttribute("editmode","no")
        }



        model.addAttribute("options", ["5", "10", "20", "100", "1000"]);
        model.addAttribute("menuoptions", ["All","Indoor"]);
        model.addAttribute("customJwtParameter", customJwtParameter);
        model.addAttribute("batch", results.get(0));
        bindBatchTypes(model)
        bindProductTypes(model)
        // once products are bound, add the static data
        calculateStaticPageData(model, results.get(0))
    }


    void bindBatchTypes(Model model){
        // get all the batchtype objects and bind them to select dropdown
        List<BatchTypeVO> batchTypeVOS = batchTypeRepo.findAll();
        model.addAttribute("batchtypes", batchTypeVOS);
    }

    void bindProductTypes(Model model){
        // get all the batchtype objects and bind them to select dropdown
        List<ProductTypeVO> productTypeVOS = productTypeRepo.findAll();
        model.addAttribute("producttypelist", productTypeVOS);
    }

    void bindProducts(Model model, Optional<Integer> page,ProductTypeVO productTypeVO){


        //pagination
        int currentPage = page.orElse(0);
        int pageSize = productTypeVO?.pagesize == null ? 5 : productTypeVO.pagesize;
        Pageable pageable;
        if(currentPage == 0){
            pageable = PageRequest.of(0 , pageSize);
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize);
        }

        Page<ProductVO> pageOfProduct = productRepo.findAll(pageable);

        int totalPages = pageOfProduct.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfProduct.getTotalPages());
        model.addAttribute("productPage", pageOfProduct);
    }

    void bindFilterProductsLikeSearch(Model model, Optional<Integer> page, String name, ProductTypeVO productTypeVO){


        //pagination
        int currentPage = page.orElse(0);
        int pageSize = productTypeVO?.pagesize == null ? 5 : productTypeVO.pagesize;
        Pageable pageable;
        if(currentPage == 0){
            pageable = PageRequest.of(0 , pageSize);
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize);
        }

        Page<ProductVO> pageOfProduct = productRepo.findAllByNameContaining(name, pageable);


        int totalPages = pageOfProduct.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfProduct.getTotalPages());
        model.addAttribute("productPage", pageOfProduct);
    }


    void bindFilterProducts(Model model, Optional<Integer> page, ProductTypeVO productTypeVO){


        //pagination
        int currentPage = page.orElse(0);
        int pageSize = productTypeVO?.pagesize == null ? 5 : productTypeVO.pagesize;;
        Pageable pageable;
        if(currentPage == 0){
            pageable = PageRequest.of(0 , pageSize);
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize);
        }

        Page<ProductVO> pageOfProduct = productRepo.findAllByProducttypeid(productTypeVO, pageable);



        int totalPages = pageOfProduct.getTotalPages();

        List<Integer> pageNumbers = new ArrayList<>();

        while(totalPages > 0){
            pageNumbers.add(totalPages);
            totalPages = totalPages - 1;
        }

        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageOfProduct.getTotalPages());
        model.addAttribute("productPage", pageOfProduct);
    }

}
