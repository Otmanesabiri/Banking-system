package com.bank.beneficiaireservice.service;

import com.bank.beneficiaireservice.dto.BeneficiaireDTO;
import com.bank.beneficiaireservice.dto.BeneficiaireRequest;
import com.bank.beneficiaireservice.dto.BeneficiaireResponse;

import java.util.List;

public interface BeneficiaireService {
    BeneficiaireResponse create(BeneficiaireRequest request);
    BeneficiaireResponse update(Long id, BeneficiaireRequest request);
    void delete(Long id);
    BeneficiaireResponse getById(Long id);
    BeneficiaireResponse getByRib(String rib);
    List<BeneficiaireDTO> getAll();
}
