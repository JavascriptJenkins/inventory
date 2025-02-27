package com.techvvs.inventory.service.controllers

import com.itextpdf.text.pdf.Barcode
import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.jparepo.CartRepo
import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.DeliveryRepo
import com.techvvs.inventory.jparepo.MenuRepo
import com.techvvs.inventory.jparepo.PackageRepo
import com.techvvs.inventory.jparepo.PackageTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.model.CrateVO
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.MenuVO
import com.techvvs.inventory.model.PackageVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.validation.StringSecurityValidator
import com.techvvs.inventory.validation.generic.ObjectValidator
import org.apache.commons.math3.stat.descriptive.summary.Product
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.Model

import javax.transaction.Transactional
import java.time.LocalDateTime

@Component
class ProductService {

    @Autowired
    ProductRepo productRepo

    @Autowired
    CartRepo cartRepo

    @Autowired
    MenuRepo menuRepo

    @Autowired
    CustomerRepo customerRepo

    @Autowired
    CartService cartService

    @Autowired
    PackageRepo packageRepo

    @Autowired
    PackageTypeRepo packageTypeRepo

    @Autowired
    DeliveryRepo deliveryRepo

    @Autowired
    ProductTypeRepo productTypeRepo

    @Autowired
    BarcodeHelper barcodeHelper

    @Autowired
    StringSecurityValidator stringSecurityValidator

    @Autowired
    ObjectValidator objectValidator

    @Autowired
    BatchService batchService


    @Transactional
    void saveProductAssociations(TransactionVO transactionVO) {


        for (ProductVO productVO : transactionVO.product_list) {

            // save the product associations
            //Optional<ProductVO> productVO = productRepo.findByBarcode(cartVO.barcode)


            if (!productVO) {

                // update the product cart list association
                if (productVO.transaction_list == null) {
                    productVO.transaction_list = new ArrayList<>()
                }
                productVO.transaction_list.add(transactionVO)

                productVO.updateTimeStamp = LocalDateTime.now()
                ProductVO savedProduct = productRepo.save(productVO)

            }


        }


    }

    ProductVO validateProductOnAdminBatchPage(ProductVO productVO, Model model, boolean iscreate) {

        // first - validate against security issues
        stringSecurityValidator.validateStringValues(productVO, model)

        // second - validate all object fields
        objectValidator.validateForCreateOrEdit(productVO, model, iscreate)

        // third - do any business logic / page specific validation below


        return productVO
    }

    BatchVO createBatchForFirstProductInBatch(String batchname){
        // create a batch record in the database
        return batchService.createBatchRecord(batchname);
    }


    @Transactional
    ProductVO saveProduct(ProductVO productVO){
        productVO.updateTimeStamp = LocalDateTime.now()
        return productRepo.save(productVO)
    }

    @Transactional
    ProductVO findProductByID(ProductVO productVO){
        return productRepo.findById(productVO.product_id).get()
    }

    List<ProductVO> getAllProducts(){
        List<ProductVO> products = productRepo.findAll()
        return products
    }

    @Transactional
    void saveProductCartAssociations(String barcode,
                                     CartVO cartVO,
                                     Model model,
                                     int counter

    ) {
        Optional<ProductVO> productVO = productRepo.findByBarcode(barcode)

        // todo: on second time thru we need to fully hydrate the customer and product_set before saving

        if(!productVO.empty){

            // update the product cart list association
            if(productVO.get().cart_list == null){
                productVO.get().cart_list = new ArrayList<>()
            }
            productVO.get().cart_list.add(cartVO)

            productVO.get().updateTimeStamp = LocalDateTime.now()
            // when product is added to the cart, decrease the quantity remaining.
            productVO.get().quantityremaining = productVO.get().quantityremaining == 0 ? 0 : productVO.get().quantityremaining - 1
            ProductVO savedProduct = productRepo.save(productVO.get())

            if(cartVO.total == null){
                cartVO.total = 0.00
            }

            /* Cart code below */
            cartVO.total += Double.valueOf(productVO.get().price) // add the product price to the total

            // handle quantity here (have to iterate thru all product cert list and update the quantity)

            // if it's the first time adding a product we need to create the set to hold them
            if(cartVO.product_cart_list == null){
                cartVO.product_cart_list = new ArrayList<ProductVO>()
            }

            cartVO = refreshProductCartList(cartVO)

            // now save the cart side of the many to many
            cartVO.product_cart_list.add(savedProduct)
            cartVO.updateTimeStamp = LocalDateTime.now()
            cartVO = cartRepo.save(cartVO)
            model.addAttribute("successMessage","Product: "+productVO.get().name + " added successfully. Quantity: "+counter)
        } else {
            // need to bind the selected customer here otherwise the dropdown wont work
            cartVO.customer = customerRepo.findById(cartVO.customer.customerid).get()
            model.addAttribute("errorMessage","Product not found")
        }


    }

