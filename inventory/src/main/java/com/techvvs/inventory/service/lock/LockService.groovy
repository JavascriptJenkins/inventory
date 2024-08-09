package com.techvvs.inventory.service.lock

import com.techvvs.inventory.jparepo.CustomerRepo
import com.techvvs.inventory.jparepo.ProductRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.modelnonpersist.LockVO
import com.techvvs.inventory.util.EncryptUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.crypto.spec.SecretKeySpec
import javax.transaction.Transactional
import java.security.MessageDigest

@Service
class LockService {

    @Autowired
    CustomerRepo customerRepo

    @Autowired
    ProductRepo productRepo

    @Autowired
    TransactionRepo transactionRepo

    @Autowired
    EncryptUtil encryptUtil


    @Transactional
    boolean lockdata(LockVO lockVO) {


        byte[] secretKey = hashSecretKey(lockVO.secretkey)

        def keySpec = new SecretKeySpec(secretKey, "AES")

        for(CustomerVO customerVO : customerRepo.findAll()) {
            encryptUtil.encryptSingleRecordStringFields(customerVO, keySpec)
            customerRepo.save(customerVO)
        }

        return true
    }


    def hashSecretKey(String secretKey) {
        MessageDigest sha = MessageDigest.getInstance("SHA-256")
        byte[] keyBytes = sha.digest(secretKey.getBytes("UTF-8"))
        return keyBytes
    }


    @Transactional
    boolean unlockdata(LockVO lockVO) {


        byte[] secretKey = hashSecretKey(lockVO.secretkey)

        def keySpec = new SecretKeySpec(secretKey, "AES")

        for(CustomerVO customerVO : customerRepo.findAll()) {
            encryptUtil.decryptSingleRecordStringFields(customerVO, keySpec)
            customerRepo.save(customerVO)
        }

        return true
    }

    boolean checkifdataislocked() {

        // the name is the same length as a 256 bit AES key then we are considering it locked
        for(CustomerVO customerVO : customerRepo.findAll()) {
            if(encryptUtil.checkifdataislocked(customerVO.name)){
                return true
            }
        }
        return false
    }


    String generateSecretKey() {
        return encryptUtil.generateAESKey()
    }









}
