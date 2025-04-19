package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.RewardConfigVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RewardConfigRepo extends JpaRepository<RewardConfigVO, Integer> {

    Optional<RewardConfigVO> findByIsactiveAndRegion(int isactive, String region);

}
