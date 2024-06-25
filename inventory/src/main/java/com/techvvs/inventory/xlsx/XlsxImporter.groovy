package com.techvvs.inventory.xlsx

import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchTypeVO
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.security.SecureRandom
import java.time.LocalDateTime

// Implementation code for importing XLSX files
@Component
class XlsxImporter {


    @Autowired
    BatchTypeRepo batchTypeRepo;

    @Autowired
    ProductTypeRepo productTypeRepo;

    @Autowired
    ProductRepo productRepo;

    @Autowired
    BatchRepo batchRepo;

    SecureRandom secureRandom = new SecureRandom();


    String UPLOAD_DIR = "./uploads/menus/";
    String IMPORT_DIR = "./uploads/import/";


    void testImportXLSXFile(){

        String excelFilePath = IMPORT_DIR+"export-1"+".xlsx";

        try {


            InputStream inputStream = new FileInputStream(excelFilePath);
            Workbook workbook = new XSSFWorkbook(inputStream)


            // create a batch record in the database
            BatchVO batchVO = createBatchRecord(excelFilePath);

            // Get the first sheet
            Sheet sheet = workbook.getSheetAt(0);

            // Iterate through rows
            for (Row row : sheet) {

                // skip the first 2 rows because they contain metadata
                if(row.getRowNum() == 0
                        || row.getRowNum() == 1
                ){
                    continue;
                }

                // now take each row and create a product entry
                ProductVO productVO = new ProductVO();
                productVO.setQuantity((int) row.getCell(0).getNumericCellValue());
                productVO.setQuantityremaining((int) row.getCell(0).getNumericCellValue());
                productVO.setName(row.getCell(1).getStringCellValue());
                String price = String.valueOf(row.getCell(2).getNumericCellValue());
                productVO.setPrice(price.substring(0, price.length() - 2));
                productVO.setCost(Integer.valueOf(productVO.price) - 400) // todo: replace this with actual cost data
                productVO.setProductnumber(secureRandom.nextInt(10000000)); // assuming this will be unique ...

                Optional<ProductTypeVO> productTypeVO = productTypeRepo.findById(2);

                productVO.setCreateTimeStamp(LocalDateTime.now());
                productVO.setUpdateTimeStamp(LocalDateTime.now());

                productVO.setBatch(batchVO);

                // add the product to the database
                ProductVO result = saveNewProduct(productVO);

                result.setProducttypeid(productTypeVO.get());
                result = productRepo.save(result);

                // add the product to the batch
                batchVO = addProductToBatch(batchVO,result);

                if(batchVO.getProduct_set() != null){
                    batchVO.getProduct_set().add(productVO);
                } else {
                    batchVO.setProduct_set(new HashSet<>());
                    batchVO.getProduct_set().add(productVO);
                }

                batchRepo.save(batchVO);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    ProductVO saveNewProduct(ProductVO productVO) {

        // when creating a new processData entry, set the last attempt visit to now - this may change in future
        productVO.setCreateTimeStamp(LocalDateTime.now());
        productVO.setUpdateTimeStamp(LocalDateTime.now());

        //todo: add support for product types on the ui so we can save this product object
        ProductVO result = productRepo.save(productVO);

        return result;
    }


    BatchVO createBatchRecord(String nameOfBatch){


        Optional<BatchTypeVO> batchTypeVO = batchTypeRepo.findById(1);

        // set the batch data
        BatchVO batchVO = new BatchVO();
        batchVO.setName(nameOfBatch);
        batchVO.setDescription("default batch description"); // insert date imported into here?
        batchVO.setBatchid(7);
        batchVO.setBatchnumber(secureRandom.nextInt(10000000)); // assuming this will be unique ...
        batchVO.setBatch_type_id(batchTypeVO.get()); // assume we always have the ref data loaded

        // when creating a new processData entry, set the last attempt visit to now - this may change in future
        batchVO.setCreateTimeStamp(LocalDateTime.now());
        batchVO.setUpdateTimeStamp(LocalDateTime.now());


        // first save the batch then we will add products to it and save it again
        BatchVO result = batchRepo.save(batchVO); //

        return result;

    }


    BatchVO addProductToBatch(BatchVO batchVO, ProductVO result){
        batchVO = batchRepo.findByBatchid(batchVO.getBatchid());
        batchVO.setUpdateTimeStamp(LocalDateTime.now());
        // this means a valid batch was found
        batchVO.getProduct_set().add(result); // add the product from database to the product set
        batchVO = batchRepo.save(batchVO);
        return batchVO;


    }









}
