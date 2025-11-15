# Architecture Fonctionnelle - Système Bancaire Micro-services

## 1. Vue d'ensemble

Système bancaire distribué permettant la gestion des virements et bénéficiaires avec assistance IA.

## 2. Acteurs

### 2.1 Utilisateurs
- **Client Web**: Accède via navigateur (Angular/React)
- **Client Mobile**: Accède via application Flutter
- **Opérateur DevOps**: Gère le déploiement et monitoring

### 2.2 Systèmes
- Micro-services métiers
- Services techniques (Gateway, Discovery, Config)
- Chatbot IA (Assistant intelligent)

## 3. Domaines Métier (DDD)

### 3.1 Domaine Bénéficiaires
**Responsabilité**: Gestion du référentiel des bénéficiaires

**Entités**:
- Bénéficiaire
  - id: Long
  - nom: String
  - prenom: String
  - rib: String (format IBAN)
  - type: Enum (PHYSIQUE, MORALE)
  - dateCreation: LocalDateTime
  - actif: Boolean

**Cas d'usage**:
- UC1: Créer un bénéficiaire
- UC2: Modifier un bénéficiaire
- UC3: Supprimer un bénéficiaire
- UC4: Consulter un bénéficiaire
- UC5: Lister les bénéficiaires (pagination)
- UC6: Rechercher bénéficiaires par critères

**Règles métier**:
- RIB doit être unique
- Validation format IBAN
- Type obligatoire
- Nom et prénom requis pour type PHYSIQUE
- Raison sociale requise pour type MORALE

### 3.2 Domaine Virements
**Responsabilité**: Gestion des opérations de virement

**Entités**:
- Virement
  - id: Long
  - beneficiaireId: Long (référence externe)
  - ribSource: String
  - montant: BigDecimal
  - description: String
  - dateVirement: LocalDateTime
  - type: Enum (NORMAL, INSTANTANE)
  - statut: Enum (EN_ATTENTE, VALIDE, REJETE, EXECUTE)

**Cas d'usage**:
- UC7: Créer un virement
- UC8: Valider un virement
- UC9: Exécuter un virement
- UC10: Annuler un virement
- UC11: Consulter historique virements
- UC12: Rechercher virements par période/bénéficiaire

**Règles métier**:
- Montant > 0
- Bénéficiaire doit exister
- RIB source doit être valide
- Virement instantané: exécution immédiate
- Virement normal: délai 24-48h
- Limite montant virement instantané: 15 000 €

### 3.3 Domaine Assistance IA
**Responsabilité**: Fournir assistance intelligente aux utilisateurs

**Fonctionnalités**:
- Répondre aux questions sur les services bancaires
- Expliquer les procédures de virement
- Assister dans la création de bénéficiaires
- Fournir informations sur les comptes
- Interpréter documents bancaires (factures, RIB)

**Cas d'usage**:
- UC13: Poser une question textuelle
- UC14: Soumettre un document à analyser
- UC15: Obtenir aide contextuelle
- UC16: Consulter historique conversation

**Règles métier**:
- Réponses basées uniquement sur documents banque
- Pas de divulgation d'informations sensibles
- Réponse "Je ne sais pas" si hors contexte
- Support multimodal (texte + images)

## 4. Flux Métier Principaux

### 4.1 Flux: Création Virement avec Validation

```
1. Client saisit informations virement
2. Frontend valide format données
3. Gateway route vers virement-service
4. Virement-service appelle beneficiaire-service (vérification existence)
5. Si bénéficiaire existe:
   a. Validation règles métier (montant, RIB)
   b. Création virement statut EN_ATTENTE
   c. Si type INSTANTANE: validation immédiate
6. Retour réponse client
7. Notification utilisateur
```

### 4.2 Flux: Question au Chatbot

```
1. Utilisateur pose question (Telegram/Web)
2. Gateway route vers chatbot-service
3. Chatbot-service:
   a. Analyse intention question
   b. Retrieval documents pertinents (RAG)
   c. Appel LLM avec contexte
   d. Génération réponse
4. Si besoin d'informations métier:
   a. Appel tools MCP (beneficiaire/virement service)
   b. Enrichissement contexte
5. Retour réponse enrichie
6. Affichage utilisateur
```

### 4.3 Flux: Analyse Document Bancaire

```
1. Utilisateur envoie photo facture/RIB (Telegram)
2. Gateway route vers chatbot-service
3. Chatbot-service:
   a. Extraction texte via GPT-4o (vision)
   b. Identification type document
   c. Parsing informations clés (RIB, montant, etc.)
4. Si création bénéficiaire suggérée:
   a. Pré-remplissage formulaire
   b. Confirmation utilisateur
   c. Appel beneficiaire-service
5. Retour confirmation
```

## 5. Exigences Non-Fonctionnelles

### 5.1 Performance
- Temps réponse API < 500ms (hors IA)
- Temps réponse chatbot < 3s
- Support 100 utilisateurs concurrents

### 5.2 Disponibilité
- SLA 99.5%
- Récupération automatique (resilience)
- Circuit breakers entre services

### 5.3 Sécurité
- Authentification OAuth2/JWT
- Chiffrement TLS
- Validation entrées
- Rate limiting sur API

### 5.4 Scalabilité
- Services stateless
- Scalabilité horizontale
- Load balancing automatique

## 6. Contraintes

- POC: données en mémoire (H2) acceptable
- Production: base données relationnelle requise
- Documents banque: format PDF
- Chatbot: modèle GPT-4o ou Llama3
- Interface mobile: Flutter
- Frontend web: React ou Angular

## 7. Livrables Attendus

1. Micro-services fonctionnels
2. Frontend web responsive
3. Application mobile
4. Documentation API (OpenAPI)
5. Pipeline DevOps
6. Documentation architecture
7. Démo fonctionnelle
