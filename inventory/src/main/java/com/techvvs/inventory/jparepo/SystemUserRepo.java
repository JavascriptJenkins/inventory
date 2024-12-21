package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.SystemUserDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemUserRepo extends JpaRepository<SystemUserDAO, Integer> {

        List<SystemUserDAO> findAll();
        SystemUserDAO findByEmail(String email);
        Optional<SystemUserDAO> findById(Integer id);


       // List<SystemUserDAO> findByOrderByUpvotesDesc();


}
