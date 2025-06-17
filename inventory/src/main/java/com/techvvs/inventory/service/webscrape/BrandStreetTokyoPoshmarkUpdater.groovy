package com.techvvs.inventory.service.webscrape

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.jsoup.Jsoup
import org.springframework.stereotype.Service

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Service
class BrandStreetTokyoPoshmarkUpdater {

    static final double MIN_PRICE_SPREAD = 50
    static final double MIN_AVG_SOLD_PRICE = 100
    static final double MIN_MAX_SOLD_SPREAD = 50
    static final int MAX_DAYS_LISTED = 30
    static final int MAX_LISTING_COUNT = 3

    void updateExcelWithPoshmarkStatus() {
        Path filePath = Paths.get("webscrape", "/brandstreettokyo/coach_bags_sorted_2.xlsx")
        File file = filePath.toFile()

        FileInputStream fis = new FileInputStream(file)
        Workbook workbook = new XSSFWorkbook(fis)
        Sheet sheet = workbook.getSheetAt(0)

        def headerRow = sheet.getRow(0)
        int statusCol = headerRow.lastCellNum
        int daysListedCol = statusCol + 1
        int listingCountCol = statusCol + 2
        int poshmarkPriceCol = statusCol + 3
        int spreadCol = statusCol + 4
        int avgSoldCol = statusCol + 5
        int maxSoldCol = statusCol + 6

        headerRow.createCell(statusCol).setCellValue("Poshmark Status")
        headerRow.createCell(daysListedCol).setCellValue("Days Listed")
        headerRow.createCell(listingCountCol).setCellValue("Listing Count")
        headerRow.createCell(poshmarkPriceCol).setCellValue("Poshmark Price")
        headerRow.createCell(spreadCol).setCellValue("Price Spread")
        headerRow.createCell(avgSoldCol).setCellValue("Average Sold Price")
        headerRow.createCell(maxSoldCol).setCellValue("Max Sold Price")

        PoshmarkLookupService poshmarkLookupService = new PoshmarkLookupService()

        List<Row> recommendedRows = []

        for (int i = 1; i <= sheet.lastRowNum; i++) {
            Row row = sheet.getRow(i)
            if (row == null) continue

            Cell urlCell = row.getCell(3)
            Cell priceCell = row.getCell(2)
            if (urlCell == null || urlCell.stringCellValue.trim().isEmpty()) continue

            String productUrl = urlCell.stringCellValue
            String modelNumber = extractModelNumber(productUrl)
            if (!modelNumber) continue

            def poshmarkData = poshmarkLookupService.lookupModelDetailsWithPriceAndCount(modelNumber)

            row.createCell(statusCol).setCellValue(poshmarkData.status)
            if (poshmarkData.daysListed != null) {
                row.createCell(daysListedCol).setCellValue(poshmarkData.daysListed)
            }
            if (poshmarkData.listingCount != null) {
                row.createCell(listingCountCol).setCellValue(poshmarkData.listingCount)
            }
            if (poshmarkData.price != null) {
                row.createCell(poshmarkPriceCol).setCellValue(poshmarkData.price)
            }
            if (poshmarkData.avgSold != null) {
                row.createCell(avgSoldCol).setCellValue(poshmarkData.avgSold)
            }
            if (poshmarkData.maxSold != null) {
                row.createCell(maxSoldCol).setCellValue(poshmarkData.maxSold)
            }

            if (priceCell != null) {
                try {
                    double originalPrice = priceCell.getCellType() == CellType.NUMERIC
                            ? priceCell.getNumericCellValue()
                            : Double.parseDouble(priceCell.getStringCellValue().replaceAll('[$,]', "").trim())

                    Double poshPrice = poshmarkData.price
                    if (poshPrice != null) {
                        def spread = originalPrice - poshPrice
                        row.createCell(spreadCol).setCellValue(spread)

                        if (
                                spread >= MIN_PRICE_SPREAD &&
                                        (poshmarkData.avgSold != null && poshmarkData.avgSold >= MIN_AVG_SOLD_PRICE) &&
                                        (poshmarkData.daysListed != null && poshmarkData.daysListed <= MAX_DAYS_LISTED) &&
                                        (poshmarkData.listingCount != null && poshmarkData.listingCount <= MAX_LISTING_COUNT) &&
                                        (poshmarkData.maxSold != null && (poshmarkData.maxSold - poshPrice) >= MIN_MAX_SOLD_SPREAD)
                        ) {
                            recommendedRows.add(row)
                        }
                    }
                } catch (Exception e) {
                    println("Error parsing price on row ${i}: ${e.message}")
                }
            }

            println("Checked model ${modelNumber}: Status=${poshmarkData.status}, Listings=${poshmarkData.listingCount}, Price=${poshmarkData.price}")
            Thread.sleep(1000)
        }

        fis.close()
        FileOutputStream fos = new FileOutputStream(file)
        workbook.write(fos)
        fos.close()
        workbook.close()

        println("Updated original Excel file with Poshmark data.")

        if (!recommendedRows.isEmpty()) {
            def recPath = Paths.get("webscrape", "recommended", "recommended_items_${LocalDate.now()}.xlsx")
            Files.createDirectories(recPath.getParent())

            def recWorkbook = new XSSFWorkbook()
            def recSheet = recWorkbook.createSheet("Recommended")

            def newHeader = recSheet.createRow(0)
            for (int j = 0; j < headerRow.lastCellNum; j++) {
                def sourceCell = headerRow.getCell(j)
                newHeader.createCell(j).setCellValue(sourceCell?.stringCellValue ?: "")
            }

            recommendedRows.eachWithIndex { Row r, int idx ->
                def newRow = recSheet.createRow(idx + 1)
                for (int k = 0; k < r.lastCellNum; k++) {
                    def cell = r.getCell(k)
                    if (cell != null) {
                        switch (cell.cellType) {
                            case CellType.STRING:
                                newRow.createCell(k).setCellValue(cell.stringCellValue)
                                break
                            case CellType.NUMERIC:
                                newRow.createCell(k).setCellValue(cell.numericCellValue)
                                break
                            default:
                                newRow.createCell(k).setCellValue(cell.toString())
                        }
                    }
                }
            }

            recPath.toFile().withOutputStream { os -> recWorkbook.write(os) }
            recWorkbook.close()

            println("Saved recommended items to: ${recPath}")
        }
    }

    String extractModelNumber(String url) {
        def matcher = url =~ /-([a-zA-Z0-9]+)$/
        return matcher.find() ? matcher.group(1).toLowerCase() : null
    }
}
