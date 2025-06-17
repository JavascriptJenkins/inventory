package com.techvvs.inventory.service.webscrape

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.stereotype.Service

import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class PoshmarkLookupService {

    String searchUrlTemplate = "https://poshmark.com/search?query="

    boolean isModelSoldOnPoshmark(String modelNumber) {
        try {
            String queryUrl = "${searchUrlTemplate}${modelNumber}"
            Document doc = Jsoup.connect(queryUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(10000)
                    .get()

            List<Element> listings = doc.select("div.tile")

            if (listings.isEmpty()) return false

            for (Element listing : listings) {
                String text = listing.text().toLowerCase()
                if (text.contains(modelNumber.toLowerCase()) && text.contains("sold")) {
                    return true
                }
            }

        } catch (Exception e) {
            System.err.println("Error checking Poshmark for model ${modelNumber}: ${e.message}")
        }

        return false
    }

    Map lookupModelDetails(String modelNumber) {
        try {
            String queryUrl = "https://poshmark.com/search?query=${modelNumber}"
            def doc = Jsoup.connect(queryUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get()

            def listings = doc.select("div.card.card--small")

            for (def listing : listings) {
                def title = listing.selectFirst("a.tile__title")?.text()?.toLowerCase()
                if (!title?.contains(modelNumber.toLowerCase())) continue

                def status = listing.text().toLowerCase().contains("sold") ? "Sold" : "Listed"

                def image = listing.selectFirst("img")
                def imageUrl = image?.attr("src") ?: ""
                def dateFromUrl = extractDateFromImageUrl(imageUrl)

                Long daysListed = null
                if (dateFromUrl != null) {
                    def listedDate = LocalDate.parse(dateFromUrl)
                    daysListed = ChronoUnit.DAYS.between(listedDate, LocalDate.now())
                }

                return [status: status, daysListed: daysListed]
            }

        } catch (Exception e) {
            println "Error checking Poshmark: ${e.message}"
        }

        return [status: "Not Found", daysListed: null]
    }

    Map lookupModelDetailsWithPriceAndCount(String modelNumber) {
        try {
            String queryUrl = "https://poshmark.com/search?query=${modelNumber}"
            def doc = Jsoup.connect(queryUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get()

            def listings = doc.select("div.card.card--small")
            int count = 0
            Double firstPrice = null
            String status = "Not Found"
            Long daysListed = null
            List<Double> soldPrices = []

            for (def listing : listings) {
                def title = listing.selectFirst("a.tile__title")?.text()?.toLowerCase()
                if (!title?.contains(modelNumber.toLowerCase())) continue

                count++
                def sold = listing.text().toLowerCase().contains("sold")
                def priceText = listing.selectFirst("span.fw--bold")?.text()?.replace('$', "")
                if (priceText) {
                    try {
                        def parsedPrice = Double.parseDouble(priceText)
                        if (sold) soldPrices << parsedPrice
                        if (count == 1) firstPrice = parsedPrice
                    } catch (Exception ignored) {}
                }

                if (count == 1) {
                    status = sold ? "Sold" : "Listed"

                    def image = listing.selectFirst("img")
                    def imageUrl = image?.attr("src") ?: ""
                    def dateFromUrl = extractDateFromImageUrl(imageUrl)

                    if (dateFromUrl != null) {
                        def listedDate = LocalDate.parse(dateFromUrl)
                        daysListed = ChronoUnit.DAYS.between(listedDate, LocalDate.now())
                    }
                }
            }

            Double avgSold = soldPrices.isEmpty() ? null : soldPrices.sum() / soldPrices.size()
            Double maxSold = soldPrices.isEmpty() ? null : soldPrices.max()

            return [
                    status: status,
                    daysListed: daysListed,
                    listingCount: count,
                    price: firstPrice,
                    soldPrices: soldPrices,
                    avgSold: avgSold,
                    maxSold: maxSold
            ]

        } catch (Exception e) {
            println "Error checking Poshmark for ${modelNumber}: ${e.message}"
        }

        return [status: "Not Found", daysListed: null, listingCount: 0, price: null, soldPrices: [], avgSold: null, maxSold: null]
    }

    private String extractDateFromImageUrl(String url) {
        def matcher = url =~ /posts\/(\d{4})\/(\d{2})\/(\d{2})\//
        if (matcher.find()) {
            return "${matcher.group(1)}-${matcher.group(2)}-${matcher.group(3)}"
        }
        return null
    }
}
