package com.techvvs.inventory.barcode.impl

import org.springframework.stereotype.Component


@Component
class BarcodeHelper {


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
    static String generateBarcodeData(int row, int col, String filenameExtension, int batchnumber, int pagenumber) {
        // Example method to generate unique barcode data based on row and column
        // note: this baseData can only be 6 characters long - batchnumbers are 7 characters so we are removing the last char
        String baseData = removeLast2Character(String.valueOf(batchnumber))+String.valueOf(pagenumber); // Base data for the barcode
        String rowColData = String.format("%02d%02d", row, col); // Row and column indices padded with leading zeros

        // Combine base data with row and column data
        String barcodeData = baseData + rowColData;

        // Calculate and append the checksum
        int checksum = calculateUPCAChecksum(barcodeData);
        barcodeData += checksum;

        return barcodeData;
    }


    // Method to calculate the checksum for UPC-A barcode data
    private static int calculateUPCAChecksum(String data) {
        int sum = 0;
        for (int i = 0; i < data.length(); i++) {
            int digit = Character.getNumericValue(data.charAt(i));
            if ((i + data.length()) % 2 == 0) {
                sum += digit * 3;
            } else {
                sum += digit;
            }
        }
        return (10 - (sum % 10)) % 10;
    }


    static String removeLast2Character(String str) {
        if (str == null || str.isEmpty()) {
            return str; // Return the original string if it's null or empty.
        }
        return str.substring(0, str.length() - 2); // Use substring to remove the last character.
    }


    static <T> LinkedHashSet<T> convertToLinkedHashSet(Set<T> originalSet) {
        return new LinkedHashSet<>(originalSet);
    }

    static <T> LinkedHashSet<T> convertListToLinkedHashSet(List<T> originalList) {
        if (originalList == null) {
            throw new NullPointerException("The original list cannot be null");
        }
        return new LinkedHashSet<>(originalList);
    }







}
