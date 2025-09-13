package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.Chat;
import com.techvvs.inventory.model.SystemUserDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepo extends JpaRepository<Chat, Integer> {

    List<Chat> findBySystemUserOrderByUpdatedTimestampDesc(SystemUserDAO systemUser);
    
    List<Chat> findBySystemUserAndIsActiveOrderByUpdatedTimestampDesc(SystemUserDAO systemUser, Boolean isActive);
    
    Optional<Chat> findByIdAndSystemUser(Integer id, SystemUserDAO systemUser);
    
    @Query("SELECT c FROM Chat c WHERE c.systemUser = :systemUser AND c.isActive = true ORDER BY c.updatedTimestamp DESC")
    List<Chat> findActiveChatsByUser(@Param("systemUser") SystemUserDAO systemUser);
    
    @Query("SELECT c FROM Chat c LEFT JOIN FETCH c.chatModel cm LEFT JOIN FETCH cm.createdByUser WHERE c.systemUser = :systemUser AND c.isActive = true ORDER BY c.updatedTimestamp DESC")
    List<Chat> findActiveChatsByUserWithChatModel(@Param("systemUser") SystemUserDAO systemUser);

    @Query("SELECT c FROM Chat c LEFT JOIN FETCH c.chatModel cm LEFT JOIN FETCH cm.createdByUser WHERE c.id = :id AND c.systemUser = :systemUser")
    Optional<Chat> findByIdAndSystemUserWithChatModel(@Param("id") Integer id, @Param("systemUser") SystemUserDAO systemUser);
    
    @Query("SELECT COUNT(c) FROM Chat c WHERE c.systemUser = :systemUser")
    Long countChatsByUser(@Param("systemUser") SystemUserDAO systemUser);
}
