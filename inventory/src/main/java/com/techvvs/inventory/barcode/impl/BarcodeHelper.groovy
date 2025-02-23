package com.techvvs.inventory.barcode.impl

import com.techvvs.inventory.barcode.service.BarcodeService
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.model.ProductVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.transaction.Transactional


@Component
class BarcodeHelper {

    @Autowired
    BarcodeService barcodeService

    @Autowired
    ProductRepo productRepo

    static <T> List<Set<T>> removeItemsInChunksOf50(LinkedHashSet<T> originalSet) {
        List<Set<T>> chunks = new ArrayList<>();
        Iterator<T> iterator = originalSet.iterator();

        while (iterator.hasNext()) {
            Set<T> chunk = new LinkedHashSet<>();
            int count = 0;
            while (iterator.hasNext() && count < 50) {
                T item = iterator.next();
                chunk.add(item);
                iterator.remove();  // Remove the item from the original set
                count++;
            }
            chunks.add(chunk);
        }

        return chunks;
    }


    // todo: this can handle up to 950 products in a single batch.  Need to write logic to handle more than 950
    // passing the batchnumber into here for first part of upc barcode
//    static String generateBarcodeData(int row, int col, int batchnumber, int pagenumber) {
//
//        // Example method to generate unique barcode data based on row and column
//        // note: this baseData can only be 6 characters long - batchnumbers are 7 characters so we are removing the last char
//        String baseData = removeLast2Character(String.valueOf(batchnumber))+String.valueOf(pagenumber); // Base data for the barcode
//        String rowColData = String.format("%02d%02d", row, col); // Row and column indices padded with leading zeros
//
//        // Combine base data with row and column data
//        String barcodeData = baseData + rowColData;
//
//        // Calculate and append the checksum
//        int checksum = calculateUPCAChecksum(barcodeData);
//        barcodeData += checksum;
//
//        return barcodeData;
//    }

    static String generateBarcodeData(int row, int col, int batchnumber, int pagenumber) {

        int length = pagenumber.toString().length()

        // Create base data from batchnumber and pagenumber, ensuring it's exactly 6 characters
        String baseData = removeLast2Character(String.valueOf(batchnumber),String.valueOf(pagenumber),length);

//        // Ensure the baseData is exactly 6 characters long
//        baseData = baseData.length() > 6 ? baseData.substring(0, 6) : baseData.padLeft(6, '0');

        // If baseData is longer than 6, take the rightmost 6 characters. Otherwise, pad with random digits.
        baseData = baseData.length() > 6 ?
                baseData.substring(baseData.length() - 6) :
                generateRandomPadding(6 - baseData.length()) + baseData


        // Create row and column data, ensuring it's exactly 4 characters
        String rowColData = String.format("%02d%02d", row, col); // Row and column indices padded with leading zeros

        // Combine base data with row and column data
        String barcodeData = baseData + rowColData;

        // Ensure the barcodeData is exactly 11 characters
        barcodeData = barcodeData.length() > 11 ? barcodeData.substring(0, 11) : barcodeData.padLeft(11, '0');

        // Calculate and append the checksum
        int checksum = calculateUPCAChecksum(barcodeData);
        barcodeData += checksum;

        return barcodeData;
    }

    // todo: add a check here to make sure barcodes are unique before adding....
    @Transactional
    ProductVO addBarcodeToProduct(ProductVO productVO, String barcodedata){

        Optional<ProductVO> existingproduct = productRepo.findById(productVO.getProduct_id())

        // if we have an existing barcode do NOT overwrite it.
        if(existingproduct.present && existingproduct.get().barcode != null && existingproduct.get().barcode.length() > 0){
            // do nothing
            return productVO;
        } else {

            Optional<ProductVO> matchingBarcodeProduct = productRepo.findByBarcode(barcodedata)

            if(matchingBarcodeProduct.present && matchingBarcodeProduct.get().product_id != productVO.product_id){
                System.out.println("Found matching product with the same barcode.  This should not happen.  Returning early. ")
                return productVO
            }

            // probably don't need to do this....
            if(barcodedata.length() == 11){
                barcodedata = barcodedata.padLeft(12,'0')
            }

            productVO.setBarcode(barcodedata)
            return productRepo.save(productVO)
        }

    }



    Random random = new Random()

    // Generate a random number string for padding
    def generateRandomPadding(int length) {
        (1..length).collect { random.nextInt(10) }.join() // Generates random digits as string
    }

    // Method to calculate the checksum for UPC-A barcode data
    static int calculateUPCAChecksum(String data) {
        int sum = 0;
        for (int i = 0; i < data.length(); i++) {
            int digit = Character.getNumericValue(data.charAt(i));
            if (i % 2 == 0) {  // Odd positions (0-indexed) get multiplied by 3
                sum += digit * 3;
            } else {  // Even positions get added as-is
                sum += digit;
            }
        }
        int mod = sum % 10;
        return mod == 0 ? 0 : 10 - mod;  // Return the difference from the next multiple of 10
    }


