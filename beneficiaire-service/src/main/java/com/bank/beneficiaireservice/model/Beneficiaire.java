package com.bank.beneficiaireservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "beneficiaires")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Beneficiaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String rib;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeBeneficiaire type;
}
