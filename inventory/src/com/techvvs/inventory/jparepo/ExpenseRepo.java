package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.ExpenseVO;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ExpenseRepo extends JpaRepository<ExpenseVO, Integer> {

}
