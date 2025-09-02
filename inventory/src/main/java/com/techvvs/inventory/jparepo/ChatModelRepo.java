package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.ChatModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatModelRepo extends JpaRepository<ChatModel, Integer> {

    List<ChatModel> findByIsActiveTrue();
    
    List<ChatModel> findByIsActiveTrueOrderByNameAsc();
    
    Optional<ChatModel> findByFolderPath(String folderPath);
    
    @Query("SELECT cm FROM ChatModel cm WHERE cm.isActive = true AND cm.modelType = :modelType")
    List<ChatModel> findActiveByModelType(@Param("modelType") String modelType);
    
    @Query("SELECT cm FROM ChatModel cm WHERE cm.isActive = true AND (cm.name LIKE %:searchTerm% OR cm.description LIKE %:searchTerm%)")
    List<ChatModel> searchActiveModels(@Param("searchTerm") String searchTerm);
    
    boolean existsByFolderPath(String folderPath);
    
    @Query("SELECT COUNT(c) FROM Chat c WHERE c.chatModel.id = :chatModelId")
    long countChatsByChatModel(@Param("chatModelId") Integer chatModelId);
}

