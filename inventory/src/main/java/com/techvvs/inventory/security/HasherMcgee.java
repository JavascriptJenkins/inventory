package com.techvvs.inventory.security;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Component
public class HasherMcgee {


    private static final String ALGO = "AES";
//    private static final byte[] keyValue = new byte[] { 'T', 'E', 'S', 'T' };
    private static final byte[] keyValue = Base64.getDecoder().decode("xxxxxxx"); // this is base 64 encoded key


/**
 * Encrypt a string using AES encryption algorithm.
 *
 * @param pwd the password to be encrypted
 * @return the encrypted string
 */
    public static String encrypt(String pwd) {
        String encodedPwd = "";
        try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(pwd.getBytes());
            encodedPwd = Base64.getEncoder().encodeToString(encVal);

        } catch (Exception e) {

            e.printStackTrace();
        }
        return encodedPwd;

    }




    /**
     * Decrypt a string with AES encryption algorithm.
     *
     * @param encryptedData the data to be decrypted
     * @return the decrypted string
     */
    public static String decrypt(String encryptedData) {
        String decodedPWD = "";
        try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decordedValue = Base64.getDecoder().decode(encryptedData);
            byte[] decValue = c.doFinal(decordedValue);
            decodedPWD = new String(decValue);

        } catch (Exception e) {

        }
        return decodedPWD;
    }

/**
 * Generate a new encryption key.
 */
    public static Key generateKey() {
        SecretKeySpec key = new SecretKeySpec(keyValue, ALGO);

        byte[] array = key.getEncoded();
        String mystring = new String(array);
        System.out.print(mystring);

        return key;
    }

}
