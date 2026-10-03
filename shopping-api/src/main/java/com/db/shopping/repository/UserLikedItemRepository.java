package com.db.shopping.repository;

import com.db.shopping.entity.UserLikedItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLikedItemRepository extends JpaRepository<UserLikedItem, Long> {
    List<UserLikedItem> findByUserId(Long userId);
}