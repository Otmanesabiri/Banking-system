# Banking System - Microservices Architecture

## Architecture
- **config-service** (Port 8888): Configuration centralisée
- **discovery-service** (Port 8761): Service registry Eureka
- **gateway-service** (Port 8080): API Gateway
- **beneficiaire-service** (Port 8081): Gestion des bénéficiaires
- **virement-service** (Port 8082): Gestion des virements
- **chatbot-service** (Port 8083): Service chatbot

## Démarrage

### 1. Ordre de démarrage
```bash
# 1. Config Service
cd config-service && mvn spring-boot:run

# 2. Discovery Service
cd discovery-service && mvn spring-boot:run

# 3. Gateway Service
cd gateway-service && mvn spring-boot:run

# 4. Services métier (en parallèle)
cd beneficiaire-service && mvn spring-boot:run
cd virement-service && mvn spring-boot:run
cd chatbot-service && mvn spring-boot:run
```

### 2. Accès aux services
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- H2 Console (Beneficiaire): http://localhost:8081/h2-console
- H2 Console (Virement): http://localhost:8082/h2-console

## Build
```bash
mvn clean install
```

## Technologies
- Java 17
- Spring Boot 3.2.0
- Spring Cloud 2023.0.0
- Maven
