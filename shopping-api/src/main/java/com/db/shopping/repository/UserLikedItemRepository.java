package com.db.shopping.repository;

import com.db.shopping.entity.UserLikedItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLikedItemRepository extends JpaRepository<UserLikedItem, Long> {

    @EntityGraph(attributePaths = {"product"})
    List<UserLikedItem> findByUserId(Long userId);
}