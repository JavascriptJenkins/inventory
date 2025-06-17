package com.techvvs.inventory.service.webscrape

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.jsoup.Jsoup
import org.springframework.stereotype.Service


@Service
class BrandStreetTokyoScraperService {

    String baseUrl = 'https://brandstreettokyo.com/collections/newest-products'
    int maxPages = 500
    String outputPath = "webscrape/brandstreettokyo/brandstreettokyo_products.xlsx"

    void scrapeAllProductsToExcel() {
        System.out.println("Starting BrandStreetTokyo scrape...")

        def workbook = new XSSFWorkbook()
        def sheet = workbook.createSheet("Products")

        def header = ["Page", "Product Name", "Price", "Product URL"]
        def headerRow = sheet.createRow(0)
        header.eachWithIndex { val, idx ->
            headerRow.createCell(idx).setCellValue(val)
        }

        def rowIndex = 1

        (1..maxPages).each { page ->
            def url = "${baseUrl}?page=${page}"
            System.out.println("Fetching page ${page}...")

            try {
                def doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .header("Accept", "text/html")
                        .header("Accept-Language", "en-US,en;q=0.9")
                        .timeout(10000)
                        .get()


//                def products = doc.select("div.grid-product")
//                def products = doc.select("div.productgrid--item")
                def products = doc.select("div.product-details")



                if (products.isEmpty()) {
                    System.out.println("No products found on page ${page}, stopping early.")
                    return
                }

                products.each { product ->
                    def titleAnchor = product.selectFirst("div.product-title a")
                    def priceElement = product.selectFirst("div.product-price h6.no-pad")

                    def name = titleAnchor?.text()?.trim() ?: "N/A"
                    def link = "https://brandstreettokyo.com" + (titleAnchor?.attr("href")?.trim() ?: "")
                    def price = priceElement?.text()?.trim() ?: "N/A"

                    def row = sheet.createRow(rowIndex++)
                    row.createCell(0).setCellValue(page)
                    row.createCell(1).setCellValue(name)
                    row.createCell(2).setCellValue(price)
                    row.createCell(3).setCellValue(link)
                }

                Thread.sleep(500)

            } catch (Exception e) {
                System.out.println("Error on page ${page}: ${e.message}", e)
            }
        }

        (0..3).each { sheet.autoSizeColumn(it) }

        def outFile = new File(outputPath)
        outFile.withOutputStream { os -> workbook.write(os) }
        workbook.close()

        System.out.println("Finished writing to ${outputPath}")
    }
}
