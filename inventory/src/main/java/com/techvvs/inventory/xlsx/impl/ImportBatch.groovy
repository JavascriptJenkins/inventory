package com.techvvs.inventory.xlsx.impl

import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.BatchTypeRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchTypeVO
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.viewcontroller.helper.BatchControllerHelper
import com.techvvs.inventory.viewcontroller.helper.ProductHelper
import com.techvvs.inventory.xlsx.helper.ImportHelper
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import picocli.CommandLine

import java.security.SecureRandom
import java.time.LocalDateTime

@Component
class ImportBatch {


    @Autowired
    ProductTypeRepo productTypeRepo;

    @Autowired
    ProductRepo productRepo;

    @Autowired
    BatchRepo batchRepo;

    @Autowired
    ImportHelper importHelper

    @Autowired
    ProductHelper productHelper


    SecureRandom secureRandom = new SecureRandom();

    BatchVO batchvotoreturn = new BatchVO()


    String IMPORT_DIR_XLSX = "./uploads/xlsx/";

    BatchVO importBatchFromExistingXlsxFile(String filename){

        String excelFilePath = IMPORT_DIR_XLSX+filename;

        try {


            InputStream inputStream = new FileInputStream(excelFilePath);
            Workbook workbook = new XSSFWorkbook(inputStream)

            // create a batch record in the database
            BatchVO batchVO = importHelper.createBatchRecord(filename);

            // Get the first sheet
            Sheet sheet = workbook.getSheetAt(0);

            // Iterate through rows
            for (Row row : sheet) {

                // skip the first row because it contains the column names
                if(row.getRowNum() == 0
                ){
                    continue;
                }


                // now take each row and create a product entry
                ProductVO productVO = new ProductVO()

                // set quantity and quantity remaining on the product record in db
                productVO.setQuantity((int) row.getCell(0).getNumericCellValue());
                productVO.setQuantityremaining((int) row.getCell(0).getNumericCellValue());

                // set name on the product record in db
                productVO.setName(row.getCell(1).getStringCellValue());

                // set price on the product record in db
                Double price = Double.valueOf(row.getCell(2).getNumericCellValue());
                productVO.setPrice(price)

                // only set barcode if the value is not empty
                productVO.barcode = row.getCell(3)?.getStringCellValue()?.trim() ? "" : productVO.barcode

                // set the cost from row 4
                Double cost = row.getCell(4) != null && row.getCell(4).getCellType() == CellType.NUMERIC ? Double.valueOf(row.getCell(4).getNumericCellValue()) : null
                productVO.cost = cost ?: 0 // if cost is null, set it to 0, otherwise take the value from above

                productVO.setProductnumber(Integer.valueOf(productHelper.generateProductNumber())); // we are doing a check here to make sure productnumber is unique

                // todo: reference this in a constants class
                Optional<ProductTypeVO> productTypeVO = productTypeRepo.findByName("INDOOR.UNIT");

                productVO.setCreateTimeStamp(LocalDateTime.now());
                productVO.setUpdateTimeStamp(LocalDateTime.now());

                productVO.setBatch(batchVO);

                // add the product to the database
                ProductVO result = importHelper.saveNewProduct(productVO);

                result.setProducttypeid(productTypeVO.get());
                result = productRepo.save(result);


                if(batchVO.getProduct_set() != null){
                    batchVO.getProduct_set().add(productVO);
                } else {
                    batchVO.setProduct_set(new HashSet<>());
                    batchVO.getProduct_set().add(productVO);
                }


                // add the product to the batch
                batchVO = importHelper.addProductToBatch(batchVO,result);


                batchvotoreturn = batchRepo.save(batchVO);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return batchvotoreturn

    }






}