    // because of how we handle quantities on the frontend, this is needed to refresh the list before saving
    @Transactional
    CartVO refreshProductCartList(CartVO cartVO){

        if(cartVO.cartid == 0){
            return cartVO
        }

        cartVO.product_cart_list = cartRepo.findById(cartVO.cartid).get().product_cart_list
        return cartVO

    }


    @Transactional
    void saveProductPackageAssociations(String barcode,
                                        PackageVO packageVO,
                                        Model model,
                                        int counter,
                                        Optional<String> deliveryid

    ) {
        Optional<ProductVO> productVO = productRepo.findByBarcode(barcode)

        // todo: on second time thru we need to fully hydrate the customer and product_set before saving

        if(!productVO.empty){

            // update the product package list association
            if(productVO.get().package_list == null){
                productVO.get().package_list = new ArrayList<>()
            }
            productVO.get().package_list.add(packageVO)

            productVO.get().updateTimeStamp = LocalDateTime.now()
            // when product is added to the cart, decrease the quantity remaining.
            productVO.get().quantityremaining = productVO.get().quantityremaining == 0 ? 0 : productVO.get().quantityremaining - 1
            ProductVO savedProduct = productRepo.save(productVO.get())

            if(packageVO.total == null){
                packageVO.total = 0.00
            }

            /* Cart code below */
            packageVO.total += Double.valueOf(productVO.get().price) // add the product price to the total

            // handle quantity here (have to iterate thru all product cert list and update the quantity)

            // if it's the first time adding a product we need to create the set to hold them
            if(packageVO.product_package_list == null){
                packageVO.product_package_list = new ArrayList<ProductVO>()
            }

            packageVO = refreshProductPackageList(packageVO)
            if(deliveryid.isPresent() && Integer.valueOf(deliveryid.get()) > 0){
                packageVO = refreshProductPackageDeliveryAssociation(packageVO, Integer.valueOf(deliveryid.get()))
            }

            // now save the cart side of the many to many
            packageVO.product_package_list.add(savedProduct)
            packageVO.updateTimeStamp = LocalDateTime.now()
            packageVO = packageRepo.save(packageVO)
            model.addAttribute("successMessage","Package: "+productVO.get().name + " added successfully. Quantity: "+counter)
        } else {
            // need to bind the selected customer here otherwise the dropdown wont work
            packageVO.packagetype = packageTypeRepo.findById(packageVO.packagetype.packagetypeid).get()
            model.addAttribute("errorMessage","Product not found")
        }


    }


    @Transactional
    PackageVO refreshProductPackageList(PackageVO packageVO){

        if(packageVO.packageid == 0){
            return packageVO
        }

        packageVO.product_package_list = packageRepo.findById(packageVO.packageid).get().product_package_list
        return packageVO

    }

    @Transactional
    PackageVO refreshProductPackageDeliveryAssociation(PackageVO packageVO, Integer deliveryid){

        if(packageVO.packageid == 0){
            return packageVO
        }

        packageVO.delivery = deliveryRepo.findByDeliveryid(deliveryid).get()
        return packageVO

    }

    @Transactional
    ProductVO subtractProductQuantity(ProductVO productVO, int quantity){

        ProductVO existingProduct = productRepo.findById(productVO.getProduct_id())

        productVO.quantityremaining = productVO.quantityremaining - quantity
        return productRepo.save(productVO)
    }

    // this will take in the split amount and divide it by 1 and return the label for the split
    String matchSplitAmountWithLabel(int split) {
        // Calculate the amount as a double
        double amount = 1.0 / split;

        // Match on the amount
        switch (String.valueOf(amount)) {
            case "0.5":  // 1/2
                return "HP";
            case "0.25": // 1/4
                return "QP";
            case "0.0625": // 1/16
                return "OUNCE";
            case "0.015625": // 1/64
                return "QUAD";
            case "0.0078125": // 1/128
                return "EIGHTH";
            default:
                return "UNKNOWN"; // Handle cases where the value doesn't match
        }
    }

