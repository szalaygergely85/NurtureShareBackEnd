package com.nurtureshare.backend.repository;

import com.nurtureshare.backend.model.Couple;
import com.nurtureshare.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoupleRepository extends JpaRepository<Couple, UUID> {
    Optional<Couple> findByPairingCode(String pairingCode);
    Optional<Couple> findByUser1OrUser2(User user1, User user2);
    Optional<Couple> findByUser1AndUser2IsNull(User user1);
}