    static String removeLast2Character(String batchnum, String pagenumber, int length) {
        if (batchnum == null || batchnum.isEmpty()) {
            return batchnum; // Return the original string if it's null or empty.
        }

        // substring the batchnum
        batchnum = batchnum.substring(0, batchnum.length() - length)

        return batchnum+pagenumber; // combine the 2 numbers
    }

    static String replaceDigitsWithRandom(String numberstring) {

        Random random = new Random()

        // Convert the string to an integer (though not strictly necessary)
        int originalNumber = numberstring.toInteger()

        // Create a new number by replacing each digit with a random digit
        def randomDigits = numberstring.collect {
            random.nextInt(10) // Generate a random digit (0 to 9)
        }.join()

        // Convert the resulting string of random digits to an integer
        int newRandomNumber = randomDigits.toInteger()

//        println "Original number: $originalNumber"
//        println "New random number: $newRandomNumber"

        return String.valueOf(newRandomNumber)
    }


    static <T> LinkedHashSet<T> convertToLinkedHashSet(Set<T> originalSet) {
        return new LinkedHashSet<>(originalSet);
    }

    /**
     * Ensures that the barcodeData is exactly 11 characters long.
     * If barcodeData is shorter than 11 characters, it will be left-padded with zeros.
     * If barcodeData is longer than 11 characters, it will be truncated to the first 11 characters.
     *
     * @param barcodeData The input string that represents the barcode data.
     * @return A string that is exactly 11 characters long.
     */
    static String ensureBarcodeDataLength(String barcodeData) {
        if (barcodeData.length() > 11) {
            return barcodeData.substring(0, 11)
        } else if (barcodeData.length() < 11) {
            return barcodeData.padLeft(11, '0')
        } else {
            return barcodeData
        }
    }

    static <T> LinkedHashSet<T> convertListToLinkedHashSet(List<T> originalList) {
        if (originalList == null) {
            throw new NullPointerException("The original list cannot be null");
        }
        return new LinkedHashSet<>(originalList);
    }

    public static <T> List<List<T>> removeItemsInChunksOf50ReturnList(LinkedHashSet<T> originalSet) {
        List<List<T>> chunks = new ArrayList<>();
        Iterator<T> iterator = originalSet.iterator();

        while (iterator.hasNext()) {
            List<T> chunk = new ArrayList<>();
            int count = 0;
            while (iterator.hasNext() && count < 50) {
                T item = iterator.next();
                chunk.add(item);
                iterator.remove();  // Remove the item from the original set
                count++;
            }
            chunks.add(chunk);
        }

        return chunks;
    }


    public static <T> List<List<T>> removeItemsInChunksOf50ReturnList(List<T> originalList) {
        // Reverse sort the original list
        // have to reverse it otherwise it will mess up order of objects
        //originalList.sort(Collections.reverseOrder());

        List<List<T>> chunks = new ArrayList<>();
        Iterator<T> iterator = originalList.iterator();

        while (iterator.hasNext()) {
            List<T> chunk = new ArrayList<>();
            int count = 0;
            while (iterator.hasNext() && count < 50) {
                T item = iterator.next();
                chunk.add(item);
                iterator.remove();  // Remove the item from the original list
                count++;
            }
            chunks.add(chunk);
        }

        return chunks;
    }

    public List<List<ProductVO>> splitIntoChunksOf50(List<ProductVO> originalList) {
        List<List<ProductVO>> result = new ArrayList<>();

        int listSize = originalList.size();
        int chunkSize = 50;

        for (int i = 0; i < listSize; i += chunkSize) {
            // Create sublist from the original list, making sure not to exceed the list size
            List<ProductVO> chunk = originalList.subList(i, Math.min(i + chunkSize, listSize));
            // Add the chunk to the result
            List newlist = new ArrayList<>(chunk)
            newlist = barcodeService.sortByPrice(newlist)
            result.add(newlist);  // Create a new list to avoid referencing the original sublist
        }

        return result;
    }

    public List<List<ProductVO>> splitIntoChunksOf10(List<ProductVO> originalList) {
        List<List<ProductVO>> result = new ArrayList<>();

        int listSize = originalList.size();
        int chunkSize = 10;

        for (int i = 0; i < listSize; i += chunkSize) {
            // Create sublist from the original list, making sure not to exceed the list size
            List<ProductVO> chunk = originalList.subList(i, Math.min(i + chunkSize, listSize));
            // Add the chunk to the result
            List newlist = new ArrayList<>(chunk)
            newlist = barcodeService.sortByPrice(newlist)
            result.add(newlist);  // Create a new list to avoid referencing the original sublist
        }

        return result;
    }

    // need this cuz the barcode code is a bit wavy if u knowwhatimsayin
    static float adjustRowXYMargin(int row, float y){

        // modify row positions
        if(row == 0){
            y-=14 // subtract from top
        }

        if(row == 1){
            y-=9 // subtract from top
        }

        if(row == 2){
            y-=6 // subtract from top
        }

        if(row == 3){
            y-=3 // subtract from top
        }


        if(row == 6){
            y+=7 // add to top
        }

        if(row == 7){
            y+=11 // add to top
        }

        if(row == 8){
            y+=14 // add to top
        }

        if(row == 9){
            y+=17 // add to top
        }
        return y
    }







}
