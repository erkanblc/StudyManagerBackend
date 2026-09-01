# StudyManager — Backend API

> REST API for personal study management: learning goals, milestones, study sessions, planning, admin settings, and user administration.

[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-orange)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/Auth-JWT%20HS512-yellow)](https://jwt.io/)

**Languages:** [English](README.md) · [Deutsch](README-DE.md)

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Database](#database)
- [Default Users](#default-users)
- [Authentication](#authentication)
- [Date & Time (UTC)](#date--time-utc)
- [API Reference](#api-reference)
- [Data Models](#data-models)
- [Swagger UI](#swagger-ui)
- [CORS](#cors)
- [Project Structure](#project-structure)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Security | Spring Security + JWT HS512 (jjwt 0.12.6) + refresh tokens |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8.x |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Build | Maven (`mvnw`) |

---

## Getting Started

### Prerequisites

- Java 21+
- MySQL 8.x
- Maven 3.x (or use `./mvnw`)

### 1. Create the database

```sql
CREATE DATABASE study_manager_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configure

Edit `src/main/resources/application.properties` (or override with env vars):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/study_manager_db?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=Europe/Berlin
spring.datasource.username=root
spring.datasource.password=your_password
jwt.secret=StudyManagerSecretKeyHS512VeryLongAndSecure1234567890ABCDEF
jwt.expiration=86400000
jwt.refresh-expiration=604800000
```

### 3. Run

```bash
./mvnw spring-boot:run
```

| | URL |
|---|---|
| API Base | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |

---

## Environment Variables

| Property | Description | Default |
|---|---|---|
| `SPRING_DATASOURCE_URL` | MySQL JDBC URL | `jdbc:mysql://localhost:3306/study_manager_db?...` |
| `SPRING_DATASOURCE_USERNAME` | DB user | `root` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | `12345` |
| `JWT_SECRET` | HS512 signing secret (≥ 64 chars) | (see `application.properties`) |
| `JWT_EXPIRATION` | Access token lifetime (ms) | `86400000` (24h) |
| `JWT_REFRESH_EXPIRATION` | Refresh token lifetime (ms) | `604800000` (7d) |
| `APP_GOALS_OVERDUE_CRON` | Cron for overdue goals | `0 0 1 * * *` |

---

## Database

`spring.jpa.hibernate.ddl-auto=update` — tables are created/updated on startup. No init SQL required.

| Table | Description |
|---|---|
| `users` | Accounts |
| `roles` | `ADMIN`, `STUDENT`, `INSTRUCTOR` |
| `user_roles` | User ↔ role join |
| `goals` | Learning goals |
| `milestones` | Sub-goals |
| `study_sessions` | Timer / manual sessions |
| `plan_sessions` | Planned sessions |
| `login_history` | Login timestamps |
| `app_settings` | App config (e.g. max session hours) |
| `refresh_tokens` | Opaque refresh tokens (if present) |

---

## Default Users

Seeded on startup if missing:

| Username | Email | Password | Role |
|---|---|---|---|
| `admin` | admin@example.com | `admin` | ADMIN |
| `student1` | student1@example.com | `student1` | STUDENT |
| `egitmen` | egitmen@example.com | `egitmen` | INSTRUCTOR |
| `erkan` | erkan@erkan.com | `12345` | ADMIN |

Default setting: `max.session.hours = 6` (clamped range **6–24**).

---

## Authentication

Stateless JWT. Access token (24h) + refresh token (7d).

### Login

```http
POST /api/auth/login
Content-Type: application/json

{ "email": "admin@example.com", "password": "admin" }
```

```json
{
  "id": 1,
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "...",
  "expiresIn": 86400000,
  "email": "admin@example.com",
  "username": "admin",
  "roles": ["ADMIN"],
  "lastLoginAt": "2026-07-17T21:00:00Z"
}
```

### Refresh / Logout

```http
POST /api/auth/refresh
{ "refreshToken": "..." }

POST /api/auth/logout
{ "refreshToken": "..." }
```

### Using the access token

```http
Authorization: Bearer <token>
```

### Register

```http
POST /api/auth/register
```

Students register immediately; admin registrations may require approval (`/api/admin/approvals`).

---

## Date & Time (UTC)

- Server stores and returns datetimes as **ISO 8601 UTC** (`ZonedDateTime`), e.g. `"2026-07-17T21:00:00Z"`.
- JVM + Hibernate JDBC timezone: **UTC**.
- Frontend: `new Date(isoString)` converts to the browser local timezone.
- Date-only fields (`startDate`, `endDate`) remain `YYYY-MM-DD` (`LocalDate`).

---

## API Reference

### Authentication (public)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | Login → access + refresh token |
| POST | `/api/auth/refresh` | New token pair |
| POST | `/api/auth/logout` | Revoke refresh token |
| POST | `/api/auth/register` | Register |

---

### Settings (authenticated)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/settings/max-session-hours` | Current max session hours (+ minutes/seconds) |

---

### Goals (JWT)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/goals` | List my goals |
| GET | `/api/goals/active` | Active goals only |
| POST | `/api/goals` | Create goal |
| PUT | `/api/goals/{id}` | Update goal |
| DELETE | `/api/goals/{id}` | Delete goal |
| POST | `/api/goals/{goalId}/milestones` | Add milestone under goal |
| PATCH | `/api/goals/{goalId}/milestones/{milestoneId}/toggle` | Toggle completion |
| DELETE | `/api/goals/{goalId}/milestones/{milestoneId}` | Delete milestone |

**Status:** `ACTIVE` · `PAUSED` · `OVERDUE` · `COMPLETED` · `CANCELLED` · `ARCHIVED`

```json
{
  "title": "Learn React",
  "description": "Full course",
  "startDate": "2026-07-01",
  "endDate": "2026-12-31",
  "targetHours": 120.0,
  "status": "ACTIVE"
}
```

---

### Milestones (JWT) — standalone

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/milestones` | List my milestones |
| GET | `/api/milestones/{id}` | Get one |
| POST | `/api/milestones` | Create |
| PUT | `/api/milestones/{id}` | Update |
| PATCH | `/api/milestones/{id}/toggle` | Toggle completion |
| DELETE | `/api/milestones/{id}` | Delete |

---

### Study Sessions (JWT)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/sessions` | List sessions |
| GET | `/api/sessions/active` | Active session (204 if none) |
| GET | `/api/sessions/last` | Last session (204 if none) |
| GET | `/api/sessions/total` | `{ "totalSeconds": N }` |
| POST | `/api/sessions/start` | Start stopwatch |
| POST | `/api/sessions/stop` | Stop active session |
| POST | `/api/sessions/manual` | Add past session |
| PATCH | `/api/sessions/{id}/heartbeat` | Sync elapsed seconds |
| POST | `/api/sessions/{id}/resolve` | `SAVE_AT_HEARTBEAT` / `CONTINUE` / `MANUAL` |
| PUT | `/api/sessions/{id}` | Update metadata |
| DELETE | `/api/sessions/{id}` | Delete |

**Session status:** `ACTIVE` · `COMPLETED` · `MANUAL`  
**Heartbeat `duration`:** elapsed seconds (not remaining countdown).  
**Duration limit:** admin setting `max.session.hours` (default 6, max 24).

---

### Plan Sessions (JWT)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/plan-sessions` | All plans |
| GET | `/api/plan-sessions/today` | Today’s plans |
| GET | `/api/plan-sessions/{id}` | One plan |
| POST | `/api/plan-sessions` | Create |
| PUT | `/api/plan-sessions/{id}` | Update |
| PATCH | `/api/plan-sessions/{id}/complete` | Mark completed |
| DELETE | `/api/plan-sessions/{id}` | Delete |

**Type:** `STUDY` · `REVIEW` · `EXAM_PREP` · `PROJECT`  
**Status:** `PLANNED` · `COMPLETED` · `MISSED`  
`plannedDurationMinutes` must be ≤ `max.session.hours × 60`.

---

### Admin — Users

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/users` | List users |
| POST | `/api/admin/users` | Create user |
| GET | `/api/admin/users/{userId}` | Get user |
| PUT | `/api/admin/users/{userId}/status` | Activate / deactivate |
| PUT | `/api/admin/users/{userId}/roles` | Set roles |
| DELETE | `/api/admin/users/{userId}` | Delete user |

---

### Admin — Roles

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/roles` | List roles |
| POST | `/api/admin/roles` | Create role |
| DELETE | `/api/admin/roles/{roleId}` | Delete role |

---

### Admin — Goals

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/goals` | All goals |
| GET | `/api/admin/goals/user/{userId}` | Goals of one user |

---

### Admin — Approvals

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/approvals/pending` | Pending admin registrations |
| GET | `/api/admin/approvals/pending/count` | Count |
| POST | `/api/admin/approvals/{userId}/approve` | Approve |
| POST | `/api/admin/approvals/{userId}/reject` | Reject |

---

### Admin — Login History

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/login-history/user/{userId}` | History of a user |
| GET | `/api/admin/login-history/{id}` | One record |
| PUT | `/api/admin/login-history/{id}` | Update timestamp |
| DELETE | `/api/admin/login-history/{id}` | Delete one |
| DELETE | `/api/admin/login-history/user/{userId}` | Delete all for user |

---

### Admin — Settings

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/settings` | List all settings |
| GET | `/api/admin/settings/{key}` | Get by key |
| PUT | `/api/admin/settings/{key}` | Update |

**Key:** `max.session.hours`  
**Body:** `{ "value": "8" }`  
Rules: whole number only; &lt; 6 → `6`; &gt; 24 → `24`.  
Response includes `updatedAt` and `updatedBy` (admin username, or `null`).

---

## Data Models

### GoalResponse

```json
{
  "id": 1,
  "title": "Learn React",
  "description": "...",
  "startDate": "2026-07-01",
  "endDate": "2026-12-31",
  "targetHours": 120.0,
  "status": "ACTIVE",
  "createdAt": "2026-07-01T10:00:00Z",
  "updatedAt": null,
  "createdByUsername": "student1",
  "milestones": [
    {
      "id": 1,
      "title": "Finish basics",
      "completed": false,
      "completedAt": null,
      "createdAt": "2026-07-01T10:05:00Z"
    }
  ],
  "milestoneCount": 1,
  "completedMilestoneCount": 0
}
```

### StudySessionResponse

```json
{
  "id": 1,
  "startTime": "2026-07-10T09:00:00Z",
  "endTime": "2026-07-10T10:30:00Z",
  "duration": 5400,
  "goalId": 1,
  "subject": "React Hooks",
  "notes": "...",
  "status": "COMPLETED",
  "lastHeartbeatAt": "2026-07-10T10:30:00Z",
  "date": "2026-07-10T09:00:00Z"
}
```

### AppSettingResponse

```json
{
  "id": 1,
  "key": "max.session.hours",
  "value": "6",
  "description": "Maximum allowed study session duration in hours. Min: 6, Max: 24.",
  "updatedAt": "2026-07-17T21:56:00Z",
  "updatedBy": "admin"
}
```

---

## Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

1. Call `POST /api/auth/login` → copy `token`
2. Click **Authorize**
3. Enter `Bearer <token>`

---

## CORS

Allowed origins:

- `http://localhost:3000`
- `http://localhost:5173`

Vite proxy (dev): `/api` → `http://127.0.0.1:8080`

---

## Project Structure

```
src/main/java/com/studymanager/
├── config/                 # Security, DataInitializer, CORS
├── controller/
│   ├── admin/              # Users, roles, goals, approvals
│   ├── auth/               # Login, refresh, register
│   ├── config/             # Settings (admin + public)
│   ├── goal/               # Goals & milestones
│   ├── study/              # Sessions & plan sessions
│   └── user/               # Login history (admin)
├── dto/request|response/
├── entity/
│   ├── config/             # AppSetting
│   ├── goal/               # Goal, Milestone, GoalStatus
│   ├── study/              # StudySession, PlanSession, enums
│   └── user/               # User, Role, LoginHistory
├── repository/
└── service/
```

---

*StudyManager · Spring Boot 4.1.0 · Java 21 · MySQL*
