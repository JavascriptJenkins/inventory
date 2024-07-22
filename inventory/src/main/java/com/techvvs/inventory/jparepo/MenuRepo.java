package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.MenuVO;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface MenuRepo extends JpaRepository<MenuVO, Integer> {

    @Transactional
    void deleteByName(String name);

    List<MenuVO> findAll();


}
