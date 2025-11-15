# Configuration Azure OpenAI pour le Chatbot

## 1. Déploiements Requis

Vous devez avoir **2 déploiements** sur Azure OpenAI :

### ✅ Déjà configuré :
- **Chat Model** : `gpt-4.1` (déployé)

### ⚠️ À configurer :
- **Embedding Model** : Déployer `text-embedding-3-small` ou `text-embedding-ada-002`

## 2. Créer le Déploiement d'Embeddings

### Via le Portail Azure :

1. Allez sur https://portal.azure.com
2. Recherchez votre ressource : `mail-sender-resource`
3. Cliquez sur **"Model deployments"** → **"Manage Deployments"**
4. Cliquez sur **"Create new deployment"**
5. Configurez :
   - **Model** : `text-embedding-3-small` (recommandé) ou `text-embedding-ada-002`
   - **Deployment name** : `text-embedding-3-small`
   - **Deployment type** : Standard
6. Cliquez sur **"Create"**

## 3. Configuration des Variables d'Environnement

Une fois les déploiements créés, configurez :

```bash
# Azure OpenAI
export AZURE_OPENAI_API_KEY="votre-clé-ici"
export AZURE_OPENAI_ENDPOINT="https://mail-sender-resource.cognitiveservices.azure.com/"

# Azure Document Intelligence (optionnel)
export AZURE_DOCUMENT_INTELLIGENCE_ENABLED=false
export AZURE_DOCUMENT_INTELLIGENCE_ENDPOINT="https://mail-sender-resource.cognitiveservices.azure.com/"
export AZURE_DOCUMENT_INTELLIGENCE_KEY="votre-clé-ici"

# ChromaDB
export CHROMA_HOST="http://localhost:8000"
```

## 4. Mettre à Jour application.yml

Le fichier `application.yml` a déjà été configuré avec :

```yaml
spring:
  ai:
    azure:
      openai:
        api-key: ${AZURE_OPENAI_API_KEY}
        endpoint: ${AZURE_OPENAI_ENDPOINT}
        chat:
          options:
            deployment-name: gpt-4.1
            temperature: 0.7
            max-tokens: 500
        embedding:
          options:
            deployment-name: text-embedding-3-small
```

**Important** : Si vous choisissez un nom différent pour votre déploiement d'embeddings, mettez à jour `deployment-name` dans `embedding.options`.

## 5. Démarrage

```bash
# 1. Démarrer ChromaDB
docker run -d -p 8000:8000 chromadb/chroma:0.4.24

# 2. Configurer les variables d'environnement
export AZURE_OPENAI_API_KEY="votre-clé"
export AZURE_OPENAI_ENDPOINT="https://mail-sender-resource.cognitiveservices.azure.com/"

# 3. Démarrer le chatbot
cd chatbot-service
mvn spring-boot:run
```

## 6. Test

```bash
curl -X POST http://localhost:8083/api/chatbot/message \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "message": "Bonjour, comment faire un virement ?"
  }'
```

## 7. Différences Azure OpenAI vs OpenAI

| Fonctionnalité | OpenAI | Azure OpenAI |
|----------------|--------|--------------|
| **Endpoint** | api.openai.com | votre-ressource.cognitiveservices.azure.com |
| **Authentication** | API Key simple | API Key + Endpoint |
| **Modèles** | Noms directs (gpt-4o) | Noms de déploiements (gpt-4.1) |
| **Régions** | Global | Régions Azure spécifiques |
| **Pricing** | Pay-per-token | Azure pricing + commitments |
| **Compliance** | OpenAI ToS | Azure Enterprise Agreement |

## 8. Avantages Azure OpenAI

✅ **Sécurité Entreprise** : VNet, Private Endpoints, Managed Identity
✅ **Conformité** : RGPD, ISO, SOC 2
✅ **SLA** : 99.9% uptime garantie
✅ **Intégration Azure** : Key Vault, Monitor, RBAC
✅ **Support Microsoft** : Support technique inclus

## 9. Coûts Estimés

### GPT-4.1 (gpt-4)
- **Input** : ~$0.03 / 1K tokens
- **Output** : ~$0.06 / 1K tokens

### Text Embedding 3 Small
- **Input** : ~$0.0001 / 1K tokens

### Exemple mensuel :
- 10,000 messages × 100 tokens input = 1M tokens → **$30**
- 10,000 messages × 200 tokens output = 2M tokens → **$120**
- 10,000 embeddings × 50 tokens = 500K tokens → **$0.05**
- **Total** : ~$150/mois

## 10. Dépannage

### Erreur : "Deployment not found"
→ Vérifiez que le nom du déploiement correspond exactement dans application.yml

### Erreur : "Unauthorized"
→ Vérifiez votre clé API dans les variables d'environnement

### Erreur : "Quota exceeded"
→ Augmentez les quotas dans Azure Portal → Quotas

### Logs utiles
```bash
# Activer les logs détaillés
export LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_AI=DEBUG
mvn spring-boot:run
```

## 11. Alternative : Sans Embedding Model

Si vous ne voulez pas déployer un modèle d'embeddings, vous pouvez désactiver temporairement le RAG :

```yaml
# application.yml
chatbot:
  rag:
    enabled: false  # Désactive le RAG
```

Mais le chatbot fonctionnera sans contexte documentaire.
