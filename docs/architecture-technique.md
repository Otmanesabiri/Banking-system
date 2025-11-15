# Architecture Technique - Système Bancaire Micro-services

## 1. Vue d'ensemble Architecture

### 1.1 Diagramme de Composants

```
┌─────────────────────────────────────────────────────────────┐
│                    Clients                                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                  │
│  │   Web    │  │  Mobile  │  │ Telegram │                  │
│  │(React/   │  │(Flutter) │  │   Bot    │                  │
│  │ Angular) │  │          │  │          │                  │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘                  │
└───────┼─────────────┼─────────────┼────────────────────────┘
        │             │             │
        │             │             │
        └─────────────┴─────────────┘
                      │
        ┌─────────────▼──────────────┐
        │    API Gateway              │
        │  (Spring Cloud Gateway)     │
        │  - Routing                  │
        │  - Load Balancing           │
        │  - Authentication           │
        │  - Rate Limiting            │
        └─────────────┬───────────────┘
                      │
        ┌─────────────▼──────────────┐
        │   Service Discovery         │
        │   (Eureka Server)           │
        │   - Service Registry        │
        │   - Health Checks           │
        └─────────────┬───────────────┘
                      │
        ┌─────────────▼──────────────┐
        │   Config Server             │
        │   (Spring Cloud Config)     │
        │   - Centralized Config      │
        │   - Git Backend             │
        └─────────────────────────────┘
                      │
        ┌─────────────┴─────────────┬─────────────┬─────────────┐
        │                           │             │             │
┌───────▼──────────┐   ┌───────────▼──────┐   ┌─▼──────────┐ │
│  Beneficiaire    │   │   Virement       │   │  Chatbot   │ │
│    Service       │   │    Service       │   │  Service   │ │
│  ┌────────────┐  │   │  ┌────────────┐  │   │ ┌────────┐ │ │
│  │ REST API   │  │   │  │ REST API   │  │   │ │SpringAI│ │ │
│  │ JPA/H2     │  │◄──┤  │ JPA/H2     │  │   │ │RAG     │ │ │
│  │ Validation │  │   │  │ Feign      │  │   │ │GPT-4o  │ │ │
│  └────────────┘  │   │  └────────────┘  │   │ │Vector  │ │ │
│                  │   │                  │   │ │Store   │ │ │
└──────────────────┘   └──────────────────┘   │ │MCP     │ │ │
                                              │ │Tools   │ │ │
                                              └─┴────────┴─┘ │
                                                    ▲         │
                                                    │         │
                                                    └─────────┘
```

## 2. Technologies Stack

### 2.1 Backend
- **Framework**: Spring Boot 3.2.0
- **Java Version**: 21 (LTS)
- **Build Tool**: Maven 3.8+
- **Service Discovery**: Eureka Server
- **API Gateway**: Spring Cloud Gateway (Reactive)
- **Config Management**: Spring Cloud Config
- **API Documentation**: SpringDoc OpenAPI 3 (Swagger)

### 2.2 IA & Chatbot
- **AI Framework**: Spring AI
- **LLM**: OpenAI GPT-4o (multimodal)
- **Alternative LLM**: Ollama + Llama3
- **RAG**: Vector embeddings + retrieval
- **Vector Store**: ChromaDB / Pinecone / Qdrant
- **Embeddings**: OpenAI text-embedding-3-small
- **Document Processing**: Apache PDFBox
- **Chat Interface**: Telegram Bot API

### 2.3 Persistence
- **Database (POC)**: H2 (in-memory)
- **Database (Prod)**: PostgreSQL 15+
- **ORM**: Spring Data JPA / Hibernate
- **Migrations**: Flyway / Liquibase

### 2.4 Communication
- **Inter-service**: OpenFeign (REST)
- **MCP Protocol**: Streamable HTTP
- **Message Format**: JSON
- **API Style**: RESTful

### 2.5 DevOps
- **Containerization**: Docker
- **Orchestration**: Docker Compose (dev), Kubernetes (prod)
- **CI/CD**: Jenkins / GitHub Actions
- **Monitoring**: Spring Boot Actuator, Prometheus, Grafana
- **Logging**: Logback + ELK Stack

## 3. Architecture Micro-services

### 3.1 Discovery Service (Eureka)

**Port**: 8761

**Responsabilités**:
- Enregistrement services
- Health checks
- Service discovery dynamique

