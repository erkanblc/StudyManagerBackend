# StudyManager — Backend API

> Kişisel çalışma yönetimi için REST API: öğrenme hedefleri, kilometre taşları, çalışma seansları, planlama, yönetici ayarları ve kullanıcı yönetimi.

[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-orange)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/Auth-JWT%20HS512-yellow)](https://jwt.io/)

**Diller:** [English](README.md) · [Türkçe](README_TR.md) · [Deutsch](README-DE.md)

**Mimari:** [UML diyagramları](architecture-uml.md)

---

## İçindekiler

- [Teknoloji Yığını](#teknoloji-yığını)
- [Başlangıç](#başlangıç)
- [Ortam Değişkenleri](#ortam-değişkenleri)
- [Veritabanı](#veritabanı)
- [Varsayılan Kullanıcılar](#varsayılan-kullanıcılar)
- [Kimlik Doğrulama](#kimlik-doğrulama)
- [Tarih & Saat (UTC)](#tarih--saat-utc)
- [API Referansı](#api-referansı)
- [Veri Modelleri](#veri-modelleri)
- [Swagger UI](#swagger-ui)
- [CORS](#cors)
- [Proje Yapısı](#proje-yapısı)
- [Mimari](#mimari)

---

## Teknoloji Yığını

| Katman | Teknoloji |
|---|---|
| Dil | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Güvenlik | Spring Security + JWT HS512 (jjwt 0.12.6) + refresh token |
| Kalıcılık | Spring Data JPA + Hibernate |
| Veritabanı | MySQL 8.x |
| API Dokümantasyonu | SpringDoc OpenAPI 3 (Swagger UI) |
| Build | Maven (`mvnw`) |

---

## Başlangıç

### Gereksinimler

- Java 21+
- MySQL 8.x
- Maven 3.x (veya `./mvnw` / `mvnw.cmd`)

### 1. Veritabanını oluşturun

```sql
CREATE DATABASE study_manager_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Yapılandırma

`src/main/resources/application.properties` dosyasını düzenleyin (veya ortam değişkenleriyle override edin):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/study_manager_db?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=Europe/Berlin
spring.datasource.username=root
spring.datasource.password=your_password
jwt.secret=StudyManagerSecretKeyHS512VeryLongAndSecure1234567890ABCDEF
jwt.expiration=86400000
jwt.refresh-expiration=604800000
```

### 3. Çalıştırma

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

| | URL |
|---|---|
| API Base | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |

---

## Ortam Değişkenleri

| Property | Açıklama | Varsayılan |
|---|---|---|
| `SPRING_DATASOURCE_URL` | MySQL JDBC URL | `jdbc:mysql://localhost:3306/study_manager_db?...` |
| `SPRING_DATASOURCE_USERNAME` | DB kullanıcı | `root` |
| `SPRING_DATASOURCE_PASSWORD` | DB şifre | `12345` |
| `JWT_SECRET` | HS512 imza anahtarı (≥ 64 karakter) | (`application.properties`) |
| `JWT_EXPIRATION` | Access token süresi (ms) | `86400000` (24s) |
| `JWT_REFRESH_EXPIRATION` | Refresh token süresi (ms) | `604800000` (7g) |
| `APP_GOALS_OVERDUE_CRON` | Gecikmiş hedefler için cron | `0 0 1 * * *` |

---

## Veritabanı

`spring.jpa.hibernate.ddl-auto=update` — tablolar başlangıçta otomatik oluşturulur/güncellenir. Ayrı `init.sql` gerekmez.

| Tablo | Açıklama |
|---|---|
| `users` | Hesaplar |
| `roles` | `ADMIN`, `STUDENT` (seed); `INSTRUCTOR` sabiti kodda var |
| `user_roles` | Kullanıcı ↔ rol |
| `goals` | Öğrenme hedefleri |
| `milestones` | Alt hedefler (hedefe bağlı veya bağımsız) |
| `study_sessions` | Zamanlayıcı / manuel seanslar |
| `plan_sessions` | Planlanan seanslar |
| `login_history` | Giriş zaman damgaları |
| `app_settings` | Uygulama ayarları (ör. max seans saati) |
| `refresh_tokens` | Opaque refresh token’lar |

---

## Varsayılan Kullanıcılar

Eksikse başlangıçta oluşturulur (`DataInitializer`):

| Username | E-posta | Şifre | Rol |
|---|---|---|---|
| `admin` | admin@example.com | `admin` | ADMIN |
| `student1` | student1@example.com | `student1` | STUDENT |
| `erkan` | erkan@erkan.com | `12345` | ADMIN |

Seed edilen roller: `ADMIN`, `STUDENT`. `INSTRUCTOR` rol adı kodda tanımlıdır; `/api/admin/roles` ile oluşturulabilir.

Varsayılan ayar: `max.session.hours = 6` (aralık **6–24**).

---

## Kimlik Doğrulama

Durumsuz JWT. Access token (24s) + refresh token (7g).

### Giriş

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

### Access token kullanımı

```http
Authorization: Bearer <token>
```

### Kayıt

```http
POST /api/auth/register
```

Öğrenciler hemen aktif olur; admin kayıtları onay gerektirebilir (`/api/admin/approvals`).

---

## Tarih & Saat (UTC)

- Sunucu datetime değerlerini **ISO 8601 UTC** (`ZonedDateTime`) olarak saklar ve döner, örn. `"2026-07-17T21:00:00Z"`.
- JVM + Hibernate JDBC saat dilimi: **UTC**.
- Frontend: `new Date(isoString)` tarayıcı yerel saatine çevirir.
- Salt tarih alanları (`startDate`, `endDate`): `YYYY-MM-DD` (`LocalDate`).

---

## API Referansı

### Kimlik doğrulama (herkese açık)

| Method | Endpoint | Açıklama |
|---|---|---|
| POST | `/api/auth/login` | Giriş → access + refresh token |
| POST | `/api/auth/refresh` | Yeni token çifti |
| POST | `/api/auth/logout` | Refresh token iptali |
| POST | `/api/auth/register` | Kayıt |

---

### Ayarlar (kimlik doğrulamalı)

| Method | Endpoint | Açıklama |
|---|---|---|
| GET | `/api/settings/max-session-hours` | Max seans süresi (saat/dakika/saniye) |

---

### Hedefler / Goals (JWT)

| Method | Endpoint | Açıklama |
|---|---|---|
| GET | `/api/goals` | Hedeflerim |
| GET | `/api/goals/active` | Yalnızca aktif hedefler |
| POST | `/api/goals` | Hedef oluştur |
| PUT | `/api/goals/{id}` | Hedef güncelle |
| DELETE | `/api/goals/{id}` | Hedef sil |
| POST | `/api/goals/{goalId}/milestones` | Hedefe kilometre taşı ekle |
| PATCH | `/api/goals/{goalId}/milestones/{milestoneId}/toggle` | Tamamlanma durumunu değiştir |
| DELETE | `/api/goals/{goalId}/milestones/{milestoneId}` | Kilometre taşını sil |

**Durum:** `ACTIVE` · `PAUSED` · `OVERDUE` · `COMPLETED` · `CANCELLED` · `ARCHIVED`

```json
{
  "title": "React öğren",
  "description": "Tam kurs",
  "startDate": "2026-07-01",
  "endDate": "2026-12-31",
  "targetHours": 120.0,
  "status": "ACTIVE"
}
```

---

### Kilometre taşları (JWT) — bağımsız

| Method | Endpoint | Açıklama |
|---|---|---|
| GET | `/api/milestones` | Kilometre taşlarım |
| GET | `/api/milestones/{id}` | Birini getir |
| POST | `/api/milestones` | Oluştur |
| PUT | `/api/milestones/{id}` | Güncelle |
| PATCH | `/api/milestones/{id}/toggle` | Tamamlanma durumunu değiştir |
| DELETE | `/api/milestones/{id}` | Sil |

---

### Çalışma seansları / Study Sessions (JWT)

| Method | Endpoint | Açıklama |
|---|---|---|
| GET | `/api/sessions` | Seans listesi |
| GET | `/api/sessions/active` | Aktif seans (yoksa 204) |
| GET | `/api/sessions/last` | Son seans (yoksa 204) |
| GET | `/api/sessions/total` | `{ "totalSeconds": N }` |
| POST | `/api/sessions/start` | Kronometreyi başlat |
| POST | `/api/sessions/stop` | Aktif seansı durdur |
| POST | `/api/sessions/manual` | Geçmiş seans ekle |
| PATCH | `/api/sessions/{id}/heartbeat` | Geçen saniyeyi senkronize et |
| POST | `/api/sessions/{id}/resolve` | `SAVE_AT_HEARTBEAT` / `CONTINUE` / `MANUAL` |
| PUT | `/api/sessions/{id}` | Meta veriyi güncelle |
| DELETE | `/api/sessions/{id}` | Sil |

**Seans durumu:** `ACTIVE` · `COMPLETED` · `MANUAL`  
**Heartbeat `duration`:** geçen saniye (kalan süre değil).  
**Süre limiti:** admin ayarı `max.session.hours` (varsayılan 6, max 24).

---

### Plan seansları / Plan Sessions (JWT, STUDENT)

| Method | Endpoint | Açıklama |
|---|---|---|
| GET | `/api/plan-sessions` | Tüm planlar |
| GET | `/api/plan-sessions/today` | Bugünün planları |
| GET | `/api/plan-sessions/{id}` | Tek plan |
| POST | `/api/plan-sessions` | Oluştur |
| PUT | `/api/plan-sessions/{id}` | Güncelle |
| PATCH | `/api/plan-sessions/{id}/complete` | Tamamlandı işaretle |
| DELETE | `/api/plan-sessions/{id}` | Sil |

**Tip:** `STUDY` · `REVIEW` · `EXAM_PREP` · `PROJECT`  
**Durum:** `PLANNED` · `COMPLETED` · `MISSED`  
`plannedDurationMinutes` ≤ `max.session.hours × 60`.

---

### Admin — Kullanıcılar

| Method | Endpoint | Açıklama |
|---|---|---|
| GET | `/api/admin/users` | Kullanıcı listesi |
| POST | `/api/admin/users` | Kullanıcı oluştur |
| GET | `/api/admin/users/{userId}` | Kullanıcı getir |
| PUT | `/api/admin/users/{userId}/status` | Aktif / pasif |
| PUT | `/api/admin/users/{userId}/roles` | Rolleri ayarla |
| DELETE | `/api/admin/users/{userId}` | Kullanıcı sil |

---

### Admin — Roller

| Method | Endpoint | Açıklama |
|---|---|---|
| GET | `/api/admin/roles` | Rol listesi |
| POST | `/api/admin/roles` | Rol oluştur |
| DELETE | `/api/admin/roles/{roleId}` | Rol sil |

---

### Admin — Hedefler

| Method | Endpoint | Açıklama |
|---|---|---|
| GET | `/api/admin/goals` | Tüm hedefler |
| GET | `/api/admin/goals/user/{userId}` | Bir kullanıcının hedefleri |

---

### Admin — Onaylar

| Method | Endpoint | Açıklama |
|---|---|---|
| GET | `/api/admin/approvals/pending` | Bekleyen admin kayıtları |
| GET | `/api/admin/approvals/pending/count` | Sayı |
| POST | `/api/admin/approvals/{userId}/approve` | Onayla |
| POST | `/api/admin/approvals/{userId}/reject` | Reddet |

---

### Admin — Giriş geçmişi

| Method | Endpoint | Açıklama |
|---|---|---|
| GET | `/api/admin/login-history/user/{userId}` | Kullanıcının geçmişi |
| GET | `/api/admin/login-history/{id}` | Tek kayıt |
| PUT | `/api/admin/login-history/{id}` | Zaman damgasını güncelle |
| DELETE | `/api/admin/login-history/{id}` | Tek kaydı sil |
| DELETE | `/api/admin/login-history/user/{userId}` | Kullanıcının tümünü sil |

---

### Admin — Ayarlar

| Method | Endpoint | Açıklama |
|---|---|---|
| GET | `/api/admin/settings` | Tüm ayarlar |
| GET | `/api/admin/settings/{key}` | Anahtara göre getir |
| PUT | `/api/admin/settings/{key}` | Güncelle |

**Anahtar:** `max.session.hours`  
**Body:** `{ "value": "8" }`  
Kurallar: yalnızca tam sayı; &lt; 6 → `6`; &gt; 24 → `24`.  
Yanıtta `updatedAt` ve `updatedBy` (admin kullanıcı adı veya `null`) bulunur.

---

## Veri Modelleri

### GoalResponse

```json
{
  "id": 1,
  "title": "React öğren",
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
      "title": "Temelleri bitir",
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

1. `POST /api/auth/login` çağırın → `token` kopyalayın
2. **Authorize** tıklayın
3. `Bearer <token>` girin

---

## CORS

İzin verilen origin’ler:

- `http://localhost:3000`
- `http://localhost:5173`

Vite proxy (dev): `/api` → `http://127.0.0.1:8080`

---

## Proje Yapısı

```
src/main/java/com/studymanager/
├── config/                 # Security, DataInitializer, CORS, OpenAPI
│   └── security/           # JwtAuthFilter, JwtUtils, SecurityConfig
├── controller/
│   ├── admin/              # Onaylar, roller, admin hedefleri
│   ├── auth/               # Login, refresh, register
│   ├── config/             # Ayarlar (admin + kimlik doğrulamalı)
│   ├── goal/               # Hedefler ve iç içe kilometre taşları
│   ├── study/              # Seanslar ve plan seansları
│   └── user/               # Kullanıcılar ve giriş geçmişi (admin)
├── dto/request|response/
├── entity/
│   ├── config/             # AppSetting
│   ├── goal/               # Goal, Milestone, GoalStatus
│   ├── study/              # StudySession, PlanSession, enum’lar
│   └── user/               # User, Role, LoginHistory, RefreshToken
├── repository/
├── scheduler/              # GoalOverdueScheduler (günlük OVERDUE işi)
└── service/
```

---

## Mimari

Katmanlı Spring Boot API: Controllers → Services → Repositories → MySQL.

Kimlik doğrulama durumuz JWT (access + refresh). Admin uçları `@PreAuthorize("hasAuthority('ADMIN')")` kullanır; plan seansları `STUDENT` gerektirir.

Tam diyagramlar (bileşen, sıra, ER, paket): **[architecture-uml.md](architecture-uml.md)**

---

*StudyManager · Spring Boot 4.1.0 · Java 21 · MySQL*
