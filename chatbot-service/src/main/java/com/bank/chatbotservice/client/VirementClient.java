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
 * Feign client for Virement Service (MCP Tool)
 */
@FeignClient(name = "virement-service", path = "/api/virements")
public interface VirementClient {
    
    @GetMapping
    List<VirementDTO> getAllVirements();
    
    @GetMapping("/{id}")
    VirementDTO getVirement(@PathVariable("id") Long id);
    
    @GetMapping("/beneficiaire/{beneficiaireId}")
    List<VirementDTO> getVirementsByBeneficiaire(@PathVariable("beneficiaireId") Long beneficiaireId);
    
    @PostMapping
    VirementDTO createVirement(@RequestBody VirementDTO virement);
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class VirementDTO {
        private Long id;
        private Long beneficiaireId;
        private String ribSource;
        private BigDecimal montant;
        private String description;
        private LocalDateTime dateVirement;
        private String type;
        private String statut;
    }
}
