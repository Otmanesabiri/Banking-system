package com.bank.beneficiaireservice.controller;

import com.bank.beneficiaireservice.dto.BeneficiaireDTO;
import com.bank.beneficiaireservice.dto.BeneficiaireRequest;
import com.bank.beneficiaireservice.dto.BeneficiaireResponse;
import com.bank.beneficiaireservice.service.BeneficiaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaires")
@RequiredArgsConstructor
public class BeneficiaireController {

    private final BeneficiaireService service;

    @PostMapping
    public ResponseEntity<BeneficiaireResponse> create(@Valid @RequestBody BeneficiaireRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BeneficiaireResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody BeneficiaireRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BeneficiaireResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/rib/{rib}")
    public ResponseEntity<BeneficiaireResponse> getByRib(@PathVariable String rib) {
        return ResponseEntity.ok(service.getByRib(rib));
    }

    @GetMapping
    public ResponseEntity<List<BeneficiaireDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}
