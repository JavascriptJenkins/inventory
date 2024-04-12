package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.TaskVO;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface TaskRepo extends JpaRepository<TaskVO, Integer> {

    @Transactional
    void deleteByName(String name);

    List<TaskVO> findAll();
    List<TaskVO> findAllByTasknumber(Integer tasknumber);
    List<TaskVO> findAllByName(String name);
    List<TaskVO> findAllByDescription(String name);

}
