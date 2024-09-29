package com.techvvs.inventory.barcode.service

import com.techvvs.inventory.barcode.impl.BarcodeGenerator
import com.techvvs.inventory.barcode.impl.BarcodeHelper
import com.techvvs.inventory.constants.AppConstants
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.CrateVO
import com.techvvs.inventory.model.DeliveryVO
import com.techvvs.inventory.model.PackageVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.viewcontroller.helper.ProductHelper
import org.apache.pdfbox.pdmodel.PDDocument
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.nio.file.Files
import java.nio.file.Paths


//UPCA format
//0: Number system character
//12345: Manufacturer code
//67890: Product code
//5: Check digit
//https://www.gs1us.org/upcs-barcodes-prefixes/how-to-get-a-upc-barcode
@Component
class BarcodeService {

    @Autowired
    BarcodeGenerator barcodeGenerator;

    @Autowired
    BarcodeHelper barcodeHelper

    @Autowired
    AppConstants appConstants

    @Autowired
    ProductHelper productHelper

    /* This method will create a single barcode for each product.
    *  If you want to make barcodes for every single product and multiples for the amount of products you have, use the other method
    *  */
//    void createSingleMenuBarcodesForBatch(BatchVO batchVO) {
//
//        // NOTE: right now this is going to generate barcodes for every product in batch regardless of product type
//       try {
//
//           LinkedHashSet linkedHashSet = barcodeHelper.convertToLinkedHashSet(batchVO.product_set)
//           List<Set<ProductVO>> result = barcodeHelper.removeItemsInChunksOf50(linkedHashSet);
//
//           System.out.println("result of rounding up: " + result);
//
//            for(int i = 0; i < result.size(); i++) {
//                barcodeGenerator.generateBarcodes(batchVO.name, batchVO.batchnumber, i, result.get(i));
//            }
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//    }



