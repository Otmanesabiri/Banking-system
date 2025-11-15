package com.bank.virementservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "beneficiaire-service", contextId = "virementServiceBeneficiaireClient", fallback = BeneficiaireClientFallback.class)
public interface BeneficiaireClient {

    @GetMapping("/internal/beneficiaires/{rib}/validate")
    void validateBeneficiaire(@PathVariable("rib") String rib);
}
