# Chatbot Service - Banking System

Service de chatbot intelligent basé sur Spring AI, RAG (Retrieval Augmented Generation), et intégration Telegram pour le système bancaire.

## 🚀 Fonctionnalités

### Intelligence Artificielle
- **LLM**: GPT-4o (OpenAI) - modèle multimodal
- **RAG**: Retrieval Augmented Generation avec embeddings vectoriels
- **Mémoire conversationnelle**: Maintien du contexte sur plusieurs échanges
- **Vision**: Analyse d'images (RIB, factures, documents bancaires)

### Outils MCP (Model Context Protocol)
Le chatbot peut interagir avec les autres microservices:
- **Bénéficiaires**: Consulter, rechercher des bénéficiaires
- **Virements**: Consulter l'historique, vérifier statuts

### Interfaces
- **REST API**: Endpoints HTTP standard
- **Telegram Bot**: Interface conversationnelle via Telegram
- **Streaming**: Réponses en temps réel (SSE)

## 📋 Prérequis

### Services externes
1. **OpenAI API**
   - Clé API requise
   - Modèles: `gpt-4o`, `text-embedding-3-small`

2. **ChromaDB** (Vector Store)
   ```bash
   docker run -d -p 8000:8000 chromadb/chroma
   ```

3. **Telegram Bot** (optionnel)
   - Créer bot via [@BotFather](https://t.me/botfather)
   - Récupérer token

### Services microservices
- Eureka Discovery Service (port 8761)
- Beneficiaire Service (port 8084)
- Virement Service (port 8082)

## ⚙️ Configuration

### Variables d'environnement

```bash
export OPENAI_API_KEY=sk-your-api-key
export CHROMA_HOST=http://localhost:8000
export TELEGRAM_BOT_ENABLED=true
export TELEGRAM_BOT_TOKEN=your-bot-token
export TELEGRAM_BOT_USERNAME=YourBankBot
```

### application.yml

Voir le fichier de configuration pour tous les paramètres disponibles.

## 🏗️ Architecture

```
chatbot-service/
├── config/              # Configuration Spring AI, ChatMemory
├── domain/              # Entités JPA (ChatSession, ChatMessage, BankDocument)
├── repository/          # Repositories Spring Data
├── service/
│   ├── ChatService      # Service principal de chat
│   ├── rag/
│   │   ├── DocumentIngestionService    # Ingestion PDFs
│   │   └── DocumentRetrievalService    # Recherche vectorielle
│   └── tools/
│       ├── BeneficiaireTools    # Outils MCP bénéficiaires
│       └── VirementTools        # Outils MCP virements
├── client/              # Feign clients (MCP)
├── controller/          # REST API
└── telegram/            # Telegram Bot
```

## 🔧 Utilisation

### 1. Démarrer le service

```bash
cd chatbot-service
mvn clean install
mvn spring-boot:run
```

### 2. Ingérer des documents (RAG)

Placer les PDFs dans `documents/` ou utiliser l'API:

```bash
curl -X POST http://localhost:8083/api/documents/ingest \
  -F "file=@guide-virements.pdf" \
  -F "category=virements" \
  -F "description=Guide des virements bancaires"
```

### 3. Utiliser l'API REST

**Envoyer un message:**
```bash
curl -X POST http://localhost:8083/api/chatbot/message \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "message": "Comment faire un virement instantané?"
  }'
```

**Streaming:**
```bash
curl -N http://localhost:8083/api/chatbot/message/stream \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "message": "Liste de mes bénéficiaires"
  }'
```

**Analyser une image:**
```bash
curl -X POST http://localhost:8083/api/chatbot/image \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "message": "Extraire les informations de ce RIB",
    "imageUrl": "https://example.com/rib.jpg"
  }'
```

### 4. Utiliser via Telegram

1. Chercher votre bot sur Telegram
2. Démarrer: `/start`
3. Poser des questions en texte
4. Envoyer des photos de documents

**Commandes:**
- `/start` - Démarrer
- `/help` - Aide
- `/clear` - Effacer l'historique

## 🧪 Tests

### Tester le RAG

```java
@Test
public void testDocumentRetrieval() {
    List<String> docs = retrievalService.retrieveRelevantDocuments(
        "Comment créer un virement?"
    );
    assertFalse(docs.isEmpty());
}
```

### Tester les Tools MCP

```java
@Test
public void testBeneficiaireTools() {
    String result = beneficiaireTools.getAllBeneficiaires().apply(null);
    assertTrue(result.contains("Liste des bénéficiaires"));
}
```

## 📊 Flux de traitement

### Requête simple
```
1. Utilisateur → Question
2. RAG → Récupération documents pertinents
3. ChatService → Construction prompt avec contexte
4. GPT-4o → Génération réponse
5. Utilisateur ← Réponse
```

### Requête avec outil MCP
```
1. Utilisateur → "Liste mes bénéficiaires"
2. GPT-4o → Décide d'utiliser tool getAllBeneficiaires()
3. Tool → Appel Feign vers beneficiaire-service
4. Beneficiaire-service → Retour données
5. GPT-4o → Formulation réponse naturelle
6. Utilisateur ← "Vous avez 3 bénéficiaires: ..."
```

### Analyse image
```
1. Utilisateur → Photo RIB + Question
2. GPT-4o Vision → Extraction texte
3. RAG → Enrichissement contexte
4. GPT-4o → Interprétation + Réponse
5. Utilisateur ← Informations extraites
```

## 🔐 Sécurité

- **API Key**: OpenAI key en variable d'environnement
- **Feign**: Timeouts configurés
- **Validation**: Inputs validés
- **Rate Limiting**: À configurer sur Gateway

## 📈 Monitoring

### Actuator endpoints
```bash
curl http://localhost:8083/actuator/health
curl http://localhost:8083/actuator/metrics
```

### Logs
Les logs incluent:
- Requêtes/réponses chatbot
- Appels tools MCP
- Recherches RAG
- Erreurs LLM

## 🐛 Troubleshooting

### ChromaDB non accessible
```
Error: Connection refused to http://localhost:8000
→ Démarrer ChromaDB: docker run -d -p 8000:8000 chromadb/chroma
```

### OpenAI API erreur 401
```
Error: Incorrect API key
→ Vérifier OPENAI_API_KEY
```

### Tools MCP erreur
```
Error: beneficiaire-service unavailable
→ Vérifier que les services sont enregistrés dans Eureka
```

### Documents non trouvés (RAG)
```
→ Ingérer des documents PDF dans le système
→ Vérifier que ChromaDB contient des embeddings
```

## 🚀 Améliorations futures

- [ ] Support Ollama + Llama3 (local LLM)
- [ ] Fine-tuning sur données bancaires
- [ ] Cache réponses fréquentes
- [ ] Analytics conversations
- [ ] Support multilingue
- [ ] Intégration WhatsApp
- [ ] Voice input/output

## 📚 Ressources

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [OpenAI API](https://platform.openai.com/docs)
- [ChromaDB](https://www.trychroma.com/)
- [Telegram Bot API](https://core.telegram.org/bots/api)

## 📝 License

Propriétaire - Banking System POC
