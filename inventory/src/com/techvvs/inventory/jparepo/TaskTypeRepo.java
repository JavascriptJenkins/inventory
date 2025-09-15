package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.TaskTypeVO;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface TaskTypeRepo extends JpaRepository<TaskTypeVO, Integer> {

    @Transactional
    void deleteByName(String name);

    List<TaskTypeVO> findAll();
    List<TaskTypeVO> findAllByName(String name);
    List<TaskTypeVO> findAllByDescription(String name);

}
