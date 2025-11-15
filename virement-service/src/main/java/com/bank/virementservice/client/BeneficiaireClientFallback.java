package com.bank.virementservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BeneficiaireClientFallback implements BeneficiaireClient {

    @Override
    public void validateBeneficiaire(String rib) {
        log.warn("Fallback validation for beneficiaire {} - assuming valid", rib);
    }
}
