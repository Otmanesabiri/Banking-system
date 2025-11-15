package com.bank.chatbotservice.service.tools;

import com.bank.chatbotservice.client.BeneficiaireClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * MCP Tools for Beneficiaire operations
 * These tools allow the AI agent to interact with the beneficiaire service
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BeneficiaireTools {
    
    private final BeneficiaireClient beneficiaireClient;
    
    /**
     * Get all beneficiaires
     */
    @Description("Récupère la liste de tous les bénéficiaires de la banque")
    public Function<Void, String> getAllBeneficiaires() {
        return (input) -> {
            try {
                log.info("Tool called: getAllBeneficiaires");
                List<BeneficiaireClient.BeneficiaireDTO> beneficiaires = beneficiaireClient.getAllBeneficiaires();
                
                if (beneficiaires.isEmpty()) {
                    return "Aucun bénéficiaire trouvé.";
                }
                
                StringBuilder result = new StringBuilder("Liste des bénéficiaires:\n");
                for (BeneficiaireClient.BeneficiaireDTO b : beneficiaires) {
                    result.append(String.format("- ID: %d, Nom: %s %s, RIB: %s, Type: %s\n",
                            b.getId(), b.getNom(), b.getPrenom(), b.getRib(), b.getType()));
                }
                
                return result.toString();
                
            } catch (Exception e) {
                log.error("Error calling getAllBeneficiaires", e);
                return "Erreur lors de la récupération des bénéficiaires: " + e.getMessage();
            }
        };
    }
    
    /**
     * Get beneficiaire by ID
     */
    @Description("Récupère les informations d'un bénéficiaire par son ID")
    public Function<Long, String> getBeneficiaire() {
        return (id) -> {
            try {
                log.info("Tool called: getBeneficiaire with id={}", id);
                BeneficiaireClient.BeneficiaireDTO beneficiaire = beneficiaireClient.getBeneficiaire(id);
                
                return String.format(
                        "Bénéficiaire trouvé:\n" +
                        "ID: %d\n" +
                        "Nom: %s %s\n" +
                        "RIB: %s\n" +
                        "Type: %s\n" +
                        "Actif: %s\n" +
                        "Date création: %s",
                        beneficiaire.getId(),
                        beneficiaire.getNom(),
                        beneficiaire.getPrenom(),
                        beneficiaire.getRib(),
                        beneficiaire.getType(),
                        beneficiaire.getActif() ? "Oui" : "Non",
                        beneficiaire.getDateCreation()
                );
                
            } catch (Exception e) {
                log.error("Error calling getBeneficiaire", e);
                return "Erreur: Bénéficiaire non trouvé (ID: " + id + ")";
            }
        };
    }
    
    /**
     * Search beneficiaires by name
     */
    @Description("Recherche des bénéficiaires par nom")
    public Function<String, String> searchBeneficiaires() {
        return (nom) -> {
            try {
                log.info("Tool called: searchBeneficiaires with nom={}", nom);
                List<BeneficiaireClient.BeneficiaireDTO> beneficiaires = 
                        beneficiaireClient.searchBeneficiaires(nom);
                
                if (beneficiaires.isEmpty()) {
                    return "Aucun bénéficiaire trouvé avec le nom: " + nom;
                }
                
                StringBuilder result = new StringBuilder("Bénéficiaires trouvés:\n");
                for (BeneficiaireClient.BeneficiaireDTO b : beneficiaires) {
                    result.append(String.format("- ID: %d, Nom: %s %s, RIB: %s\n",
                            b.getId(), b.getNom(), b.getPrenom(), b.getRib()));
                }
                
                return result.toString();
                
            } catch (Exception e) {
                log.error("Error calling searchBeneficiaires", e);
                return "Erreur lors de la recherche: " + e.getMessage();
            }
        };
    }
}
