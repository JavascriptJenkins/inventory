package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.BatchVO;
import com.techvvs.inventory.model.CartVO;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface CartRepo extends JpaRepository<CartVO, Integer> {


    List<CartVO> findAll();


}
