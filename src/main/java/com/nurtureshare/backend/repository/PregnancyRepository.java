package com.nurtureshare.backend.repository;

import com.nurtureshare.backend.model.Pregnancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PregnancyRepository extends JpaRepository<Pregnancy, UUID> {
    Optional<Pregnancy> findByCoupleId(UUID coupleId);
}
