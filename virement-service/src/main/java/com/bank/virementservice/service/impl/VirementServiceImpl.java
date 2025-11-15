package com.bank.virementservice.service.impl;

import com.bank.virementservice.client.BeneficiaireClient;
import com.bank.virementservice.dto.VirementDTO;
import com.bank.virementservice.dto.VirementRequest;
import com.bank.virementservice.dto.VirementResponse;
import com.bank.virementservice.exception.VirementNotFoundException;
import com.bank.virementservice.mapper.VirementMapper;
import com.bank.virementservice.model.Virement;
import com.bank.virementservice.repository.VirementRepository;
import com.bank.virementservice.service.VirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VirementServiceImpl implements VirementService {

    private final VirementRepository repository;
    private final VirementMapper mapper;
    private final BeneficiaireClient beneficiaireClient;

    @Override
    public VirementResponse create(VirementRequest request) {
        beneficiaireClient.validateBeneficiaire(request.getDestinationAccount());
        Virement entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    public VirementResponse update(Long id, VirementRequest request) {
        Virement entity = repository.findById(id)
                .orElseThrow(() -> new VirementNotFoundException(id));
        mapper.updateEntity(entity, request);
        entity.setDateExecution(LocalDateTime.now());
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new VirementNotFoundException(id);
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public VirementResponse getById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new VirementNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VirementDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VirementDTO> getByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return repository.findByDateExecutionBetween(start, end).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}
