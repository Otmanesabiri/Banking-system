# Guide de Démarrage - Chatbot Service

## Démarrage Rapide

### 1. Prérequis

Assurez-vous que les services suivants sont démarrés:

```bash
# Discovery Service (Eureka)
cd discovery-service
mvn spring-boot:run

# Config Service
cd config-service
mvn spring-boot:run

# Gateway Service
cd gateway-service
mvn spring-boot:run

# Beneficiaire Service
cd beneficiaire-service
mvn spring-boot:run

# Virement Service
cd virement-service
mvn spring-boot:run
```

### 2. Démarrer ChromaDB (Vector Store)

```bash
docker run -d --name chromadb -p 8000:8000 chromadb/chroma
```

### 3. Configuration

Créez un fichier `.env` à la racine du projet:

```bash
export OPENAI_API_KEY=sk-your-actual-openai-api-key
export CHROMA_HOST=http://localhost:8000
export TELEGRAM_BOT_ENABLED=false
```

Sourcez le fichier:
```bash
source .env
```

### 4. Démarrer le Chatbot Service

```bash
cd chatbot-service
mvn clean install
mvn spring-boot:run
```

Le service démarre sur le port **8083**.

## Tester le Service

### Via Swagger UI

Ouvrez votre navigateur: http://localhost:8083/swagger-ui.html

Vous verrez toutes les API disponibles avec documentation interactive.

### Tester l'ingestion de documents

Si vous avez des PDFs, placez-les dans `documents/` puis utilisez l'API:

```bash
curl -X POST http://localhost:8083/api/documents/ingest \
  -F "file=@documents/guide-virements.pdf" \
  -F "category=virements" \
  -F "description=Guide des virements"
```

### Tester le chatbot

**Question simple:**
```bash
curl -X POST http://localhost:8083/api/chatbot/message \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user",
    "message": "Comment faire un virement instantané?"
  }'
```

**Réponse attendue:**
```json
{
  "sessionId": "uuid-generated",
  "message": "Pour faire un virement instantané:\n1. Sélectionnez le bénéficiaire...",
  "timestamp": "2025-11-14T15:30:00",
  "success": true
}
```

**Question utilisant les tools MCP:**
```bash
curl -X POST http://localhost:8083/api/chatbot/message \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user",
    "message": "Montre-moi la liste de mes bénéficiaires"
  }'
```

Le chatbot va appeler automatiquement le tool `getAllBeneficiaires()` via Feign.

### Tester le streaming

```bash
curl -N http://localhost:8083/api/chatbot/message/stream \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user",
    "message": "Explique-moi comment créer un bénéficiaire"
  }'
```

Vous verrez la réponse arriver mot par mot en temps réel.

## Configuration Telegram (Optionnel)

### 1. Créer un bot Telegram

1. Ouvrez Telegram et cherchez **@BotFather**
2. Envoyez `/newbot`
3. Suivez les instructions
4. Copiez le token fourni

### 2. Configurer le service

```bash
export TELEGRAM_BOT_ENABLED=true
export TELEGRAM_BOT_TOKEN=123456789:ABCdefGHIjklMNOpqrsTUVwxyz
export TELEGRAM_BOT_USERNAME=YourBankBot
```

### 3. Redémarrer le service

```bash
mvn spring-boot:run
```

### 4. Utiliser le bot

1. Cherchez votre bot sur Telegram
2. Démarrez: `/start`
3. Posez vos questions!

**Exemples:**
- "Liste mes bénéficiaires"
- "Comment faire un virement?"
- [Envoyer photo RIB] + "Extraire les infos"

## Vérifications

### Santé du service

```bash
curl http://localhost:8083/actuator/health
```

### Eureka

Vérifiez que le service est enregistré: http://localhost:8761

### Documents ingérés

```bash
curl http://localhost:8083/api/documents
```

### Historique conversation

```bash
curl http://localhost:8083/api/chatbot/history/your-session-id
```

## Résolution de problèmes

### Erreur: Connection refused ChromaDB

```bash
# Vérifier que ChromaDB tourne
docker ps | grep chroma

# Si non démarré
docker start chromadb
```

### Erreur: OpenAI API key invalid

```bash
# Vérifier la variable
echo $OPENAI_API_KEY

# Doit commencer par sk-
```

### Erreur: Service beneficiaire-service unavailable

Vérifiez que le service est démarré et enregistré dans Eureka:
```bash
curl http://localhost:8761/eureka/apps
```

### Pas de documents trouvés (RAG)

```bash
# Vérifier les documents dans la base
curl http://localhost:8083/api/documents

# Si vide, ingérer manuellement
curl -X POST http://localhost:8083/api/documents/ingest \
  -F "file=@your-document.pdf" \
  -F "category=general"
```

## Architecture du RAG

Le pipeline RAG fonctionne ainsi:

```
Question utilisateur
    ↓
[Embedding de la question]
    ↓
[Recherche similarité dans ChromaDB]
    ↓
[Top-5 documents pertinents]
    ↓
[Construction prompt avec contexte]
    ↓
[Appel GPT-4o]
    ↓
[Génération réponse]
    ↓
Réponse utilisateur
```

## Logs

Les logs détaillés sont dans la console:

```
2025-11-14 15:30:00 - Received chat request from user: test-user
2025-11-14 15:30:01 - Retrieved 5 relevant documents
2025-11-14 15:30:02 - Tool called: getAllBeneficiaires
2025-11-14 15:30:03 - Generated response for session abc-123
```

## Métriques

Actuator expose des métriques:

```bash
curl http://localhost:8083/actuator/metrics

# Métriques spécifiques
curl http://localhost:8083/actuator/metrics/http.server.requests
```

## Base de données H2

Console H2: http://localhost:8083/h2-console

```
JDBC URL: jdbc:h2:mem:chatbotdb
Username: sa
Password: (vide)
```

Tables:
- `chat_session`
- `chat_message`
- `bank_document`
- `document_chunk`

## Performance

**Temps de réponse typiques:**
- Question simple (sans RAG): 1-2s
- Question avec RAG: 2-3s
- Question avec tool MCP: 2-4s
- Analyse image: 3-5s

**Optimisations:**
- Cache des embeddings
- Pagination historique
- Connection pooling Feign
- Timeout configurés

## Production

Pour la production:

1. **Base de données**: Remplacer H2 par PostgreSQL
2. **Vector Store**: ChromaDB persistant ou Pinecone
3. **Secrets**: Utiliser Vault
4. **Monitoring**: Prometheus + Grafana
5. **Rate Limiting**: Sur Gateway
6. **Logging**: ELK Stack

## Support

- Documentation: [README.md](README.md)
- Architecture: [docs/architecture-technique.md](../docs/architecture-technique.md)
- Issues: GitHub Issues
