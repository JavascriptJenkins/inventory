package com.techvvs.inventory.validation

import com.techvvs.inventory.model.TransactionVO
import org.springframework.stereotype.Component

@Component
class ValidateTransaction {


    boolean validateNewTransaction(TransactionVO transactionVO) {
        return true
    }


}
