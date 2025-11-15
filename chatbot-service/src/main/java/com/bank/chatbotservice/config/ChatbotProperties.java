package com.bank.chatbotservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "chatbot")
@Data
public class ChatbotProperties {
    
    private Ai ai = new Ai();
    private Rag rag = new Rag();
    private Telegram telegram = new Telegram();
    
    @Data
    public static class Ai {
        private String provider = "openai";
        private String model = "gpt-4o";
        private Double temperature = 0.7;
        private Integer maxTokens = 500;
        private String systemPrompt = """
                Vous êtes un assistant bancaire intelligent pour une banque.
                Vous aidez les clients avec:
                - Informations sur les virements bancaires
                - Gestion des bénéficiaires
                - Procédures bancaires
                - Interprétation de documents (RIB, factures)
                
                Règles:
                - Répondez uniquement basé sur les documents fournis
                - Si l'information n'est pas dans le contexte, dites "Je ne sais pas"
                - Soyez professionnel et courtois
                - N'inventez jamais d'informations
                """;
    }
    
    @Data
    public static class Rag {
        private Integer chunkSize = 512;
        private Integer chunkOverlap = 50;
        private Integer topK = 5;
        private String documentsPath = "documents/";
        private Boolean autoIngest = true;
    }
    
    @Data
    public static class Telegram {
        private String botToken;
        private String botUsername;
        private Boolean enabled = false;
    }
}
