package com.bank.beneficiaireservice.repository;

import com.bank.beneficiaireservice.model.Beneficiaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeneficiaireRepository extends JpaRepository<Beneficiaire, Long> {
    Optional<Beneficiaire> findByRib(String rib);
    boolean existsByRib(String rib);
}
