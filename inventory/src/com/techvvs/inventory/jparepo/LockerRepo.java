package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.LockerVO;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface LockerRepo extends JpaRepository<LockerVO, Integer> {

    @Transactional
    void deleteByName(String name);

    List<LockerVO> findAll();
    List<LockerVO> findAllByName(String name);
    Optional<LockerVO> findByName(String name);
    List<LockerVO> findAllByDescription(String name);

}
