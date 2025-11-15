# Guide d'Utilisation - Azure AI Document Intelligence

## Vue d'ensemble

L'intégration d'Azure AI Document Intelligence améliore considérablement les capacités d'extraction de documents du chatbot. Ce service offre :

- **Extraction de texte avancée** : Préserve la structure et la mise en page
- **Extraction de tableaux** : Identifie et structure les tableaux automatiquement
- **Extraction de paires clé-valeur** : Trouve automatiquement les champs importants
- **Analyse de factures** : Extrait les champs structurés (montant, date, vendeur, etc.)
- **Support multiformat** : PDF, images (JPEG, PNG), TIFF

## Configuration

### 1. Obtenir les Credentials Azure

Vous avez déjà une ressource Azure AI Document Intelligence :
- **Nom** : mail-sender-resource
- **Endpoint** : https://mail-sender-resource.cognitiveservices.azure.com/
- **Clé** : (voir le portail Azure)

### 2. Configurer les Variables d'Environnement

Créez un fichier `.env` à la racine de `chatbot-service` :

```bash
# Azure Document Intelligence
AZURE_DOCUMENT_INTELLIGENCE_ENABLED=true
AZURE_DOCUMENT_INTELLIGENCE_ENDPOINT=https://mail-sender-resource.cognitiveservices.azure.com/
AZURE_DOCUMENT_INTELLIGENCE_KEY=votre-clé-primaire-ici

# OpenAI (déjà configuré)
OPENAI_API_KEY=sk-...

# ChromaDB (déjà configuré)
CHROMA_HOST=http://localhost:8000
```

### 3. Charger les Variables d'Environnement

**Option A - Manuellement** :
```bash
export AZURE_DOCUMENT_INTELLIGENCE_ENABLED=true
export AZURE_DOCUMENT_INTELLIGENCE_ENDPOINT=https://mail-sender-resource.cognitiveservices.azure.com/
export AZURE_DOCUMENT_INTELLIGENCE_KEY=votre-clé-ici
```

**Option B - Avec dotenv (recommandé)** :
```bash
# Installer dotenv-cli
npm install -g dotenv-cli

# Lancer le service avec .env
dotenv -e .env mvn spring-boot:run
```

## Utilisation

### Mode Automatique (RAG Pipeline)

Une fois configuré, le service utilise automatiquement Azure AI pour tous les documents ingérés :

```bash
# 1. Démarrer ChromaDB
docker run -d -p 8000:8000 chromadb/chroma

# 2. Démarrer le service
cd chatbot-service
mvn spring-boot:run

# 3. Ingérer un document (il utilisera automatiquement Azure AI)
curl -X POST http://localhost:8083/api/documents/ingest \
  -F "file=@votre-facture.pdf" \
  -F "category=factures" \
  -F "description=Facture test"
```

### Endpoints API Spécifiques Azure AI

#### 1. Vérifier le Statut

```bash
curl http://localhost:8083/api/azure-document/status
```

Réponse :
```json
{
  "enabled": true,
  "service": "Azure AI Document Intelligence",
  "capabilities": [
    "Text extraction",
    "Table extraction",
    "Key-value pair extraction",
    "Layout analysis",
    "Invoice analysis",
    "Receipt analysis"
  ]
}
```

#### 2. Analyser un Document Général

```bash
curl -X POST http://localhost:8083/api/azure-document/analyze \
  -F "file=@document.pdf"
```

Réponse :
```json
{
  "success": true,
  "filename": "document.pdf",
  "pages_analyzed": 3,
  "documents": [
    {
      "id": "uuid-123",
      "content": "Texte extrait avec tableaux formatés...",
      "metadata": {
        "page_number": 1,
        "has_tables": true,
        "table_count": 2
      }
    }
  ]
}
```

#### 3. Analyser une Facture

```bash
curl -X POST http://localhost:8083/api/azure-document/analyze-invoice \
  -F "file=@facture.pdf"
```

Réponse :
```json
{
  "success": true,
  "filename": "facture.pdf",
  "invoice_data": {
    "InvoiceId": "FAC-2025-001",
    "InvoiceDate": "2025-11-14",
    "DueDate": "2025-12-14",
    "VendorName": "Entreprise ABC",
    "CustomerName": "Client XYZ",
    "InvoiceTotal": "1250.00 EUR",
    "AmountDue": "1250.00 EUR"
  }
}
```

#### 4. Prévisualiser le Texte Extrait

```bash
curl -X POST http://localhost:8083/api/azure-document/preview \
  -F "file=@document.pdf"
```

## Comparaison avec PDFBox

