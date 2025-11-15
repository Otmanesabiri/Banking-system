package com.bank.chatbotservice.telegram;

import com.bank.chatbotservice.config.ChatbotProperties;
import com.bank.chatbotservice.dto.ChatRequest;
import com.bank.chatbotservice.dto.ChatResponse;
import com.bank.chatbotservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Comparator;
import java.util.List;

/**
 * Telegram Bot integration for chatbot
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "chatbot.telegram", name = "enabled", havingValue = "true")
public class BankChatBot extends TelegramLongPollingBot {
    
    private final ChatService chatService;
    private final ChatbotProperties properties;
    
    @Override
    public String getBotUsername() {
        return properties.getTelegram().getBotUsername();
    }
    
    @Override
    public String getBotToken() {
        return properties.getTelegram().getBotToken();
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            String userId = update.getMessage().getFrom().getId().toString();
            
            try {
                // Send typing action
                sendTypingAction(chatId);
                
                // Handle text message
                if (update.getMessage().hasText()) {
                    String messageText = update.getMessage().getText();
                    handleTextMessage(chatId, userId, messageText);
                }
                // Handle photo message
                else if (update.getMessage().hasPhoto()) {
                    handlePhotoMessage(chatId, userId, update.getMessage().getPhoto(),
                            update.getMessage().getCaption());
                }
                
            } catch (Exception e) {
                log.error("Error processing update", e);
                sendMessage(chatId, "Désolé, une erreur s'est produite. Veuillez réessayer.");
            }
        }
    }
    
    /**
     * Handle text message
     */
    private void handleTextMessage(Long chatId, String userId, String text) {
        log.info("Received text message from user {}: {}", userId, text);
        
        // Handle commands
        if (text.startsWith("/")) {
            handleCommand(chatId, userId, text);
            return;
        }
        
        // Process with ChatService
        ChatRequest request = ChatRequest.builder()
                .sessionId(userId)
                .userId(userId)
                .message(text)
                .streaming(false)
                .build();
        
        ChatResponse response = chatService.processMessage(request);
        
        if (response.getSuccess()) {
            sendMessage(chatId, response.getMessage());
        } else {
            sendMessage(chatId, "Désolé, je n'ai pas pu traiter votre demande: " + response.getError());
        }
    }
    
    /**
     * Handle photo message (multimodal)
     */
    private void handlePhotoMessage(Long chatId, String userId, List<PhotoSize> photos, String caption) {
        log.info("Received photo message from user {}", userId);
        
        // Get highest resolution photo
        PhotoSize photo = photos.stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .orElse(null);
        
        if (photo == null) {
            sendMessage(chatId, "Impossible de traiter l'image.");
            return;
        }
        
        // Get file URL (simplified - in production, use Telegram API to download)
        String fileId = photo.getFileId();
        String imageUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + fileId;
        
        String message = caption != null ? caption : "Analysez cette image";
        
        ChatRequest request = ChatRequest.builder()
                .sessionId(userId)
                .userId(userId)
                .message(message)
                .imageUrl(imageUrl)
                .streaming(false)
                .build();
        
        ChatResponse response = chatService.processMessage(request);
        
        if (response.getSuccess()) {
            sendMessage(chatId, response.getMessage());
        } else {
            sendMessage(chatId, "Désolé, je n'ai pas pu analyser l'image.");
        }
    }
    
    /**
     * Handle bot commands
     */
    private void handleCommand(Long chatId, String userId, String command) {
        switch (command) {
            case "/start":
                sendMessage(chatId, """
                        Bienvenue sur le chatbot bancaire! 🏦
                        
                        Je peux vous aider avec:
                        • Informations sur les virements
                        • Gestion des bénéficiaires
                        • Analyse de documents (RIB, factures)
                        • Questions sur les services bancaires
                        
                        Commandes:
                        /help - Afficher l'aide
                        /clear - Effacer l'historique
                        
                        Posez-moi une question!
                        """);
                break;
                
            case "/help":
                sendMessage(chatId, """
                        Aide du chatbot bancaire:
                        
                        Vous pouvez:
                        1. Poser des questions en texte
                        2. Envoyer des photos de documents à analyser
                        3. Demander des informations sur vos virements
                        4. Rechercher des bénéficiaires
                        
                        Exemples:
                        • "Liste de mes bénéficiaires"
                        • "Créer un virement"
                        • "Comment faire un virement instantané?"
                        • [Envoyer photo RIB] + "Extraire les informations"
                        """);
                break;
                
            case "/clear":
                chatService.clearSession(userId);
                sendMessage(chatId, "Historique de conversation effacé ✓");
                break;
                
            default:
                sendMessage(chatId, "Commande inconnue. Tapez /help pour l'aide.");
        }
    }
    
    /**
     * Send message to user
     */
    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.enableMarkdown(true);
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message", e);
        }
    }
    
    /**
     * Send typing action
     */
    private void sendTypingAction(Long chatId) {
        SendChatAction action = new SendChatAction();
        action.setChatId(chatId.toString());
        action.setAction(org.telegram.telegrambots.meta.api.methods.ActionType.TYPING);
        
        try {
            execute(action);
        } catch (TelegramApiException e) {
            log.error("Error sending typing action", e);
        }
    }
}
