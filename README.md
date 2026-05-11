# JWT Authentication API — Spring Boot 4

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-6DB33F?logo=springsecurity&logoColor=white)
![JJWT](https://img.shields.io/badge/JJWT-0.12.6-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-UI-85EA2D?logo=swagger&logoColor=black)
![Render](https://img.shields.io/badge/Render-Live-46E3B7?logo=render&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-multi--stage-2496ED?logo=docker&logoColor=white)
![Mailtrap](https://img.shields.io/badge/Mailtrap-email-00B2A9?logo=mailtrap&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-green)

---

## 🎯 En bref — ce que ce projet démontre

> **Pour les recruteurs et managers non techniques**

Ce projet est une **API de gestion d'identité et d'authentification** déployée en production, construite de A à Z en Java. En termes simples : c'est le composant qui, dans n'importe quelle application (site web, application mobile), gère :

- **La création de comptes** — inscription sécurisée avec validation des données
- **La connexion** — vérification de l'identité et délivrance d'un « badge d'accès » numérique (token JWT)
- **Le contrôle d'accès** — certaines fonctionnalités sont réservées aux administrateurs, d'autres aux utilisateurs connectés, d'autres encore sont publiques
- **La récupération de mot de passe** — envoi d'un email sécurisé avec lien de réinitialisation (valable 15 minutes)
- **Le renouvellement de session** — les utilisateurs restent connectés sans ressaisir leur mot de passe

### Ce que ce projet illustre techniquement

| Compétence | Ce qui est mis en œuvre |
|---|---|
| **Architecture logicielle** | Architecture en couches (Controller → Service → Repository), séparation des responsabilités |
| **Sécurité applicative** | JWT, BCrypt, prévention des attaques par énumération, Spring Security |
| **API REST** | Endpoints documentés, codes HTTP corrects, validation des données entrantes |
| **Base de données** | Modélisation relationnelle, JPA/Hibernate, gestion du pool de connexions |
| **Tests** | Tests unitaires (Mockito) + tests d'intégration (H2 in-memory) |
| **Déploiement** | Docker multi-stage, CI/CD via GitHub → Render, variables d'environnement |
| **Communication** | Intégration service d'emailing (Mailtrap SMTP) |
| **Documentation** | Swagger UI interactif, README complet |

---

## 🌐 Demo live

| Ressource | URL |
|---|---|
| **Swagger UI** | [https://jwt-api-gkdg.onrender.com/swagger-ui.html](https://jwt-api-gkdg.onrender.com/swagger-ui.html) |
| **Health check** | [https://jwt-api-gkdg.onrender.com/actuator/health](https://jwt-api-gkdg.onrender.com/actuator/health) |
| **OpenAPI JSON** | [https://jwt-api-gkdg.onrender.com/v3/api-docs](https://jwt-api-gkdg.onrender.com/v3/api-docs) |

> ⏱️ **Cold start** — hébergé sur le tier gratuit de Render. Le premier appel après inactivité peut prendre ~50 secondes le temps de réveiller le conteneur.

---

## 📋 Table des matières

- [Fonctionnalités](#-fonctionnalités)
- [Architecture](#️-architecture)
- [Flux de sécurité](#-flux-de-sécurité)
- [Quick Start](#-quick-start)
- [Référence API](#-référence-api)
- [Configuration email](#-configuration-email-mot-de-passe-oublié)
- [Configuration](#️-configuration)
- [Modèle de données](#️-modèle-de-données)
- [Rôles & permissions](#-rôles--permissions)
- [Tests](#-tests)
- [Déploiement](#-déploiement)
- [Stack technique](#-stack-technique)
- [Structure du projet](#️-structure-du-projet)

---

## ✨ Fonctionnalités

| # | Fonctionnalité | Endpoint | Accès |
|---|---|---|---|
| 1 | Inscription | `POST /api/v1/auth/register` | Public |
| 2 | Connexion | `POST /api/v1/auth/login` | Public |
| 3 | Renouvellement de tokens | `POST /api/v1/auth/refresh` | Public |
| 4 | Mot de passe oublié | `POST /api/v1/auth/forgot-password` | Public |
| 5 | Réinitialisation du mot de passe | `POST /api/v1/auth/reset-password` | Public |
| 6 | Profil utilisateur | `GET /api/v1/users/me` | 🔒 Bearer token |
| 7 | Changement de mot de passe | `PUT /api/v1/users/change-password` | 🔒 Bearer token |
| 8 | Liste des utilisateurs | `GET /api/v1/users` | 🔒 ADMIN uniquement |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT (Browser / Mobile / Postman)     │
└───────────────────────────────┬─────────────────────────────────┘
                                │ HTTPS
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    RENDER  (Docker container)                   │
│                                                                 │
│  ┌──────────────┐   ┌───────────────────┐    ┌────────────────┐ │
│  │JwtAuth       │──▶│  Controllers      │──▶│   Services     │ │
│  │Filter        │   │  AuthController   │    │  AuthService   │ │
│  │(every req.)  │   │  UserController   │    │  UserService   │ │
│  └──────────────┘   └───────────────────┘    │  PasswordReset │ │
│                                              │  EmailService  │ │
│  ┌──────────────┐                            └───────┬────────┘ │
│  │JwtService    │                                    │          │
│  │(sign/verify) │                            ┌───────▼────────┐ │
│  └──────────────┘                            │ UserRepository │ │
│                                              │ (Spring Data)  │ │
│  ┌──────────────┐                            └───────┬────────┘ │
│  │SecurityConfig│                                    │          │
│  │(filter chain)│                                    │ JDBC     │
│  └──────────────┘                                    ▼          │
└─────────────────────────────────────────────────────────────────┘
                                                        │
                                                        ▼
                                          ┌─────────────────────────┐
                                          │  CLEVER CLOUD  (MySQL)  │
                                          │  Table : users          │
                                          └─────────────────────────┘
```

---

## 🔐 Flux de sécurité

### Inscription & connexion

```mermaid
sequenceDiagram
    participant C as Client
    participant AC as AuthController
    participant AS as AuthService
    participant DB as Base de données

    C->>AC: POST /register { username, email, password }
    AC->>AS: register(request)
    AS->>DB: existsByUsername / existsByEmail
    DB-->>AS: false / false
    AS->>DB: save(user) — password BCrypt hashé
    AS->>AS: generateToken() + generateRefreshToken()
    AS-->>AC: AuthResponse
    AC-->>C: 201 { accessToken, refreshToken }

    C->>AC: POST /login { username, password }
    AC->>AS: login(request)
    AS->>AS: AuthenticationManager.authenticate()
    AS->>DB: findByUsername
    DB-->>AS: User
    AS->>AS: generateToken() + generateRefreshToken()
    AS-->>C: 200 { accessToken, refreshToken }
```

### Requête authentifiée

```mermaid
sequenceDiagram
    participant C as Client
    participant F as JwtAuthFilter
    participant J as JwtService
    participant DB as Base de données
    participant CT as Controller

    C->>F: GET /api/v1/users/me<br/>Authorization: Bearer <token>
    F->>J: extractUsername(token)
    J-->>F: "alice"
    F->>DB: loadUserByUsername("alice")
    DB-->>F: User
    F->>J: isTokenValid(token, user)
    J-->>F: true
    F->>F: setAuthentication()
    F->>CT: requête transmise
    CT-->>C: 200 { id, username, email, role }
```

### Mot de passe oublié

```mermaid
sequenceDiagram
    participant C as Client
    participant AC as AuthController
    participant PRS as PasswordResetService
    participant DB as Base de données
    participant M as Email (Mailtrap)

    C->>AC: POST /forgot-password { email }
    AC->>PRS: requestPasswordReset(email)
    PRS->>DB: findByEmail
    Note over PRS: Si email inconnu → réponse identique<br/>(prévention d'énumération)
    PRS->>DB: save user avec token UUID + expiry 15 min
    PRS->>M: sendPasswordResetEmail(token)
    AC-->>C: 200 { message: "Si un compte existe..." }

    Note over C: L'utilisateur reçoit le token par email

    C->>AC: POST /reset-password { token, newPassword }
    AC->>PRS: resetPassword(token, newPassword)
    PRS->>DB: findByResetToken
    PRS->>PRS: vérifier expiry
    PRS->>DB: save nouveau mot de passe BCrypt<br/>+ invalider token
    AC-->>C: 200 { message: "Mot de passe réinitialisé" }
```

---

## ⚡ Quick Start

### Prérequis

| Outil | Version minimale |
|---|---|
| Java (JDK) | 17 |
| Maven | 3.8+ *(ou utiliser `./mvnw` inclus)* |
| MySQL | 8.0 |
| Docker *(optionnel)* | 24+ |

### 1. Cloner et configurer

```bash
git clone https://github.com/GomuGomuNo01/API_REST.git
cd jwt-api
```

Créer la base de données MySQL :

```sql
CREATE DATABASE jwt_api_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Démarrer en local

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

L'API est disponible sur `http://localhost:8080`.  
La Swagger UI est accessible sur `http://localhost:8080/swagger-ui.html`.

> **Astuce** : le profil `local` active MySQL local, HikariCP permissif, les logs SQL et Mailtrap sandbox. Sans credentials Mailtrap configurés, le token de reset est simplement loggué en console.

### 3. S'inscrire

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","password":"motdepasse8"}'
```

Réponse :
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

### 4. Accéder à un endpoint protégé

```bash
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

Réponse :
```json
{
  "id": 1,
  "username": "alice",
  "email": "alice@example.com",
  "role": "ROLE_USER"
}
```

### 5. Renouveler les tokens

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"eyJhbGciOiJIUzI1NiJ9..."}'
```

### 6. Changer de mot de passe

```bash
curl -X PUT http://localhost:8080/api/v1/users/change-password \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"currentPassword":"motdepasse8","newPassword":"nouveauMdp8"}'
```

### 7. Mot de passe oublié

```bash
# Étape 1 — demander le token (valable 15 min)
curl -X POST http://localhost:8080/api/v1/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com"}'

# Étape 2 — utiliser le token reçu par email
curl -X POST http://localhost:8080/api/v1/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"token":"<uuid-recu-par-email>","newPassword":"nouveauMdp8"}'
```

---

## 📡 Référence API

### 🔓 Authentification — `/api/v1/auth` (public)

| Méthode | Endpoint | Description | Corps | Réponse |
|---|---|---|---|---|
| `POST` | `/register` | Créer un compte | `RegisterRequest` | `201 AuthResponse` |
| `POST` | `/login` | Se connecter | `AuthRequest` | `200 AuthResponse` |
| `POST` | `/refresh` | Renouveler les tokens | `RefreshTokenRequest` | `200 AuthResponse` |
| `POST` | `/forgot-password` | Demander un reset par email | `ForgotPasswordRequest` | `200 MessageResponse` |
| `POST` | `/reset-password` | Réinitialiser avec le token | `ResetPasswordRequest` | `200 MessageResponse` |

### 🔒 Utilisateurs — `/api/v1/users` (Bearer token requis)

| Méthode | Endpoint | Description | Rôle | Réponse |
|---|---|---|---|---|
| `GET` | `/me` | Profil de l'utilisateur connecté | USER / ADMIN | `200 UserResponse` |
| `PUT` | `/change-password` | Changer son mot de passe | USER / ADMIN | `200 MessageResponse` |
| `GET` | `/` | Liste de tous les utilisateurs | ADMIN only | `200 List<UserResponse>` |

### Schémas

<details>
<summary><strong>RegisterRequest</strong></summary>

```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "motdepasse8"
}
```

> Le mot de passe doit contenir au moins 8 caractères.
</details>

<details>
<summary><strong>AuthRequest</strong></summary>

```json
{
  "username": "alice",
  "password": "motdepasse8"
}
```
</details>

<details>
<summary><strong>AuthResponse</strong></summary>

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```
</details>

<details>
<summary><strong>UserResponse</strong></summary>

```json
{
  "id": 1,
  "username": "alice",
  "email": "alice@example.com",
  "role": "ROLE_USER"
}
```
</details>

<details>
<summary><strong>ErrorResponse</strong></summary>

```json
{
  "status": 400,
  "error": "Validation échouée",
  "message": "Les données envoyées sont invalides",
  "timestamp": "2026-05-06T10:00:00",
  "fieldErrors": {
    "email": "Format d'email invalide",
    "password": "Le mot de passe doit contenir au moins 8 caractères"
  }
}
```
</details>

### Codes HTTP

| Code | Signification |
|---|---|
| `200 OK` | Succès |
| `201 Created` | Compte créé |
| `400 Bad Request` | Données invalides (`@Valid` échoue) |
| `401 Unauthorized` | Token absent/expiré ou identifiants incorrects |
| `403 Forbidden` | Rôle insuffisant |
| `404 Not Found` | Utilisateur introuvable |
| `409 Conflict` | Username ou email déjà utilisé |
| `500 Internal Server Error` | Erreur serveur |

---

## 📧 Configuration email (mot de passe oublié)

> ⚠️ **Les endpoints `/forgot-password` et `/reset-password` nécessitent un service d'envoi d'email.** Sans credentials SMTP, le token est uniquement loggué en console.

L'envoi d'email est géré via **Mailtrap**, intégré différemment selon le profil actif :

| Profil | Mode Mailtrap | Host | Port | Comportement |
|---|---|---|---|---|
| `local` | Testing (sandbox) | `sandbox.smtp.mailtrap.io` | `2525` | Emails interceptés, visibles dans le dashboard Mailtrap |
| `prod` | Sending (live) | `live.smtp.mailtrap.io` | `587` | Emails envoyés réellement aux destinataires |

**Étapes de configuration :**

1. Créer un compte sur [mailtrap.io](https://mailtrap.io) (plan gratuit)
2. **Profil local** — **Email Testing → Inboxes** → SMTP Settings → copier `Username` et `Password` dans les variables d'env `MAILTRAP_USERNAME` / `MAILTRAP_PASSWORD`
3. **Profil prod** — **Email Sending** → vérifier son domaine → récupérer les credentials SMTP → les définir sur Render

> 💡 En sandbox, aucun email n'est livré aux vrais destinataires — tout est capturé dans le dashboard Mailtrap, idéal pour tester sans risque.

**Sans credentials configurés**, le token de réinitialisation est loggué dans la console :

```
WARN  EmailService - Impossible d'envoyer l'email à alice@example.com
      — token de réinitialisation (DEV) : f47ac10b-58cc-4372-a567-0e02b2c3d479
```

---

## ⚙️ Configuration

### Profils Spring Boot

La configuration est séparée en trois fichiers :

| Fichier | Profil | Usage |
|---|---|---|
| `application.properties` | *(base commune)* | Paramètres partagés entre tous les environnements |
| `application-local.properties` | `local` | Développement local — MySQL local, Mailtrap sandbox, logs SQL activés |
| `application-prod.properties` | `prod` | Production Render — tout via variables d'environnement, HikariCP strict |

**Activer un profil :**

```bash
# Local
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Production (Render) — définir la variable d'environnement
SPRING_PROFILES_ACTIVE=prod
```

### Variables d'environnement

#### Profil `local` — valeurs à définir dans votre shell ou IDE

| Variable | Description | Défaut si absent |
|---|---|---|
| `MAILTRAP_USERNAME` | Username inbox Mailtrap sandbox | *(vide — token loggué en console)* |
| `MAILTRAP_PASSWORD` | Password inbox Mailtrap sandbox | *(vide — token loggué en console)* |
| `JWT_SECRET` | Clé HMAC-SHA256 Base64 ≥ 256 bits | Clé de dev embarquée |

#### Profil `prod` — variables à définir sur Render

| Variable | Description |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://host:3306/db?useSSL=true&serverTimezone=UTC` |
| `SPRING_DATASOURCE_USERNAME` | Utilisateur MySQL Clever Cloud |
| `SPRING_DATASOURCE_PASSWORD` | Mot de passe MySQL Clever Cloud |
| `JWT_SECRET` | Clé Base64 ≥ 256 bits (`openssl rand -base64 64`) |
| `JWT_EXPIRATION` | Durée access token en ms *(défaut : `86400000` = 24 h)* |
| `MAILTRAP_USERNAME` | Username Mailtrap Sending |
| `MAILTRAP_PASSWORD` | Password / API token Mailtrap Sending |
| `MAIL_FROM` | Adresse expéditeur (domaine vérifié Mailtrap) |
| `APP_RESET_URL` | URL publique de l'API (`https://jwt-api-gkdg.onrender.com`) |

Générer une clé JWT sécurisée :

```bash
openssl rand -base64 64
```

---

## 🗄️ Modèle de données

### Table `users`

| Colonne | Type | Contraintes |
|---|---|---|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` |
| `username` | `VARCHAR(50)` | `NOT NULL`, `UNIQUE` |
| `email` | `VARCHAR(100)` | `NOT NULL`, `UNIQUE` |
| `password` | `VARCHAR(255)` | `NOT NULL` — BCrypt |
| `role` | `VARCHAR(20)` | `NOT NULL` — `ROLE_USER` / `ROLE_ADMIN` |
| `reset_token` | `VARCHAR(255)` | `UNIQUE`, nullable |
| `reset_token_expiry` | `DATETIME` | nullable — expiry du token de reset |

> Les mots de passe sont toujours stockés hashés via **BCryptPasswordEncoder**. Les tokens de reset sont invalidés après usage ou expiration (15 min).

### Durée de vie des tokens

| Token | Durée | Configurable via |
|---|---|---|
| Access token | 24 heures | `JWT_EXPIRATION` (ms) |
| Refresh token | 7 jours | `REFRESH_EXPIRATION` dans `JwtService` |
| Token de reset | **15 minutes** | `TOKEN_EXPIRY_MINUTES` dans `PasswordResetService` |

---

## 👤 Rôles & permissions

L'API utilise deux rôles : `ROLE_USER` (défaut) et `ROLE_ADMIN`.

### Matrice des accès

| Endpoint | ROLE_USER | ROLE_ADMIN | Anonyme |
|---|:---:|:---:|:---:|
| `/auth/**` | ✅ | ✅ | ✅ |
| `/users/me` | ✅ | ✅ | ❌ |
| `/users/change-password` | ✅ | ✅ | ❌ |
| `/users/` (liste) | ❌ | ✅ | ❌ |

### Promouvoir un utilisateur en ADMIN

Après l'inscription, modifier directement en base de données :

```sql
UPDATE users SET role = 'ROLE_ADMIN' WHERE username = 'alice';
```

---

## 🧪 Tests

### Suite de tests

| Classe | Type | Stratégie |
|---|---|---|
| `JwtApiApplicationTests` | Intégration | `@SpringBootTest` + H2 — chargement du contexte complet |
| `JwtServiceTest` | Unitaire | `ReflectionTestUtils` — injection directe de `secretKey` et `jwtExpiration` |
| `AuthServiceTest` | Unitaire | `@ExtendWith(MockitoExtension)` — mock `UserRepository`, `JwtService`, `PasswordEncoder` |
| `AuthControllerTest` | Couche web | `MockMvcBuilders.standaloneSetup()` — sans contexte Spring |
| `UserRepositoryTest` | Intégration BDD | `@SpringBootTest` + H2 + `@Transactional` — rollback automatique |

```bash
./mvnw test                              # tous les tests (H2, aucun MySQL requis)
./mvnw test -Dtest=JwtServiceTest        # test ciblé
./mvnw verify                            # tests + rapport Surefire
```

---

## 🚀 Déploiement

### Infrastructure

```
GitHub (main) ──push──▶ Render (build Docker) ──deploy──▶ 🌐 Live
                                                     │
                                              ┌──────▼──────┐
                                              │ Clever Cloud│
                                              │ MySQL DEV   │
                                              └─────────────┘
```

| Composant | Service | Plan |
|---|---|---|
| Application | Render — Web Service Docker | Free |
| Base de données | Clever Cloud — MySQL add-on | DEV (256 MB, gratuit) |
| Email | Mailtrap (sandbox + sending) | Free |

### Dockerfile (multi-stage)

```dockerfile
# Stage 1 : Build Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2 : Runtime JRE Alpine (~180 MB)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Variables à configurer sur Render

| Variable | Valeur |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://host:3306/db?useSSL=true&serverTimezone=UTC` |
| `SPRING_DATASOURCE_USERNAME` | Utilisateur MySQL Clever Cloud |
| `SPRING_DATASOURCE_PASSWORD` | Mot de passe MySQL Clever Cloud |
| `JWT_SECRET` | Clé Base64 ≥ 256 bits (`openssl rand -base64 64`) |
| `MAILTRAP_USERNAME` | Username Mailtrap Sending |
| `MAILTRAP_PASSWORD` | Password / API token Mailtrap Sending |
| `MAIL_FROM` | Adresse expéditeur (domaine vérifié) |
| `APP_RESET_URL` | `https://jwt-api-gkdg.onrender.com` |
| `APP_RESET_URL` | URL publique de l'API (`https://jwt-api-gkdg.onrender.com`) |

> **Limite Clever Cloud DEV** : 5 connexions MySQL simultanées. La configuration HikariCP est calibrée (`pool=1`, `min-idle=0`, `lazy-initialization=true`) pour démarrer avec 0 connexion et n'en ouvrir qu'à la première requête HTTP.

---

## 📦 Stack technique

| Couche | Technologie | Version |
|---|---|---|
| Langage | Java | 17 |
| Framework | Spring Boot | 4.0.6 |
| Web | Spring MVC | 7.x |
| Sécurité | Spring Security | 6.x |
| Persistance | Spring Data JPA + Hibernate | 7.x |
| Base de données (local) | MySQL / WAMP | 8.x |
| Base de données (prod) | Clever Cloud MySQL | 8.0 |
| Tests | H2 in-memory | — |
| Connection pool | HikariCP | 7.x |
| JWT | JJWT | 0.12.6 |
| Emails | Spring Mail + Mailtrap | — |
| Documentation | springdoc-openapi + Swagger UI | **3.0.3** ¹ |
| Monitoring | Spring Boot Actuator | 4.0.6 |
| Boilerplate | Lombok | — |
| Null-safety | JSpecify | — |
| Build | Maven | 3.x |
| Conteneur | Docker multi-stage | — |
| Hébergement | Render | Free |

> ¹ springdoc **3.x** est obligatoire pour Spring Boot 4 / Spring Framework 7 (la v2.x cible Boot 3.x).

---

## 🗂️ Structure du projet

```
src/
├── main/java/com/cedric/jwtapi/
│   ├── config/
│   │   ├── SecurityConfig.java        # Chaîne de filtres, CORS, règles d'accès
│   │   └── OpenApiConfig.java         # Schéma Bearer pour Swagger UI
│   ├── controller/
│   │   ├── AuthController.java        # register, login, refresh, forgot/reset password
│   │   └── UserController.java        # me, change-password, liste (admin)
│   ├── dto/
│   │   ├── AuthRequest.java
│   │   ├── AuthResponse.java
│   │   ├── RegisterRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   ├── ForgotPasswordRequest.java
│   │   ├── ResetPasswordRequest.java
│   │   ├── ChangePasswordRequest.java
│   │   ├── MessageResponse.java
│   │   └── UserResponse.java
│   ├── entity/
│   │   ├── User.java                  # Entité JPA + UserDetails + champs reset token
│   │   └── Role.java                  # ROLE_USER / ROLE_ADMIN
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ErrorResponse.java
│   │   ├── UserAlreadyExistsException.java
│   │   └── InvalidTokenException.java
│   ├── repository/
│   │   └── UserRepository.java
│   ├── security/
│   │   ├── JwtService.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── UserDetailsServiceImpl.java
│   └── service/
│       ├── AuthService.java
│       ├── UserService.java
│       ├── PasswordResetService.java  # Logique forgot/reset password
│       └── EmailService.java         # Envoi d'emails via Mailtrap SMTP
├── resources/
│   ├── application.properties          # Configuration commune (base)
│   ├── application-local.properties    # Profil local : MySQL local, Mailtrap sandbox
│   └── application-prod.properties     # Profil prod  : tout via variables d'env Render
Dockerfile
```

*Spring Boot 4.0.6 · Java 17 · JJWT 0.12.6 · springdoc 3.0.3 · Render + Clever Cloud + Mailtrap*
