package com.bank.virementservice.controller;

import com.bank.virementservice.dto.VirementDTO;
import com.bank.virementservice.dto.VirementRequest;
import com.bank.virementservice.dto.VirementResponse;
import com.bank.virementservice.service.VirementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/virements")
@RequiredArgsConstructor
public class VirementController {

    private final VirementService service;

    @PostMapping
    public ResponseEntity<VirementResponse> create(@Valid @RequestBody VirementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VirementResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody VirementRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VirementResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<VirementDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/date")
    public ResponseEntity<List<VirementDTO>> getByDate(@RequestParam("value") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.getByDate(date));
    }
}
