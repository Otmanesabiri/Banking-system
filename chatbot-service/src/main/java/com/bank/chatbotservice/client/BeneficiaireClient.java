package com.bank.chatbotservice.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Feign client for Beneficiaire Service (MCP Tool)
 */
@FeignClient(name = "beneficiaire-service", path = "/api/beneficiaires")
public interface BeneficiaireClient {
    
    @GetMapping
    List<BeneficiaireDTO> getAllBeneficiaires();
    
    @GetMapping("/{id}")
    BeneficiaireDTO getBeneficiaire(@PathVariable("id") Long id);
    
    @GetMapping("/search")
    List<BeneficiaireDTO> searchBeneficiaires(@RequestParam("nom") String nom);
    
    @PostMapping
    BeneficiaireDTO createBeneficiaire(@RequestBody BeneficiaireDTO beneficiaire);
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class BeneficiaireDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String rib;
        private String type;
        private LocalDateTime dateCreation;
        private Boolean actif;
    }
}
