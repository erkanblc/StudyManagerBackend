# StudyManager Backend — Architecture & UML

> Visual overview of the Spring Boot REST API: layered components, auth flow, domain model, and package layout.

**Docs:** [English](README.md) · [Türkçe](README_TR.md) · [Deutsch](README-DE.md)

Diagrams use [Mermaid](https://mermaid.js.org/) (renders on GitHub / most Markdown viewers).

---

## Table of Contents

1. [System context](#1-system-context)
2. [Layered architecture](#2-layered-architecture)
3. [Package / component view](#3-package--component-view)
4. [Authentication sequence](#4-authentication-sequence)
5. [Study session lifecycle](#5-study-session-lifecycle)
6. [Entity-relationship (ER)](#6-entity-relationship-er)
7. [Class overview (domain)](#7-class-overview-domain)
8. [Security & roles](#8-security--roles)
9. [Scheduled jobs](#9-scheduled-jobs)

---

## 1. System context

```mermaid
flowchart LR
  FE["Frontend<br/>(React / Vite<br/>:3000 or :5173)"]
  API["StudyManager Backend<br/>Spring Boot 4.1 · Java 21<br/>:8080"]
  DB[(MySQL 8.x<br/>study_manager_db)]
  SW["Swagger UI<br/>/swagger-ui"]

  FE -->|"HTTPS/HTTP JSON<br/>Bearer JWT"| API
  SW -->|"Try-it-out"| API
  API -->|"JDBC / JPA"| DB
```

---

## 2. Layered architecture

```mermaid
flowchart TB
  subgraph Client
    Browser[Web Client / Swagger]
  end

  subgraph API["com.studymanager"]
    direction TB
    Filter["JwtAuthFilter<br/>SecurityFilterChain"]
    Ctrl["Controllers<br/>auth · goal · study · admin · user · config"]
    Svc["Services<br/>business rules & validation"]
    Sched["Schedulers<br/>GoalOverdueScheduler"]
    Repo["Repositories<br/>Spring Data JPA"]
    Ent["Entities / Enums"]
  end

  DB[(MySQL)]

  Browser --> Filter --> Ctrl --> Svc --> Repo --> Ent
  Sched --> Svc
  Repo --> DB
```

**Request path:** HTTP → CORS / Security → JWT filter → Controller → Service → Repository → MySQL → DTO response.

---

## 3. Package / component view

```mermaid
flowchart TB
  subgraph config["config"]
    SC[SecurityConfig]
    DI[DataInitializer]
    WC[WebConfig / CORS]
    OA[OpenApiConfig]
    JWT[JwtUtils + JwtAuthFilter]
  end

  subgraph controllers["controller"]
    AuthC[auth]
    GoalC[goal]
    StudyC[study]
    AdminC[admin]
    UserC[user]
    CfgC[config]
  end

  subgraph services["service"]
    AuthS[auth]
    GoalS[goal]
    StudyS[study]
    UserS[user]
    CfgS[config]
  end

  subgraph data["persistence"]
    Repos[repository.*]
    Entities[entity.*]
  end

  Sched[scheduler.GoalOverdueScheduler]

  AuthC --> AuthS
  GoalC --> GoalS
  StudyC --> StudyS
  AdminC --> UserS
  AdminC --> GoalS
  UserC --> UserS
  CfgC --> CfgS
  Sched --> GoalS
  AuthS --> Repos
  GoalS --> Repos
  StudyS --> Repos
  UserS --> Repos
  CfgS --> Repos
  Repos --> Entities
  JWT --> AuthS
  SC --> JWT
```

| Package | Responsibility |
|---|---|
| `config` / `config.security` | Security, JWT, CORS, OpenAPI, seed data |
| `controller.*` | REST endpoints, auth annotations |
| `service.*` | Domain logic, ownership checks, limits |
| `repository.*` | Spring Data JPA interfaces |
| `entity.*` | JPA entities & enums |
| `dto.request` / `dto.response` | API contracts |
| `scheduler` | Cron jobs (overdue goals) |

---

## 4. Authentication sequence

```mermaid
sequenceDiagram
  actor Client
  participant Auth as AuthController
  participant Svc as AuthService
  participant JWT as JwtUtils
  participant DB as MySQL
  participant Filter as JwtAuthFilter

  Client->>Auth: POST /api/auth/login {email, password}
  Auth->>Svc: authenticate
  Svc->>DB: load User + roles
  Svc->>DB: insert LoginHistory
  Svc->>JWT: create access token (HS512)
  Svc->>DB: store RefreshToken
  Auth-->>Client: AuthResponse {token, refreshToken, roles, ...}

  Client->>Filter: GET /api/goals  Authorization: Bearer access
  Filter->>JWT: validate & parse
  Filter-->>Client: continue to GoalController

  Client->>Auth: POST /api/auth/refresh {refreshToken}
  Auth->>Svc: rotate / issue new pair
  Svc->>DB: validate RefreshToken (not revoked / not expired)
  Auth-->>Client: new token pair

  Client->>Auth: POST /api/auth/logout {refreshToken}
  Auth->>Svc: revoke RefreshToken
  Auth-->>Client: 200 OK
```

**Token lifetimes** (defaults): access `86400000` ms (24h), refresh `604800000` ms (7d). Refresh lifetime is absolute from login (does not extend on refresh).

---

## 5. Study session lifecycle

```mermaid
stateDiagram-v2
  [*] --> ACTIVE: POST /sessions/start
  ACTIVE --> ACTIVE: PATCH .../heartbeat<br/>(duration = elapsed seconds)
  ACTIVE --> COMPLETED: POST /sessions/stop
  ACTIVE --> COMPLETED: POST .../resolve SAVE_AT_HEARTBEAT
  ACTIVE --> ACTIVE: POST .../resolve CONTINUE
  ACTIVE --> MANUAL: POST .../resolve MANUAL
  [*] --> MANUAL: POST /sessions/manual
  COMPLETED --> [*]
  MANUAL --> [*]
```

Duration is capped by `app_settings.max.session.hours` (default 6, clamp 6–24).

---

## 6. Entity-relationship (ER)

```mermaid
erDiagram
  users ||--o{ user_roles : has
  roles ||--o{ user_roles : assigned
  users ||--o{ goals : creates
  goals ||--o{ milestones : contains
  users ||--o{ milestones : owns
  users ||--o{ study_sessions : logs
  users ||--o{ plan_sessions : plans
  users ||--o{ login_history : records
  users ||--o{ refresh_tokens : holds

  users {
    bigint id PK
    string username UK
    string email UK
    string password
    string fullName
    boolean active
    string adminApprovalStatus
    datetime createdAt
  }

  roles {
    bigint id PK
    string name UK
    boolean active
  }

  user_roles {
    bigint user_id FK
    bigint role_id FK
  }

  goals {
    bigint id PK
    string title
    string description
    date startDate
    date endDate
    double targetHours
    string status
    datetime createdAt
    datetime updatedAt
    bigint created_by FK
  }

  milestones {
    bigint id PK
    string title
    string description
    date dueDate
    string type
    boolean completed
    datetime completedAt
    datetime createdAt
    bigint user_id FK
    bigint goal_id FK "nullable = standalone"
  }

  study_sessions {
    bigint id PK
    datetime startTime
    datetime endTime
    bigint durationSeconds
    bigint goalId
    string subject
    string notes
    string status
    datetime lastHeartbeatAt
    datetime createdAt
    bigint user_id FK
  }

  plan_sessions {
    bigint id PK
    string title
    bigint goalId
    string goalTitle
    string type
    datetime plannedDate
    int plannedDurationMinutes
    string notes
    string status
    datetime createdAt
    bigint user_id FK
  }

  login_history {
    bigint id PK
    bigint user_id FK
    datetime loginAt
  }

  refresh_tokens {
    bigint id PK
    string token UK
    bigint user_id FK
    datetime expiresAt
    datetime createdAt
    boolean revoked
  }

  app_settings {
    bigint id PK
    string settingKey UK
    string settingValue
    string description
    datetime updatedAt
    string updatedBy
  }
```

**Notes:**

- `study_sessions.goalId` and `plan_sessions.goalId` are logical references (columns), not JPA `@ManyToOne` FKs.
- Milestones may be goal-linked (`goal_id` set) or standalone (`goal_id` null, `user_id` set).

---

## 7. Class overview (domain)

```mermaid
classDiagram
  class User {
    +Long id
    +String username
    +String email
    +String password
    +String fullName
    +boolean active
    +AdminApprovalStatus adminApprovalStatus
    +Set~Role~ roles
  }

  class Role {
    +Long id
    +String name
    +boolean active
    +ADMIN
    +STUDENT
    +INSTRUCTOR
  }

  class Goal {
    +Long id
    +String title
    +LocalDate startDate
    +LocalDate endDate
    +Double targetHours
    +GoalStatus status
    +User createdBy
    +List~Milestone~ milestones
  }

  class Milestone {
    +Long id
    +String title
    +boolean completed
    +User user
    +Goal goal
  }

  class StudySession {
    +Long id
    +ZonedDateTime startTime
    +Long durationSeconds
    +Long goalId
    +SessionStatus status
    +User user
  }

  class PlanSession {
    +Long id
    +String title
    +PlanSessionType type
    +PlanSessionStatus status
    +Integer plannedDurationMinutes
    +User user
  }

  class RefreshToken {
    +String token
    +ZonedDateTime expiresAt
    +boolean revoked
    +User user
  }

  class AppSetting {
    +String settingKey
    +String settingValue
    +String updatedBy
  }

  User "*" --> "*" Role : user_roles
  User "1" --> "*" Goal : createdBy
  Goal "1" --> "*" Milestone
  User "1" --> "*" Milestone
  User "1" --> "*" StudySession
  User "1" --> "*" PlanSession
  User "1" --> "*" RefreshToken
```

### Enums

| Enum | Values |
|---|---|
| `GoalStatus` | `ACTIVE`, `PAUSED`, `OVERDUE`, `COMPLETED`, `CANCELLED`, `ARCHIVED` |
| `SessionStatus` | `ACTIVE`, `COMPLETED`, `MANUAL` |
| `PlanSessionType` | `STUDY`, `REVIEW`, `EXAM_PREP`, `PROJECT` |
| `PlanSessionStatus` | `PLANNED`, `COMPLETED`, `MISSED` |
| `AdminApprovalStatus` | `NONE`, `PENDING`, `APPROVED`, `REJECTED` (as used by registration flow) |

---

## 8. Security & roles

```mermaid
flowchart LR
  Req[HTTP Request] --> Cors[CORS]
  Cors --> Chain[SecurityFilterChain]
  Chain --> Match{Path?}
  Match -->|"/api/auth/**"| Public[permitAll]
  Match -->|"Swagger"| Public
  Match -->|"/api/sessions|goals|milestones|plan-sessions/**"| AuthN[authenticated]
  Match -->|other| Public
  AuthN --> Method["@PreAuthorize<br/>ADMIN / STUDENT"]
  Method --> Handler[Controller]
```

| Area | Access |
|---|---|
| `/api/auth/**` | Public |
| Swagger / OpenAPI | Public |
| `/api/sessions/**`, `/api/goals/**`, `/api/milestones/**` | Authenticated (JWT) |
| `/api/plan-sessions/**` | Authenticated + `@PreAuthorize("hasAuthority('STUDENT')")` |
| Admin approvals, login-history, admin settings | `@PreAuthorize("hasAuthority('ADMIN')")` |

Passwords are hashed with **BCrypt**. JWT algorithm: **HS512**.

---

## 9. Scheduled jobs

```mermaid
sequenceDiagram
  participant Cron as GoalOverdueScheduler
  participant Svc as GoalOverdueService
  participant DB as goals table

  Note over Cron: cron = app.goals.overdue-cron<br/>default 0 0 1 * * * (01:00 daily)
  Cron->>Svc: syncOverdueGoals()
  Svc->>DB: ACTIVE/PAUSED where endDate &lt; today → OVERDUE
  Svc-->>Cron: updated count
```

---

## Related configuration

| Setting | Location |
|---|---|
| Datasource, JWT, overdue cron | `src/main/resources/application.properties` |
| CORS origins | `SecurityConfig` + `WebConfig` → `localhost:3000`, `localhost:5173` |
| Seed users & `max.session.hours` | `DataInitializer` |

---

*StudyManager · architecture reference · Spring Boot 4.1.0 · Java 21 · MySQL*
