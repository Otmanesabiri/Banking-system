package com.bank.chatbotservice.config;

import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.ChromaVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chroma.ChromaApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SpringAIConfig {
    
    @Value("${spring.ai.vectorstore.chroma.client.host:http://localhost:8000}")
    private String chromaHost;
    
    /**
     * Chat Memory for conversation context
     */
    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }
    
    /**
     * ChatClient for interacting with LLM
     */
    @Bean
    public ChatClient chatClient(AzureOpenAiChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .build();
    }
    
    /**
     * Vector Store for RAG
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel, RestClient.Builder restClientBuilder) {
        String chromaUrl = chromaHost;
        ChromaApi chromaApi = new ChromaApi(chromaUrl, restClientBuilder);
        return new ChromaVectorStore(embeddingModel, chromaApi, "bank-documents", true);
    }
}
