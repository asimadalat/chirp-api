# Chirp API — Scalable Chat Messaging Backend


> This repository is public for **portfolio and educational review purposes only**.  
> It is **not open source**. No permission is granted to use, copy, modify, deploy,
> or redistribute this software without explicit written consent from the author.

**Chirp API** is a production-ready, scalable chat messaging backend powering the Chirp application.  
It provides secure authentication, 1-to-1 and group messaging, profile pictures, real-time updates, push notifications, and email workflows.

Built with Spring Boot + Kotlin, deployed on Hetzner, Chirp API has been implemented as a modular monolith:
a single deployable application composed of isolated, feature-based modules with clear domain boundaries.

---

## Features

- **Secure Authentication**
  - JWT-based authentication issued and signed by the server
  - Password hashing using Argon2
  - Email verification flow
  - Password reset & change password support
  - Refresh tokens and logout

- **Chat & Messaging**
  - 1-to-1 chats
  - Group chats with participant management
  - Message deletion (editing not supported)
  - Paginated message history
  - Profile picture uploads

- **Real-Time Communication**
  - Websocket-based live updates
  - Domain events for chat and user actions

- **Notifications**
  - Push notifications via Firebase
  - Email notifications via Mailgun
  - Device token registration & deregistration

- **Security & Rate Limiting**
  - IP-based rate limiting
  - Email-based rate limiting
  - Redis-backed rate limiter implementation

- **Persistence & Storage**
  - PostgreSQL via Supabase
  - Supabase Storage for media and profile pictures
  - Spring Data JPA with Jakarta Persistence

- **CI/CD & Deployment**
  - GitHub Actions pipeline
  - Builds executable JAR
  - Automated deployment to Hetzner via SSH

---

## Technology Stack

### Backend
- **Language:** Kotlin
- **Framework:** Spring Boot
- **Security:** Spring Security, JWT, Argon2
- **Validation:** Jakarta Validation
- **Persistence:** Spring Data JPA, Jakarta Persistence
- **Build Tool:** Gradle (Kotlin DSL)

### Infrastructure
- **Hosting:** Hetzner (CX23)
- **Database:** Supabase (PostgreSQL)
- **Storage:** Supabase Storage
- **Caching / Rate Limiting:** Redis
- **Message Queue:** RabbitMQ
- **Push Notifications:** Firebase
- **Email:** Mailgun

### Environments
- **Development:** `http://localhost:8080`
- **Production:** `https://api.chirp.asimorphic.dev`

---

## Architecture Overview

Chirp API is organised as feature-based Gradle modules which allows for a clear separation of concerns and potential for greater scalability.

### Feature Modules

- **user**
  - Authentication & authorisation
  - Email verification
  - Password management
  - JWT handling
  - Rate limiting

- **chat**
  - Chats and participants
  - Messages
  - Profile pictures
  - Websockets
  - Chat domain events

- **notification**
  - Push notifications (Firebase)
  - Email notifications (Mailgun)
  - Device token management

- **common**
  - Shared domain events
  - JWT services
  - Exception handling
  - Utility functions

The system utilises message queues to abide by an **event-driven architecture**, with chat and user events published and consumed across modules.

---

## Project Structure

