package com.bank.virementservice.service;

import com.bank.virementservice.dto.VirementDTO;
import com.bank.virementservice.dto.VirementRequest;
import com.bank.virementservice.dto.VirementResponse;

import java.time.LocalDate;
import java.util.List;

public interface VirementService {
    VirementResponse create(VirementRequest request);
    VirementResponse update(Long id, VirementRequest request);
    void delete(Long id);
    VirementResponse getById(Long id);
    List<VirementDTO> getAll();
    List<VirementDTO> getByDate(LocalDate date);
}