    void createBarcodeSheetForSingleDeliveryUPCA(DeliveryVO deliveryVO) {

        // NOTE: right now this is going to generate barcodes for every product in batch regardless of product type
        try {

            // run this 50 times to make a barcode sheet
            PDDocument document = new PDDocument()

            barcodeGenerator.generateBarcodeSheetForBarcodeUPCA(
                    deliveryVO.deliveryid,
                    0,
                    deliveryVO.deliverybarcode, // generate a sheet of 50 of same barcode
                    deliveryVO.name,
                    document,
                    "Delivery" // This will show up on the metadata at top of barcode sheet
            )


            saveBarcodeLabelPdfFileForDelivery(document, appConstants.BARCODES_ALL_DIR, deliveryVO.name, deliveryVO.deliveryid)

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void createBarcodeSheetForSinglePackageUPCA(PackageVO packageVO) {

        // NOTE: right now this is going to generate barcodes for every product in batch regardless of product type
        try {

            // run this 50 times to make a barcode sheet
            PDDocument document = new PDDocument()

            barcodeGenerator.generateBarcodeSheetForBarcodeUPCA(
                    packageVO.packageid,
                    0,
                    packageVO.packagebarcode, // generate a sheet of 50 of same barcode
                    packageVO.name,
                    document,
                    "Package" // This will show up on the metadata at top of barcode sheet
            )


            saveBarcodeLabelPdfFileForPackage(document, appConstants.BARCODES_ALL_DIR, packageVO.name, packageVO.packageid)

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void createBarcodeSheetForSingleCrateUPCA(CrateVO crateVO) {

        // NOTE: right now this is going to generate barcodes for every product in batch regardless of product type
        try {

            // run this 50 times to make a barcode sheet
            PDDocument document = new PDDocument()

            barcodeGenerator.generateBarcodeSheetForBarcodeUPCA(
                    crateVO.crateid,
                    0,
                    crateVO.cratebarcode, // generate a sheet of 50 of same barcode
                    crateVO.name,
                    document,
                    "Crate" // This will show up on the metadata at top of barcode sheet
            )


            saveBarcodeLabelPdfFileForCrate(document, appConstants.BARCODES_ALL_DIR, crateVO.name, crateVO.crateid)

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // todo: finish coding this method so we can pass a productSet in and get custom labels printed
    void createAllBarcodesForProductList(Set<ProductVO> productset) {

        // NOTE: right now this is going to generate barcodes for every product in batch regardless of product type
        try {

            List<List<ProductVO>> result = productHelper.sortAndExpandProductSet(productset)

            // create document before we loop over the collections of products so all pdf pages land in a single document
            PDDocument document = new PDDocument()
            for(int i = 0; i < result.size(); i++) {
                barcodeGenerator.generateBarcodesForAllItems(
                        33333333,
                        i,
                        result.get(i),
                        "adhocbenny",
                        document);
            }

            saveBarcodeLabelPdfFileForBatch(document, appConstants.BARCODES_ALL_DIR, "adhocbenny", 33333333)

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void createAllBarcodesForBatch(BatchVO batchVO) {

        // NOTE: right now this is going to generate barcodes for every product in batch regardless of product type
        try {

            List<List<ProductVO>> result = productHelper.sortAndExpandProductSet(batchVO.product_set)

            // create document before we loop over the collections of products so all pdf pages land in a single document
            PDDocument document = new PDDocument()
            for(int i = 0; i < result.size(); i++) {
                barcodeGenerator.generateBarcodesForAllItems(
                        batchVO.batchnumber,
                        i,
                        result.get(i),
                        batchVO.name,
                        document);
            }

            saveBarcodeLabelPdfFileForBatch(document, appConstants.BARCODES_ALL_DIR, batchVO.name, batchVO.batchnumber)

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // this one is saved normally with no batchnumber subdir
    void saveBarcodeLabelPdfFileForEntity(PDDocument document, String entitysubdirectory, String entityname, int entitynumber) {
        // create a directory with the batchnumber and /barcodes dir if it doesn't exist yet
        Files.createDirectories(Paths.get(appConstants.PARENT_LEVEL_DIR+String.valueOf(entitynumber)+entitysubdirectory));

        String filename = entityname+"-"+entitynumber
        // save the actual file after looping thru all products
        document.save(appConstants.PARENT_LEVEL_DIR+entitysubdirectory+appConstants.filenameprefix+filename+".pdf");
        document.close();
    }

    // this one is saving under a batchnumber dir - assuming all calls to this method are saving batch barcodes
    void saveBarcodeLabelPdfFileForBatch(PDDocument document, String entitysubdirectory, String entityname, int entitynumber) {
        // create a directory with the batchnumber and /barcodes dir if it doesn't exist yet
        Files.createDirectories(Paths.get(appConstants.PARENT_LEVEL_DIR+String.valueOf(entitynumber)+entitysubdirectory));

        String filename = entityname+"-"+entitynumber
        // save the actual file after looping thru all products
        document.save(appConstants.PARENT_LEVEL_DIR+entitynumber+entitysubdirectory+appConstants.filenameprefix+filename+".pdf");
        document.close();
    }

    // this one is saving under a batchnumber dir - assuming all calls to this method are saving batch barcodes
    void saveBarcodeLabelPdfFileForPackage(PDDocument document, String entitysubdirectory, String entityname, int entitynumber) {
        // create a directory with the batchnumber and /barcodes dir if it doesn't exist yet
        Files.createDirectories(Paths.get(appConstants.PARENT_LEVEL_DIR+appConstants.PACKAGE_DIR+String.valueOf(entitynumber)+entitysubdirectory));

        String filename = entityname+"-"+entitynumber
        // save the actual file after looping thru all products
        document.save(appConstants.PARENT_LEVEL_DIR+appConstants.PACKAGE_DIR+entitynumber+entitysubdirectory+appConstants.filenameprefix+filename+".pdf");
        document.close();
    }

    void saveBarcodeLabelPdfFileForCrate(PDDocument document, String entitysubdirectory, String entityname, int entitynumber) {
        // create a directory with the batchnumber and /barcodes dir if it doesn't exist yet
        Files.createDirectories(Paths.get(appConstants.PARENT_LEVEL_DIR+appConstants.CRATE_DIR+String.valueOf(entitynumber)+entitysubdirectory));

        String filename = entityname+"-"+entitynumber
        // save the actual file after looping thru all products
        document.save(appConstants.PARENT_LEVEL_DIR+appConstants.CRATE_DIR+entitynumber+entitysubdirectory+appConstants.filenameprefix+filename+".pdf");
        document.close();
    }

    void saveBarcodeLabelPdfFileForDelivery(PDDocument document, String entitysubdirectory, String entityname, int entitynumber) {
        // create a directory with the batchnumber and /barcodes dir if it doesn't exist yet
        Files.createDirectories(Paths.get(appConstants.PARENT_LEVEL_DIR+appConstants.DELIVERY_DIR+String.valueOf(entitynumber)+entitysubdirectory));

        String filename = entityname+"-"+entitynumber
        // save the actual file after looping thru all products
        document.save(appConstants.PARENT_LEVEL_DIR+appConstants.DELIVERY_DIR+entitynumber+entitysubdirectory+appConstants.filenameprefix+filename+".pdf");
        document.close();
    }

    public static List<List<ProductVO>> sortProductListsByName(List<List<ProductVO>> result) {
        for (List<ProductVO> productList : result) {
            Collections.sort(productList, new Comparator<ProductVO>() {
                @Override
                public int compare(ProductVO p1, ProductVO p2) {
                    return p1.getName().compareToIgnoreCase(p2.getName());
                }
            });
        }
        return result;
    }

    public static List<ProductVO> expandAndDuplicateProductQuantities(LinkedHashSet<ProductVO> originalSet) {
        List<ProductVO> expandedList = new ArrayList<>();

        for (ProductVO product : originalSet) {
            for (int i = 0; i < product.getQuantity(); i++) {
                expandedList.add(product) // for each quantity add the product again to the list
            }
        }

        return expandedList;
    }

    static List<ProductVO> expandAndDuplicateProductQuantitiesWithLimit(LinkedHashSet<ProductVO> originalSet, int maxDuplicates) {
        List<ProductVO> expandedList = new ArrayList<>();

        for (ProductVO product : originalSet) {
            int quantityToAdd = Math.min(product.getQuantity(), maxDuplicates); // Limit the number of duplicates
            for (int i = 0; i < quantityToAdd; i++) {
                expandedList.add(product); // Add the product to the list up to the limit
            }
        }

        return expandedList;
    }

    List<ProductVO> sortByPrice(List<ProductVO> productList) {
        return productList.sort { a, b -> a.price <=> b.price }
    }


    public static List<ProductVO> sortProductsByIdAscending(List<ProductVO> products) {
        Collections.sort(products, new Comparator<ProductVO>() {
            @Override
            public int compare(ProductVO p1, ProductVO p2) {
                return Integer.compare(p1.getProduct_id(), p2.getProduct_id());
            }
        });
        return products;
    }

    public static List<List<ProductVO>> splitIntoChunksOf50(List<ProductVO> originalList) {
        List<List<ProductVO>> result = new ArrayList<>();

        int listSize = originalList.size();
        int chunkSize = 50;

        for (int i = 0; i < listSize; i += chunkSize) {
            // Create sublist from the original list, making sure not to exceed the list size
            List<ProductVO> chunk = originalList.subList(i, Math.min(i + chunkSize, listSize));
            // Add the chunk to the result
            result.add(new ArrayList<>(chunk));  // Create a new list to avoid referencing the original sublist
        }

        return result;
    }

    static List<ProductVO> sortProductsByIdDescending(List<ProductVO> products) {
        products.sort { a, b -> b.product_id <=> a.product_id }
        return products
    }

    static List<List> reverseOrder(List<List> listOfLists) {
        listOfLists.reverse()
    }


}