```
.
├── .github
│   └── workflows
│       └── deploy.yml
├── app
├── build-logic
├── chat
│   ├── api
│   ├── domain
│   ├── infra
│   └── service
├── user
│   ├── api
│   ├── domain
│   ├── infra
│   └── security
├── notification
│   ├── api
│   ├── infra
│   └── service
├── common
│   ├── api
│   ├── domain
│   └── service
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## API Access

Chirp API is a **private API**.

All requests must include a valid **API key**.

### API Key Header



X-API-KEY: \<CHIRP_API_KEY>


- Only **one API key** exists
- The key is managed by the server owner
- Intended for trusted clients only (e.g. `chirp-app`)
- Requests without a valid API key will be rejected


---

## Authentication

Authentication is handled entirely by the backend.

### Flow

1. Client provides API key
2. User authenticates with email & password
3. Server issues a signed JWT
4. Subsequent requests include:

Authorization: Bearer \<ACCESS_TOKEN>

Some endpoints require **both** API key and JWT.

---

## Example API Endpoints

### Auth

| Endpoint | Method | Description |
|--------|--------|-------------|
| `/api/auth/register` | POST | Register a new user |
| `/api/auth/login` | POST | Authenticate user |
| `/api/auth/refresh` | POST | Refresh access token |
| `/api/auth/logout` | POST | Logout user |
| `/api/auth/resend-verification` | POST | Resend verification email |
| `/api/auth/verify-email` | GET | Verify email token |
| `/api/auth/forgot-password` | POST | Request password reset |
| `/api/auth/reset-password` | POST | Reset password |
| `/api/auth/change-password` | POST | Change password |

---

### Chats

| Endpoint | Method | Description |
|--------|--------|-------------|
| `/api/chat` | POST | Create a chat |
| `/api/chat` | GET | Get user chats |
| `/api/chat/{chatId}` | GET | Get chat by ID |
| `/api/chat/{chatId}/add` | POST | Add participants |
| `/api/chat/{chatId}/leave` | DELETE | Leave chat |
| `/api/chat/{chatId}/messages` | GET | Fetch chat messages |

---

### Notifications

| Endpoint | Method | Description |
|--------|--------|-------------|
| `/api/notification/register` | POST | Register device token |
| `/api/notification/deregister/{token}` | DELETE | Deregister device token |

---

## Websockets

Websockets are used for real-time chat updates.

Events include:
- Message sent
- Message deleted
- Chat participant joined / left
- Profile picture updated

---

## Rate Limiting

- IP-based rate limiting
- Email-based rate limiting
- Backed by Redis
- Enforced via custom annotations and interceptors

---

## Key Libraries & Dependencies

Chirp API relies on a carefully curated set of libraries to enable
security, scalability, and real-time communication.

### Core Platform
- **Kotlin:** 2.2.x
- **Spring Boot:** 4.0.0-M3

### Web & Messaging
- **Spring Web / WebSocket** — REST APIs and real-time communication
- **Spring AMQP (RabbitMQ)** — internal domain events and async processing
- **OkHttp** — outbound HTTP calls (Supabase, Mailgun, external services)

### Security & Cryptography
- **Spring Security** — authentication and authorisation
- **JWT (jjwt)** — access & refresh token handling
- **Argon2** — secure password hashing
- **Bouncy Castle** — cryptographic primitives and token support

### Data & Storage
- **Spring Data JPA** — persistence layer
- **PostgreSQL** — primary database
- **Spring Data Redis** — IP and email rate limiting

### Notifications & Email
- **Firebase Admin SDK** — push notifications
- **Spring Mail** — email delivery
- **Thymeleaf** — email template rendering

### Tools
- **Jackson** — JSON serialisation
- **Jakarta Validation** — request validation

Dependency versions are managed using a central version catalog to ensure consisstency across feature modules.

---

## Build & Run

### Prerequisites

- JDK 21+
- Redis
- Supabase project
- Firebase service account
- Mailgun credentials

### Build

`./gradlew clean build`

### Run Locally

`java -jar build/libs/chirp.jar`

---

## CI/CD

GitHub Actions pipeline:
- Triggers on push
- Builds executable JAR
- Deploys to Hetzner via SSH

This can ensure continuous repeatable deployments.

---

## Versioning

Chirp API follows **Semantic Versioning**:

MAJOR.MINOR.PATCH

Examples:
- `1.0.0` – Initial stable release
- `1.1.0` – New features
- `1.1.1` – Bug fixes

---

## Related Repositories

- **chirp-app** — Client application consuming this API

---

## License & Usage

This project is **proprietary software**.

No license is granted to use, copy, modify, or distribute this code.
The repository is published publicly for **portfolio and educational review only**.

Copyright © 2026 Asimorphic
