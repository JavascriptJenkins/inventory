package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.BatchTypeVO;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface BatchTypeRepo extends JpaRepository<BatchTypeVO, Integer> {

    @Transactional
    void deleteByName(String name);

    List<BatchTypeVO> findAll();
    List<BatchTypeVO> findAllByName(String name);
    Optional<BatchTypeVO> findByName(String name);
    List<BatchTypeVO> findAllByDescription(String name);

}