    // this will take in the split amount and figure out how many times it fits into 1.00
    int calculateSplitMultiplier(int split) {
        // Calculate the amount as a double
        double amount = 1.0 / split;

        // Match on the amount
        switch (String.valueOf(amount)) {
            case "0.5":  // 1/2
                return 2;
            case "0.25": // 1/4
                return 4;
            case "0.0625": // 1/16
                return 16;
            case "0.015625": // 1/64
                return 64;
            case "0.0078125": // 1/128
                return 128;
            default:
                return 0; // Handle cases where the value doesn't match
        }
    }

    // this will take in the split amount and figure out how many times it fits into 1.00
    ProductTypeVO assignProductType(ProductTypeVO existingProductType, String splitlabel) {

        // check if productType exists with the name extension
        Optional<ProductTypeVO> productType = productTypeRepo.findByName(existingProductType.name + " " + splitlabel)

        if(productType.isPresent()){
            return productType.get() // if we already have this productType in DB, return it
        } else {

            ProductTypeVO newProductType = new ProductTypeVO(
                    name: existingProductType.name + " " + splitlabel,
                    description: existingProductType.description,
            )
            newProductType.name = existingProductType.name + " " + splitlabel
            newProductType.description = existingProductType.description + " split label is " + splitlabel

            newProductType.createTimeStamp = LocalDateTime.now()
            newProductType.updateTimeStamp = LocalDateTime.now()

            return productTypeRepo.save(newProductType) // save a new productType in DB and return it
        }


    }


    // NOTE: this method is terrible.  needs to handle barcode creation better if we accidently generate an existing barcode
    // NOTE: I don't know why we are generating barcodes using batchnumbers.  This was a bad idea.
    // NOTE: Going to run with this for now and hope everything is ok.  This is just a temporary implementation...... fuck
    String generateBarcodeForSplitProduct(int batchnumber) {
        Random random = new Random();

        // Generate random 2-digit integers for row, column, and pagenumber
        int row = random.nextInt(90) + 10;       // Random number between 10 and 99
        int column = random.nextInt(90) + 10;    // Random number between 10 and 99
        int pagenumber = random.nextInt(90) + 10; // Random number between 10 and 99

        // Generate the barcode
        String barcode = barcodeHelper.generateBarcodeData(row, column, batchnumber, pagenumber);

        // Check for existing barcode and regenerate if necessary
        if (barcodeExists(barcode)) {
            System.out.println("Found existing barcode when splitting product. This is not good. This should not happen.");

            // Regenerate random values and barcode
            row = random.nextInt(90) + 10;
            column = random.nextInt(90) + 10;
            pagenumber = random.nextInt(90) + 10;
            barcode = barcodeHelper.generateBarcodeData(row, column, batchnumber, pagenumber);
        }

        return barcode;
    }


    boolean barcodeExists(String barcode){
        Optional<ProductVO> existingproduct = productRepo.findByBarcode(barcode)
        return existingproduct.isPresent()
    }