| Fonctionnalité | PDFBox (Baseline) | Azure AI Document Intelligence |
|----------------|-------------------|-------------------------------|
| **Texte simple** | ✅ Bon | ✅ Excellent |
| **Tableaux** | ❌ Non structuré | ✅ Formaté en Markdown |
| **Colonnes** | ❌ Ordre incorrect | ✅ Ordre préservé |
| **Images scannées** | ❌ Pas d'OCR | ✅ OCR intégré |
| **Factures** | ❌ Extraction manuelle | ✅ Champs auto-détectés |
| **Performance** | ✅ Rapide | ⚠️ ~2-5s par page |
| **Coût** | ✅ Gratuit | 💰 Pay-per-page |

## Exemples d'Utilisation avec le Chatbot

### Scénario 1 : Analyser un RIB

1. **Ingérer le RIB** :
```bash
curl -X POST http://localhost:8083/api/documents/ingest \
  -F "file=@rib-client.pdf" \
  -F "category=rib" \
  -F "description=RIB du client"
```

2. **Poser des questions** :
```bash
curl -X POST http://localhost:8083/api/chatbot/message \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "message": "Quel est le numéro IBAN sur le RIB ?"
  }'
```

Le chatbot peut maintenant extraire précisément le numéro IBAN grâce à Azure AI.

### Scénario 2 : Comparer des Factures

1. **Ingérer plusieurs factures** :
```bash
curl -X POST http://localhost:8083/api/documents/ingest \
  -F "file=@facture-janvier.pdf" -F "category=factures"

curl -X POST http://localhost:8083/api/documents/ingest \
  -F "file=@facture-fevrier.pdf" -F "category=factures"
```

2. **Poser des questions** :
```bash
curl -X POST http://localhost:8083/api/chatbot/message \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "message": "Quelle est la différence de montant entre les factures de janvier et février ?"
  }'
```

### Scénario 3 : Extraire des Tableaux

Si vos documents contiennent des tableaux complexes (tarifs, données financières), Azure AI les extrait automatiquement en format Markdown :

```markdown
| Produit | Quantité | Prix Unitaire | Total |
|---------|----------|---------------|-------|
| Item A | 10 | 25.00 | 250.00 |
| Item B | 5 | 50.00 | 250.00 |
```

## Fonctionnalités Avancées

### 1. Modèles Personnalisés

Si vous avez des documents spécifiques à votre domaine, vous pouvez entraîner un modèle personnalisé :

```java
// Dans AzureDocumentIntelligenceService
AnalyzeResult result = analyzeDocumentWithModel(resource, "votre-modele-custom");
```

### 2. Classification de Documents

Azure AI peut automatiquement classifier vos documents :

```java
// Détecte automatiquement : facture, reçu, contrat, etc.
AnalyzeResult result = analyzeDocumentWithModel(resource, "prebuilt-document");
```

## Dépannage

### Erreur : "Azure Document Intelligence is not enabled"

**Solution** : Vérifiez que les variables d'environnement sont bien configurées :
```bash
echo $AZURE_DOCUMENT_INTELLIGENCE_ENABLED
echo $AZURE_DOCUMENT_INTELLIGENCE_ENDPOINT
```

### Erreur : "Unauthorized" ou "Invalid key"

**Solution** : Vérifiez votre clé Azure dans le portail :
1. Allez sur https://portal.azure.com
2. Recherchez "mail-sender-resource"
3. Allez dans "Keys and Endpoint"
4. Copiez la clé primaire

### Fallback sur PDFBox

Si Azure AI n'est pas configuré, le service utilise automatiquement PDFBox comme fallback :

```
[INFO] Azure Document Intelligence service disabled or not configured
[INFO] Using Spring AI PDF reader for extraction
```

## Coûts

Azure AI Document Intelligence est payant :
- **Prebuilt-layout** : ~$0.01 par page
- **Prebuilt-invoice** : ~$0.03 par page
- **Modèle personnalisé** : ~$0.05 par page

**Estimation mensuelle** :
- 100 documents/mois × 5 pages = 500 pages
- 500 × $0.01 = **$5/mois**

## Swagger UI

Testez les endpoints dans l'interface Swagger :

```
http://localhost:8083/swagger-ui.html
```

Section : **Azure Document Intelligence**

## Recommandations

1. **Production** : Activez Azure AI pour meilleure qualité
2. **Développement** : Utilisez PDFBox pour économiser
3. **Documents complexes** : Utilisez Azure AI (tableaux, colonnes)
4. **Documents simples** : PDFBox suffit

## Support

- **Documentation Azure** : https://learn.microsoft.com/azure/ai-services/document-intelligence/
- **Exemples de code** : https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/formrecognizer
- **Tarification** : https://azure.microsoft.com/pricing/details/form-recognizer/
