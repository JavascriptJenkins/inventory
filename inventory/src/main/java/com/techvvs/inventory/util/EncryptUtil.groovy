package com.techvvs.inventory.util

import com.techvvs.inventory.model.CustomerVO

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Base64

import org.springframework.stereotype.Component

import java.security.MessageDigest


@Component
class EncryptUtil {




    def encrypt(String strToEncrypt, SecretKeySpec keySpec) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            return Base64.encodeBase64String(cipher.doFinal(strToEncrypt.getBytes("UTF-8")))
        } catch (Exception e) {
            throw new RuntimeException("Error occurred during encryption", e)
        }
    }

    def decrypt(String strToDecrypt, SecretKeySpec keySpec) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            return new String(cipher.doFinal(Base64.decodeBase64(strToDecrypt)))
        } catch (Exception e) {
            throw new RuntimeException("Error occurred during decryption", e)
        }
    }


    boolean encryptSingleRecordStringFields(Object item, SecretKeySpec keySpec) {
        item.class.declaredFields.each { field ->
            if (field.type == String) {
                field.accessible = true
                def originalValue = field.get(item)
                if (originalValue != null && originalValue instanceof String) {
                    def encryptedValue = encrypt(originalValue, keySpec)
                    field.set(item, encryptedValue)
                }
            }
        }
        return true
    }


    boolean decryptSingleRecordStringFields(Object item, SecretKeySpec keySpec) {
        item.class.declaredFields.each { field ->
            if (field.type == String) {
                field.accessible = true
                def originalValue = field.get(item)
                if (originalValue != null && originalValue instanceof String) {
                    def decryptedValue = decrypt(originalValue, keySpec)
                    field.set(item, decryptedValue)
                }
            }
        }
        return true
    }


    boolean isStringEncrypted(String str) {

        try {
            // Check if the string is valid Base64
            byte[] decodedBytes = Base64.decodeBase64(str)
            return true
        } catch (Exception e) {
            // If decoding fails, it's not a valid Base64 string
            return false
        }
    }

    // we are assuming nobody will every put a + sign or == sign in the customer name
    boolean checkifdataislocked(String input) {
        return input.contains("==") || input.contains("+")
    }


    String generateAESKey() {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256) // Specify the key size (128, 192, or 256 bits)
        SecretKey secretKey = keyGen.generateKey();
         // get string value
        return Base64.encodeBase64String(secretKey.getEncoded());
    }


}
