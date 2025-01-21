package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.TokenDAO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepo extends CrudRepository<TokenDAO, Long> {

    // todo: make this only grab tokens from last 24 hours
    List<TokenDAO> findAllByUsermetadata(String email);

    Optional<TokenDAO> findByToken(String token);

    // this will list with oldest token at the top
    List<TokenDAO> findTop10ByUsermetadataOrderByCreatetimestampAsc(String email);

    // this will list with newest token at the top
    List<TokenDAO> findTop10ByUsermetadataOrderByCreatetimestampDesc(String email);



}
