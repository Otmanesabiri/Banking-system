package com.bank.virementservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "virements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Virement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sourceAccount;

    @Column(nullable = false)
    private String destinationAccount;

    @Column(nullable = false)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeVirement type;

    @Column(nullable = false)
    private LocalDateTime dateExecution;

    @Column(nullable = false)
    private String statut;

    private String motif;
}
