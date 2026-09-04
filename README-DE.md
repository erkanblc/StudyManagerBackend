# StudyManager — Backend-API

> REST-API für persönliches Lernmanagement: Lernziele, Meilensteine, Lernsitzungen, Planung, Admin-Einstellungen und Benutzerverwaltung.

[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-orange)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/Auth-JWT%20HS512-yellow)](https://jwt.io/)

**Sprachen:** [English](README.md) · [Türkçe](README_TR.md) · [Deutsch](README-DE.md)

**Architektur:** [UML-Diagramme](architecture-uml.md)

---

## Inhaltsverzeichnis

- [Technologie-Stack](#technologie-stack)
- [Schnellstart](#schnellstart)
- [Umgebungsvariablen](#umgebungsvariablen)
- [Datenbank](#datenbank)
- [Standardbenutzer](#standardbenutzer)
- [Authentifizierung](#authentifizierung)
- [Datum & Zeit (UTC)](#datum--zeit-utc)
- [API-Referenz](#api-referenz)
- [Datenmodelle](#datenmodelle)
- [Swagger UI](#swagger-ui)
- [CORS](#cors)
- [Projektstruktur](#projektstruktur)
- [Architektur](#architektur)

---

## Technologie-Stack

| Schicht | Technologie |
|---|---|
| Sprache | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Sicherheit | Spring Security + JWT HS512 (jjwt 0.12.6) + Refresh-Tokens |
| Persistenz | Spring Data JPA + Hibernate |
| Datenbank | MySQL 8.x |
| API-Doku | SpringDoc OpenAPI 3 (Swagger UI) |
| Build | Maven (`mvnw`) |

---

## Schnellstart

### Voraussetzungen

- Java 21+
- MySQL 8.x
- Maven 3.x (oder `./mvnw`)

### 1. Datenbank anlegen

```sql
CREATE DATABASE study_manager_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Konfiguration

`src/main/resources/application.properties` anpassen (oder per Env überschreiben):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/study_manager_db?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=Europe/Berlin
spring.datasource.username=root
spring.datasource.password=your_password
jwt.secret=StudyManagerSecretKeyHS512VeryLongAndSecure1234567890ABCDEF
jwt.expiration=86400000
jwt.refresh-expiration=604800000
```

### 3. Starten

```bash
./mvnw spring-boot:run
```

| | URL |
|---|---|
| API-Basis | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |

---

## Umgebungsvariablen

| Property | Beschreibung | Standard |
|---|---|---|
| `SPRING_DATASOURCE_URL` | MySQL-JDBC-URL | `jdbc:mysql://localhost:3306/study_manager_db?...` |
| `SPRING_DATASOURCE_USERNAME` | DB-Benutzer | `root` |
| `SPRING_DATASOURCE_PASSWORD` | DB-Passwort | `12345` |
| `JWT_SECRET` | HS512-Geheimnis (≥ 64 Zeichen) | (siehe `application.properties`) |
| `JWT_EXPIRATION` | Access-Token-Laufzeit (ms) | `86400000` (24h) |
| `JWT_REFRESH_EXPIRATION` | Refresh-Token-Laufzeit (ms) | `604800000` (7d) |
| `APP_GOALS_OVERDUE_CRON` | Cron für überfällige Ziele | `0 0 1 * * *` |

---

## Datenbank

`spring.jpa.hibernate.ddl-auto=update` — Tabellen werden beim Start automatisch angelegt/aktualisiert. Kein `init.sql` nötig.

| Tabelle | Beschreibung |
|---|---|
| `users` | Benutzerkonten |
| `roles` | `ADMIN`, `STUDENT` (Seed); Konstante `INSTRUCTOR` vorhanden |
| `user_roles` | Benutzer ↔ Rolle |
| `goals` | Lernziele |
| `milestones` | Teilziele (an Ziel gebunden oder eigenständig) |
| `study_sessions` | Timer- / manuelle Sitzungen |
| `plan_sessions` | Geplante Sitzungen |
| `login_history` | Login-Zeitstempel |
| `app_settings` | App-Konfiguration (z. B. max. Sitzungsdauer) |
| `refresh_tokens` | Opaque Refresh-Tokens |

---

## Standardbenutzer

Werden beim Start angelegt, falls nicht vorhanden (`DataInitializer`):

| Username | E-Mail | Passwort | Rolle |
|---|---|---|---|
| `admin` | admin@example.com | `admin` | ADMIN |
| `student1` | student1@example.com | `student1` | STUDENT |
| `erkan` | erkan@erkan.com | `12345` | ADMIN |

Geseedete Rollen: `ADMIN`, `STUDENT`. Die Rolle `INSTRUCTOR` ist im Code definiert und kann über `/api/admin/roles` angelegt werden.

Standardeinstellung: `max.session.hours = 6` (Bereich **6–24**).

---

## Authentifizierung

Zustandsloses JWT. Access-Token (24h) + Refresh-Token (7 Tage).

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

### Access-Token verwenden

```http
Authorization: Bearer <token>
```

### Registrierung

```http
POST /api/auth/register
```

Studierende werden sofort aktiv; Admin-Registrierungen können eine Freigabe brauchen (`/api/admin/approvals`).

---

## Datum & Zeit (UTC)

- Datetimes werden als **ISO 8601 UTC** (`ZonedDateTime`) gespeichert und geliefert, z. B. `"2026-07-17T21:00:00Z"`.
- JVM + Hibernate JDBC-Zeitzone: **UTC**.
- Frontend: `new Date(isoString)` → lokale Browser-Zeit.
- Reine Datumswerte (`startDate`, `endDate`): `YYYY-MM-DD` (`LocalDate`).

---

## API-Referenz

### Authentifizierung (öffentlich)

| Methode | Endpoint | Beschreibung |
|---|---|---|
| POST | `/api/auth/login` | Login → Access + Refresh |
| POST | `/api/auth/refresh` | Neues Token-Paar |
| POST | `/api/auth/logout` | Refresh-Token widerrufen |
| POST | `/api/auth/register` | Registrieren |

---

### Einstellungen (authentifiziert)

| Methode | Endpoint | Beschreibung |
|---|---|---|
| GET | `/api/settings/max-session-hours` | Aktuelle max. Sitzungsdauer (Stunden/Minuten/Sekunden) |

---

### Ziele / Goals (JWT)

| Methode | Endpoint | Beschreibung |
|---|---|---|
| GET | `/api/goals` | Meine Ziele |
| GET | `/api/goals/active` | Nur aktive Ziele |
| POST | `/api/goals` | Ziel anlegen |
| PUT | `/api/goals/{id}` | Ziel aktualisieren |
| DELETE | `/api/goals/{id}` | Ziel löschen |
| POST | `/api/goals/{goalId}/milestones` | Meilenstein unter Ziel |
| PATCH | `/api/goals/{goalId}/milestones/{milestoneId}/toggle` | Erledigt umschalten |
| DELETE | `/api/goals/{goalId}/milestones/{milestoneId}` | Meilenstein löschen |

**Status:** `ACTIVE` · `PAUSED` · `OVERDUE` · `COMPLETED` · `CANCELLED` · `ARCHIVED`

```json
{
  "title": "React lernen",
  "description": "Kompletter Kurs",
  "startDate": "2026-07-01",
  "endDate": "2026-12-31",
  "targetHours": 120.0,
  "status": "ACTIVE"
}
```

---

### Meilensteine (JWT) — eigenständig

| Methode | Endpoint | Beschreibung |
|---|---|---|
| GET | `/api/milestones` | Meine Meilensteine |
| GET | `/api/milestones/{id}` | Einen lesen |
| POST | `/api/milestones` | Anlegen |
| PUT | `/api/milestones/{id}` | Aktualisieren |
| PATCH | `/api/milestones/{id}/toggle` | Erledigt umschalten |
| DELETE | `/api/milestones/{id}` | Löschen |

---

### Lernsitzungen / Study Sessions (JWT)

| Methode | Endpoint | Beschreibung |
|---|---|---|
| GET | `/api/sessions` | Alle Sitzungen |
| GET | `/api/sessions/active` | Aktive Sitzung (204 wenn keine) |
| GET | `/api/sessions/last` | Letzte Sitzung (204 wenn keine) |
| GET | `/api/sessions/total` | `{ "totalSeconds": N }` |
| POST | `/api/sessions/start` | Stoppuhr starten |
| POST | `/api/sessions/stop` | Aktive Sitzung beenden |
| POST | `/api/sessions/manual` | Vergangene Sitzung manuell |
| PATCH | `/api/sessions/{id}/heartbeat` | Vergangene Sekunden sync |
| POST | `/api/sessions/{id}/resolve` | `SAVE_AT_HEARTBEAT` / `CONTINUE` / `MANUAL` |
| PUT | `/api/sessions/{id}` | Metadaten aktualisieren |
| DELETE | `/api/sessions/{id}` | Löschen |

**Sitzungsstatus:** `ACTIVE` · `COMPLETED` · `MANUAL`  
**Heartbeat `duration`:** vergangene Sekunden (nicht Restzeit).  
**Dauerlimit:** Admin-Einstellung `max.session.hours` (Standard 6, max. 24).

---

### Geplante Sitzungen / Plan Sessions (JWT)

| Methode | Endpoint | Beschreibung |
|---|---|---|
| GET | `/api/plan-sessions` | Alle Pläne |
| GET | `/api/plan-sessions/today` | Heutige Pläne |
| GET | `/api/plan-sessions/{id}` | Einen Plan |
| POST | `/api/plan-sessions` | Anlegen |
| PUT | `/api/plan-sessions/{id}` | Aktualisieren |
| PATCH | `/api/plan-sessions/{id}/complete` | Als erledigt markieren |
| DELETE | `/api/plan-sessions/{id}` | Löschen |

**Typ:** `STUDY` · `REVIEW` · `EXAM_PREP` · `PROJECT`  
**Status:** `PLANNED` · `COMPLETED` · `MISSED`  
`plannedDurationMinutes` ≤ `max.session.hours × 60`.

---

### Admin — Benutzer

| Methode | Endpoint | Beschreibung |
|---|---|---|
| GET | `/api/admin/users` | Alle Benutzer |
| POST | `/api/admin/users` | Benutzer anlegen |
| GET | `/api/admin/users/{userId}` | Benutzer lesen |
| PUT | `/api/admin/users/{userId}/status` | Aktivieren / deaktivieren |
| PUT | `/api/admin/users/{userId}/roles` | Rollen setzen |
| DELETE | `/api/admin/users/{userId}` | Löschen |

---

### Admin — Rollen

| Methode | Endpoint | Beschreibung |
|---|---|---|
| GET | `/api/admin/roles` | Alle Rollen |
| POST | `/api/admin/roles` | Rolle anlegen |
| DELETE | `/api/admin/roles/{roleId}` | Rolle löschen |

---

### Admin — Ziele

| Methode | Endpoint | Beschreibung |
|---|---|---|
| GET | `/api/admin/goals` | Alle Ziele |
| GET | `/api/admin/goals/user/{userId}` | Ziele eines Benutzers |

---

### Admin — Freigaben

| Methode | Endpoint | Beschreibung |
|---|---|---|
| GET | `/api/admin/approvals/pending` | Ausstehende Admin-Registrierungen |
| GET | `/api/admin/approvals/pending/count` | Anzahl |
| POST | `/api/admin/approvals/{userId}/approve` | Genehmigen |
| POST | `/api/admin/approvals/{userId}/reject` | Ablehnen |

---

### Admin — Login-Historie

| Methode | Endpoint | Beschreibung |
|---|---|---|
| GET | `/api/admin/login-history/user/{userId}` | Historie eines Benutzers |
| GET | `/api/admin/login-history/{id}` | Ein Eintrag |
| PUT | `/api/admin/login-history/{id}` | Zeitstempel ändern |
| DELETE | `/api/admin/login-history/{id}` | Einen löschen |
| DELETE | `/api/admin/login-history/user/{userId}` | Alle eines Benutzers löschen |

---

### Admin — Einstellungen

| Methode | Endpoint | Beschreibung |
|---|---|---|
| GET | `/api/admin/settings` | Alle Einstellungen |
| GET | `/api/admin/settings/{key}` | Nach Key |
| PUT | `/api/admin/settings/{key}` | Aktualisieren |

**Key:** `max.session.hours`  
**Body:** `{ "value": "8" }`  
Regeln: nur ganze Zahl; &lt; 6 → `6`; &gt; 24 → `24`.  
Response enthält `updatedAt` und `updatedBy` (Admin-Username oder `null`).

---

## Datenmodelle

### GoalResponse

```json
{
  "id": 1,
  "title": "React lernen",
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
      "title": "Grundlagen",
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

1. `POST /api/auth/login` → `token` kopieren
2. **Authorize** klicken
3. `Bearer <token>` eintragen

---

## CORS

Erlaubte Origins:

- `http://localhost:3000`
- `http://localhost:5173`

Vite-Proxy (Dev): `/api` → `http://127.0.0.1:8080`

---

## Projektstruktur

```
src/main/java/com/studymanager/
├── config/                 # Security, DataInitializer, CORS, OpenAPI
│   └── security/           # JwtAuthFilter, JwtUtils, SecurityConfig
├── controller/
│   ├── admin/              # Freigaben, Rollen, Admin-Ziele
│   ├── auth/               # Login, Refresh, Register
│   ├── config/             # Settings (Admin + authentifiziert)
│   ├── goal/               # Goals & verschachtelte Milestones
│   ├── study/              # Sessions & Plan Sessions
│   └── user/               # Benutzer & Login-Historie (Admin)
├── dto/request|response/
├── entity/
│   ├── config/             # AppSetting
│   ├── goal/               # Goal, Milestone, GoalStatus
│   ├── study/              # StudySession, PlanSession, Enums
│   └── user/               # User, Role, LoginHistory, RefreshToken
├── repository/
├── scheduler/              # GoalOverdueScheduler (täglicher OVERDUE-Job)
└── service/
```

---

## Architektur

Geschichtete Spring-Boot-API: Controllers → Services → Repositories → MySQL.

Auth ist zustandsloses JWT (Access + Refresh). Admin-Endpunkte nutzen `@PreAuthorize("hasAuthority('ADMIN')")`; Plan-Sessions erfordern `STUDENT`.

Vollständige Diagramme (Komponenten, Sequenz, ER, Pakete): **[architecture-uml.md](architecture-uml.md)**

---

*StudyManager · Spring Boot 4.1.0 · Java 21 · MySQL*
