package com.bank.chatbotservice.service.tools;

import com.bank.chatbotservice.client.VirementClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * MCP Tools for Virement operations
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class VirementTools {
    
    private final VirementClient virementClient;
    
    /**
     * Get all virements
     */
    @Description("Récupère la liste de tous les virements")
    public Function<Void, String> getAllVirements() {
        return (input) -> {
            try {
                log.info("Tool called: getAllVirements");
                List<VirementClient.VirementDTO> virements = virementClient.getAllVirements();
                
                if (virements.isEmpty()) {
                    return "Aucun virement trouvé.";
                }
                
                StringBuilder result = new StringBuilder("Liste des virements:\n");
                for (VirementClient.VirementDTO v : virements) {
                    result.append(String.format(
                            "- ID: %d, Bénéficiaire: %d, Montant: %.2f€, Type: %s, Statut: %s, Date: %s\n",
                            v.getId(), v.getBeneficiaireId(), v.getMontant(),
                            v.getType(), v.getStatut(), v.getDateVirement()));
                }
                
                return result.toString();
                
            } catch (Exception e) {
                log.error("Error calling getAllVirements", e);
                return "Erreur lors de la récupération des virements: " + e.getMessage();
            }
        };
    }
    
    /**
     * Get virement by ID
     */
    @Description("Récupère les informations d'un virement par son ID")
    public Function<Long, String> getVirement() {
        return (id) -> {
            try {
                log.info("Tool called: getVirement with id={}", id);
                VirementClient.VirementDTO virement = virementClient.getVirement(id);
                
                return String.format(
                        "Virement trouvé:\n" +
                        "ID: %d\n" +
                        "Bénéficiaire ID: %d\n" +
                        "RIB Source: %s\n" +
                        "Montant: %.2f€\n" +
                        "Description: %s\n" +
                        "Type: %s\n" +
                        "Statut: %s\n" +
                        "Date: %s",
                        virement.getId(),
                        virement.getBeneficiaireId(),
                        virement.getRibSource(),
                        virement.getMontant(),
                        virement.getDescription(),
                        virement.getType(),
                        virement.getStatut(),
                        virement.getDateVirement()
                );
                
            } catch (Exception e) {
                log.error("Error calling getVirement", e);
                return "Erreur: Virement non trouvé (ID: " + id + ")";
            }
        };
    }
    
    /**
     * Get virements by beneficiaire
     */
    @Description("Récupère tous les virements pour un bénéficiaire donné")
    public Function<Long, String> getVirementsByBeneficiaire() {
        return (beneficiaireId) -> {
            try {
                log.info("Tool called: getVirementsByBeneficiaire with beneficiaireId={}", beneficiaireId);
                List<VirementClient.VirementDTO> virements = 
                        virementClient.getVirementsByBeneficiaire(beneficiaireId);
                
                if (virements.isEmpty()) {
                    return "Aucun virement trouvé pour le bénéficiaire ID: " + beneficiaireId;
                }
                
                StringBuilder result = new StringBuilder(
                        String.format("Virements pour le bénéficiaire %d:\n", beneficiaireId));
                
                double total = 0;
                for (VirementClient.VirementDTO v : virements) {
                    result.append(String.format(
                            "- ID: %d, Montant: %.2f€, Type: %s, Statut: %s, Date: %s\n",
                            v.getId(), v.getMontant(), v.getType(), v.getStatut(), v.getDateVirement()));
                    total += v.getMontant().doubleValue();
                }
                
                result.append(String.format("\nTotal: %.2f€", total));
                
                return result.toString();
                
            } catch (Exception e) {
                log.error("Error calling getVirementsByBeneficiaire", e);
                return "Erreur lors de la récupération: " + e.getMessage();
            }
        };
    }
}
