package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.SystemUserDAO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemUserRepo extends CrudRepository<SystemUserDAO, Long> {

        List<SystemUserDAO> findAll();
        SystemUserDAO findByEmail(String email);
        SystemUserDAO findById(Integer id);


       // List<SystemUserDAO> findByOrderByUpvotesDesc();


}
