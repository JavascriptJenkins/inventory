package com.techvvs.inventory.service.rewards

import com.techvvs.inventory.jparepo.RewardConfigRepo
import com.techvvs.inventory.jparepo.RewardRepo
import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.model.RewardConfigVO
import com.techvvs.inventory.model.RewardVO
import com.techvvs.inventory.model.TransactionVO
import com.techvvs.inventory.service.rewards.constants.RewardAction
import com.techvvs.inventory.service.rewards.constants.RewardReason
import com.techvvs.inventory.service.rewards.constants.RewardSourceChannel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RewardsService {

    @Autowired
    RewardRepo rewardRepo

    @Autowired
    RewardConfigRepo rewardConfigRepo


    int calculateRewardPointsForTransaction(TransactionVO transactionVO) {

    }


    int calculateRewardPointsForCustomer(CustomerVO customerVO) {

    }


    String calculateRewardStatusForCustomer(CustomerVO customerVO) {

    }


    // Create & persist a reward for a transaction
    RewardVO createReward(TransactionVO transaction, CustomerVO customer, int points, RewardAction action, RewardReason reason, RewardSourceChannel channel){



    }

    // Redeem reward points for a transaction (subtract)
    RewardVO redeemPoints(CustomerVO customer, int pointsToRedeem, String description){


    }

    // Reverse reward points (for refund, cancel, etc.)
    RewardVO reverseRewardForTransaction(TransactionVO transaction, RewardReason reason){

    }


    // Total rewards earned by a customer
    int getTotalEarnedPoints(CustomerVO customer){


    }


    // Total rewards available (not yet used)
    int getAvailablePoints(CustomerVO customer){


    }


    // Get all rewards for a customer
    List<RewardVO> getRewardHistory(CustomerVO customer){

    }

    // Calculate points based on config
    int calculatePointsFromAmount(double amount, String region){


    }



    // Load active reward config for a region
    RewardConfigVO getActiveRewardConfig(String region){

    }









}