    // The normal b2b checkout uses a barcode scanning ui to identify products.
    // When we send a product to checkout from the Menu, we are using the product_id to identify it
    // We are also saving a menu that is associated with the cart too
    @Transactional
    void saveProductCartMenuAssociationsForMenuShopping(
                                    String product_id,
                                    String menuid,
                                     CartVO cartVO,
                                     Model model,
                                     int counter

    ) {
        Optional<ProductVO> productVO = productRepo.findById(Integer.valueOf(product_id))

        // todo: on second time thru we need to fully hydrate the customer and product_set before saving

        if(!productVO.empty){

            // update the product cart list association
            if(productVO.get().cart_list == null){
                productVO.get().cart_list = new ArrayList<>()
            }
            productVO.get().cart_list.add(cartVO)

            productVO.get().updateTimeStamp = LocalDateTime.now()
            // when product is added to the cart, decrease the quantity remaining.
            productVO.get().quantityremaining = productVO.get().quantityremaining == 0 ? 0 : productVO.get().quantityremaining - 1
            ProductVO savedProduct = productRepo.save(productVO.get())

            if(cartVO.total == null){
                cartVO.total = 0.00
            }

            /* Cart code below */
            cartVO.total += Double.valueOf(productVO.get().price) // add the product price to the total

            // handle quantity here (have to iterate thru all product cert list and update the quantity)

            // if it's the first time adding a product we need to create the set to hold them
            if(cartVO.product_cart_list == null){
                cartVO.product_cart_list = new ArrayList<ProductVO>()
            }

            cartVO = refreshProductCartList(cartVO)

            // now save the cart side of the many to many
            cartVO.product_cart_list.add(savedProduct)
            cartVO.updateTimeStamp = LocalDateTime.now()

            // find the menu from db and save it associated with the cart
            Optional<MenuVO> menuVO = menuRepo.findById(Integer.valueOf(menuid))
            cartVO.menu = menuVO.get()

            cartVO = cartRepo.save(cartVO)


            // find the menu from db and save it associated with the cart
            // this could maybe be done in the above save but we
//            Optional<MenuVO> menuVO = menuRepo.findById(Integer.valueOf(menuid))
//            cartVO.menu = menuVO.get()
//            cartVO.updateTimeStamp = LocalDateTime.now()
//            cartVO = cartRepo.save(cartVO)


            model.addAttribute("successMessage","Product: "+productVO.get().name + " added successfully. Quantity: "+counter)
        } else {
            // need to bind the selected customer here otherwise the dropdown wont work
            cartVO.customer = customerRepo.findById(cartVO.customer.customerid).get()
            model.addAttribute("errorMessage","Product not found")
        }


    }


    // add product to cart and then update the cart and product associations
    @Transactional
    CartVO addProductToCart(
            CartVO cartVO,
            int quantityselected,
            Model model,
            String product_id,
            String menuid
    ){

        if(quantityselected == 0){
            saveProductCartMenuAssociationsForMenuShopping(product_id, menuid, cartVO, model, 1)
        } else {
            int j = 0;
            // run the product save once for every quantity selected
            for (int i = 0; i < quantityselected; i++) {
                j++
                saveProductCartMenuAssociationsForMenuShopping(product_id, menuid, cartVO, model, j)
            }
        }


        return cartVO
    }


    @Transactional
    Optional<ProductVO> createProduct(ProductVO productVO) {

        // implement other logic here from the xlsx page
        productVO = productRepo.save(productVO)
        return Optional.of(productVO)
    }



    @Transactional
    boolean addProductsToMenu(Integer menuid, List<Integer> productids) {
        // Retrieve the MenuVO
        MenuVO menuVO = menuRepo.findById(menuid).orElse(null)
        if (menuVO == null) {
            println "Menu not found for ID: $menuid"
            return false
        }

        // Fetch products from the repository
        List<ProductVO> products = productRepo.findAllById(productids)

        // Add products to menu_product_list if they are not already there
        products.each { product ->
            if (!menuVO.menu_product_list.contains(product)) {
                menuVO.menu_product_list.add(product)
            }
        }

        // Save updated MenuVO
        menuRepo.save(menuVO)

        // Add menu to each product's menu_list
        products.each { product ->
            if (!product.menu_list.contains(menuVO)) {
                product.menu_list.add(menuVO)
            }
        }

        // Save updated products
        productRepo.saveAll(products)

        return true
    }

    @Transactional
    boolean removeProductsToMenu(Integer menuid, List<Integer> productids) {
        // Retrieve the MenuVO
        MenuVO menuVO = menuRepo.findById(menuid).orElse(null)
        if (menuVO == null) {
            println "Menu not found for ID: $menuid"
            return false
        }

        // Filter out products from menu_product_list
        menuVO.menu_product_list.removeIf { ProductVO product -> productids.contains(product.product_id) }

        // Save updated MenuVO
        menuRepo.save(menuVO)

        // Process each product
        productids.each { Integer productId ->
            ProductVO productVO = productRepo.findById(productId).orElse(null)
            if (productVO != null) {
                // Remove the menu from the product's menu_list
                productVO.menu_list.removeIf { MenuVO menu -> menu.menuid == menuid }

                // Save updated ProductVO
                productRepo.save(productVO)
            } else {
                println "Product not found for ID: $productId"
            }
        }

        return true
    }




}
