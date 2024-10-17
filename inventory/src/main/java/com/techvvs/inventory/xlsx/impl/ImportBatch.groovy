package com.techvvs.inventory.xlsx.impl

import com.techvvs.inventory.jparepo.BatchRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.ProductTypeRepo
import com.techvvs.inventory.model.BatchVO
import com.techvvs.inventory.model.ProductTypeVO
import com.techvvs.inventory.model.ProductVO
import com.techvvs.inventory.viewcontroller.helper.ProductHelper
import com.techvvs.inventory.xlsx.helper.ImportHelper
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.apache.poi.ss.usermodel.Cell;

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
                productVO.setQuantity((int) row?.getCell(0)?.getNumericCellValue());
                productVO.setQuantityremaining((int) row?.getCell(0)?.getNumericCellValue());
                productVO.setVendorquantity((int) row?.getCell(0)?.getNumericCellValue());

                // set name on the product record in db
                productVO.setName(row.getCell(1).getStringCellValue().trim());

                // set price on the product record in db
                Double price = Double.valueOf(row?.getCell(2)?.getNumericCellValue());
                productVO.setPrice(price)

                // only set barcode if the value is not empty
                // also, we need to check if length is 11.  If it is, it's because spreadsheets
                // don't allow leading 0's unless it's explicitly a string type cell
                // todo: this code doesn't work the way i want it to.... debug this at some point
                if(11 == row?.getCell(3)?.getStringCellValue()?.trim()?.length()){
                    productVO.barcode = row?.getCell(3)?.getStringCellValue()?.trim() ? "" : "0"+productVO.barcode
                } else {
                    productVO.barcode = row?.getCell(3)?.getStringCellValue()?.trim() ? "" : productVO.barcode
                }

                // set the cost from row 4
                Double cost = row.getCell(4) != null && row.getCell(4).getCellType() == CellType.NUMERIC ? Double.valueOf(row?.getCell(4)?.getNumericCellValue()) : null
                productVO.cost = cost ?: 0 // if cost is null, set it to 0, otherwise take the value from above

                // set the value of vendor
                productVO.setVendor(row.getCell(5).getStringCellValue().trim());
                productVO.setBagcolor(row.getCell(6).getStringCellValue().trim());

                productVO.setProductnumber(Integer.valueOf(productHelper.generateProductNumber())); // we are doing a check here to make sure productnumber is unique

                String producttype = row.getCell(7).getStringCellValue().trim().toUpperCase()

                productVO.setCrateposition(row.getCell(8).getStringCellValue().trim());
                productVO.setCrate((int) row.getCell(9).getNumericCellValue()); // crate #

                // todo: reference this in a constants class

                boolean exists = productTypeRepo.existsByName(producttype);
                if(!exists){
                    productTypeRepo.save(new ProductTypeVO(
                            name: producttype,
                            description: producttype + " description",
                            updateTimeStamp: LocalDateTime.now(),
                            createTimeStamp: LocalDateTime.now()
                    ))
                }
                Optional<ProductTypeVO> productTypeVO = productTypeRepo.findByName(producttype);

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



    // Method to check if a row contains any empty or null cells
    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell == null || cell.getCellType() == CellType.BLANK) {
                return true; // Cell is empty
            }

            // Check if the cell is a string and empty
            if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty()) {
                return true; // Cell has an empty string
            }
        }
        return false; // No empty cells found
    }




}
