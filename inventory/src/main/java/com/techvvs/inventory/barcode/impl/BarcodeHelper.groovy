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
        // Create base data from batchnumber and pagenumber, ensuring it's exactly 6 characters
        String baseData = removeLast2Character(String.valueOf(batchnumber)) + String.valueOf(pagenumber);

        // Ensure the baseData is exactly 6 characters long
        baseData = baseData.length() > 6 ? baseData.substring(0, 6) : baseData.padLeft(6, '0');

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


    static String removeLast2Character(String str) {
        if (str == null || str.isEmpty()) {
            return str; // Return the original string if it's null or empty.
        }
        return str.substring(0, str.length() - 2); // Use substring to remove the last character.
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







}
