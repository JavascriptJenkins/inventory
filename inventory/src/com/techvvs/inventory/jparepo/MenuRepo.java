package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.MenuVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface MenuRepo extends JpaRepository<MenuVO, Integer> {

    @Transactional
    void deleteByName(String name);

    List<MenuVO> findAll();



//    @Query("SELECT m FROM MenuVO m " +
//            "JOIN FETCH m.menu_product_list p " +
//            "LEFT JOIN FETCH p.attribute_list " +
//            "WHERE m.menuid = :menuid")
//    Optional<MenuVO> findByIdWithAttributes(@Param("menuid") Integer menuid);

}
