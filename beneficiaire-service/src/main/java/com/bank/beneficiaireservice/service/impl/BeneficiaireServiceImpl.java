package com.bank.beneficiaireservice.service.impl;

import com.bank.beneficiaireservice.dto.BeneficiaireDTO;
import com.bank.beneficiaireservice.dto.BeneficiaireRequest;
import com.bank.beneficiaireservice.dto.BeneficiaireResponse;
import com.bank.beneficiaireservice.exception.BeneficiaireNotFoundException;
import com.bank.beneficiaireservice.mapper.BeneficiaireMapper;
import com.bank.beneficiaireservice.model.Beneficiaire;
import com.bank.beneficiaireservice.repository.BeneficiaireRepository;
import com.bank.beneficiaireservice.service.BeneficiaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BeneficiaireServiceImpl implements BeneficiaireService {

    private final BeneficiaireRepository repository;
    private final BeneficiaireMapper mapper;

    @Override
    public BeneficiaireResponse create(BeneficiaireRequest request) {
        Beneficiaire entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    public BeneficiaireResponse update(Long id, BeneficiaireRequest request) {
        Beneficiaire entity = repository.findById(id)
                .orElseThrow(() -> new BeneficiaireNotFoundException(id));
        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new BeneficiaireNotFoundException(id);
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BeneficiaireResponse getById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new BeneficiaireNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public BeneficiaireResponse getByRib(String rib) {
        return repository.findByRib(rib)
                .map(mapper::toResponse)
                .orElseThrow(() -> new BeneficiaireNotFoundException(rib));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaireDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}
