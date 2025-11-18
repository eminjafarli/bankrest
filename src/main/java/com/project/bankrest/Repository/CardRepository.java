package com.project.bankrest.Repository;

import com.project.bankrest.Entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByUserId(Long userId);
    @Query("SELECT c FROM Card c JOIN FETCH c.user ORDER BY c.id DESC")
    List<Card> findAllWithUserOrderByIdAsc();

    @Query("SELECT c FROM Card c JOIN FETCH c.user WHERE c.user.id = :userId ORDER BY c.id ASC")
    Page<Card> findByUser_IdWithUserEagerly(Long userId, Pageable pageable);

    @Query("SELECT c FROM Card c JOIN FETCH c.user WHERE c.user.id = :userId AND c.number LIKE %:search% ORDER BY c.id ASC")
    Page<Card> findByUser_IdAndNumberContainingWithUserEagerly(Long userId, String search, Pageable pageable);
}