**Configuration**:
```yaml
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  server:
    enable-self-preservation: false
```

### 3.2 Config Service

**Port**: 8888

**Responsabilités**:
- Configuration centralisée
- Support profils (dev, prod)
- Refresh dynamique

**Backend**: Git repository

### 3.3 Gateway Service

**Port**: 8080

**Responsabilités**:
- Point d'entrée unique
- Routing dynamique
- Load balancing
- Authentication/Authorization
- Rate limiting
- CORS handling

**Routes**:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: beneficiaire-service
          uri: lb://BENEFICIAIRE-SERVICE
          predicates:
            - Path=/api/beneficiaires/**
        
        - id: virement-service
          uri: lb://VIREMENT-SERVICE
          predicates:
            - Path=/api/virements/**
        
        - id: chatbot-service
          uri: lb://CHATBOT-SERVICE
          predicates:
            - Path=/api/chatbot/**
```

### 3.4 Beneficiaire Service

**Port**: 8084

**Responsabilités**:
- CRUD bénéficiaires
- Validation RIB
- Recherche/filtrage

**API Endpoints**:
```
GET    /api/beneficiaires
GET    /api/beneficiaires/{id}
POST   /api/beneficiaires
PUT    /api/beneficiaires/{id}
DELETE /api/beneficiaires/{id}
GET    /api/beneficiaires/search?nom={nom}
```

**Base de données**:
```sql
CREATE TABLE beneficiaire (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    rib VARCHAR(34) UNIQUE NOT NULL,
    type VARCHAR(20) NOT NULL,
    date_creation TIMESTAMP,
    actif BOOLEAN DEFAULT true
);
```

### 3.5 Virement Service

**Port**: 8082

**Responsabilités**:
- CRUD virements
- Validation montant/RIB
- Appel beneficiaire-service (Feign)
- Gestion statuts

**API Endpoints**:
```
GET    /api/virements
GET    /api/virements/{id}
POST   /api/virements
PUT    /api/virements/{id}/valider
PUT    /api/virements/{id}/executer
DELETE /api/virements/{id}
GET    /api/virements/beneficiaire/{beneficiaireId}
```

**Base de données**:
```sql
CREATE TABLE virement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    beneficiaire_id BIGINT NOT NULL,
    rib_source VARCHAR(34) NOT NULL,
    montant DECIMAL(15,2) NOT NULL,
    description VARCHAR(500),
    date_virement TIMESTAMP,
    type VARCHAR(20) NOT NULL,
    statut VARCHAR(20) NOT NULL
);
```

**Communication inter-service**:
```java
@FeignClient(name = "beneficiaire-service")
public interface BeneficiaireClient {
    @GetMapping("/api/beneficiaires/{id}")
    BeneficiaireDTO getBeneficiaire(@PathVariable Long id);
}
```

### 3.6 Chatbot Service

**Port**: 8083

**Responsabilités**:
- Interface conversationnelle
- RAG (Retrieval Augmented Generation)
- Intégration Telegram
- Appel LLM
- Gestion contexte conversation
- Accès outils métier (MCP)

**Architecture Interne**:

```
┌─────────────────────────────────────────┐
│         Chatbot Service                 │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │   Telegram Bot Interface         │  │
│  │   (TelegramLongPollingBot)       │  │
│  └────────────┬─────────────────────┘  │
│               │                         │
│  ┌────────────▼─────────────────────┐  │
│  │   Chat Controller                │  │
│  │   - Message routing              │  │
│  │   - Session management           │  │
│  └────────────┬─────────────────────┘  │
│               │                         │
│  ┌────────────▼─────────────────────┐  │
│  │   Agent AI (Spring AI)           │  │
│  │   ┌──────────────────────────┐   │  │
│  │   │ ChatClient               │   │  │
│  │   │ - GPT-4o integration     │   │  │
│  │   │ - Prompt management      │   │  │
│  │   └──────────────────────────┘   │  │
│  │   ┌──────────────────────────┐   │  │
│  │   │ ChatMemory               │   │  │
│  │   │ - Conversation history   │   │  │
│  │   │ - Context retention      │   │  │
│  │   └──────────────────────────┘   │  │
│  │   ┌──────────────────────────┐   │  │
│  │   │ Tools (MCP Protocol)     │   │  │
│  │   │ - BeneficiaireTools      │   │  │
│  │   │ - VirementTools          │   │  │
│  │   └──────────────────────────┘   │  │
│  └────────────┬─────────────────────┘  │
│               │                         │
│  ┌────────────▼─────────────────────┐  │
│  │   RAG Pipeline                   │  │
│  │   ┌──────────────────────────┐   │  │
│  │   │ PDF Ingestion            │   │  │
│  │   │ - Text extraction        │   │  │
│  │   │ - Chunking               │   │  │
│  │   └──────────────────────────┘   │  │
│  │   ┌──────────────────────────┐   │  │
│  │   │ Embedding Service        │   │  │
│  │   │ - OpenAI embeddings      │   │  │
│  │   └──────────────────────────┘   │  │
│  │   ┌──────────────────────────┐   │  │
│  │   │ Vector Store             │   │  │
│  │   │ - ChromaDB/Qdrant        │   │  │
│  │   │ - Similarity search      │   │  │
│  │   └──────────────────────────┘   │  │
│  │   ┌──────────────────────────┐   │  │
│  │   │ Retrieval Service        │   │  │
│  │   │ - Top-K retrieval        │   │  │
│  │   └──────────────────────────┘   │  │
│  └──────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

**API Endpoints**:
```
POST   /api/chatbot/message
POST   /api/chatbot/image
GET    /api/chatbot/history/{sessionId}
DELETE /api/chatbot/history/{sessionId}
```

**RAG Pipeline détaillé**:

1. **Ingestion**:
   - Chargement PDFs documents banque
   - Extraction texte (Apache PDFBox)
   - Nettoyage/normalisation

2. **Chunking**:
   - Découpage chunks 512 tokens
   - Overlap 50 tokens
   - Préservation contexte

3. **Embedding**:
   - Modèle: text-embedding-3-small
   - Dimension: 1536
   - Stockage vector store

4. **Retrieval**:
   - Embedding requête utilisateur
   - Similarity search (cosine)
   - Top-K = 5 documents

5. **Generation**:
   - Prompt system + contexte récupéré
   - Appel GPT-4o
   - Streaming réponse

**Configuration Spring AI**:
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o
          temperature: 0.7
          max-tokens: 500
      embedding:
        options:
          model: text-embedding-3-small
```

## 4. Patterns & Bonnes Pratiques

### 4.1 Resilience
- **Circuit Breaker**: Resilience4j
- **Retry**: Exponential backoff
- **Timeout**: 5s max
- **Fallback**: Réponses dégradées

### 4.2 Observabilité
- **Logs structurés**: JSON format
- **Distributed tracing**: Spring Cloud Sleuth + Zipkin
- **Metrics**: Micrometer + Prometheus
- **Health checks**: Actuator

### 4.3 Sécurité
- **JWT**: Tokens signés
- **OAuth2**: Authorization server
- **HTTPS**: TLS 1.3
- **Secrets**: Vault / Config encryption

### 4.4 Communication
- **RESTful**: Richardson Maturity Level 2
- **Versioning**: URL versioning (/v1/, /v2/)
- **Pagination**: Page + Size
- **HATEOAS**: Liens navigation

## 5. Déploiement

### 5.1 Ordre démarrage
1. Config Service
2. Discovery Service (Eureka)
3. Gateway Service
4. Micro-services métiers (parallel)

### 5.2 Configuration Docker Compose
```yaml
version: '3.8'
services:
  config-service:
    build: ./config-service
    ports: ["8888:8888"]
  
  discovery-service:
    build: ./discovery-service
    ports: ["8761:8761"]
    depends_on: [config-service]
  
  gateway-service:
    build: ./gateway-service
    ports: ["8080:8080"]
    depends_on: [discovery-service]
  
  beneficiaire-service:
    build: ./beneficiaire-service
    depends_on: [discovery-service]
  
  virement-service:
    build: ./virement-service
    depends_on: [discovery-service, beneficiaire-service]
  
  chatbot-service:
    build: ./chatbot-service
    depends_on: [discovery-service]
    environment:
      - OPENAI_API_KEY=${OPENAI_API_KEY}
```

## 6. Monitoring & Métriques

### 6.1 Métriques clés
- Temps réponse API (p50, p95, p99)
- Taux erreur (4xx, 5xx)
- Throughput (req/s)
- Temps réponse LLM
- Coût tokens OpenAI
- Mémoire/CPU par service

### 6.2 Alerting
- Service down > 1 min
- Temps réponse > 2s
- Taux erreur > 5%
- Mémoire > 80%
