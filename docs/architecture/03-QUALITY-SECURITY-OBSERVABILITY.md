# MaidFinder Architecture — Part 3: APIs, Quality, Security, Observability

---

## 5. API & Integration Design

### 5.1 API Contract (REST + WebSocket)

**Base URL:** `https://api.maidfinder.in/v1`

**Authentication:** Bearer JWT (RS256) via `Authorization` header

**Rate Limiting:** 100 req/min per user, 1000 req/min global

### 5.2 Key API Endpoints

```
POST   /auth/send-otp          Send OTP to phone
POST   /auth/verify-otp        Verify OTP, return JWT
POST   /auth/refresh           Refresh token

GET    /users/me               Current user profile
PUT    /users/me               Update profile
POST   /users/me/photo         Upload photo

GET    /maids/search           Search nearby maids (PostGIS)
GET    /maids/:id              Maid profile detail
POST   /maids/:id/save         Bookmark maid

GET    /jobs                   List jobs (with filters)
POST   /jobs                   Create job (client)
GET    /jobs/:id               Job detail
POST   /jobs/:id/apply         Apply to job (maid)

GET    /bookings               List bookings
POST   /bookings               Create booking
PUT    /bookings/:id/cancel    Cancel booking
PUT    /bookings/:id/complete  Mark complete

GET    /chats                  List conversations
GET    /chats/:id/messages     Get messages
POST   /chats/:id/messages     Send message (REST fallback)

WS     /ws                     WebSocket (real-time chat + events)

GET    /payments/:id/receipt   Get receipt PDF
```

### 5.3 API Response Format

```typescript
// Success
{
  "success": true,
  "data": { ... },
  "meta": {
    "requestId": "req_abc123",
    "timestamp": "2026-03-31T10:00:00Z"
  }
}

// Paginated
{
  "success": true,
  "data": [ ... ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 47,
    "hasMore": true
  }
}

// Error
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input",
    "details": [{ "field": "phone", "message": "Must be 10 digits" }]
  },
  "meta": { "requestId": "req_abc123" }
}
```

### 5.4 WebSocket Events

```typescript
// Client → Server
{ "event": "authenticate", "data": { "token": "jwt..." } }
{ "event": "join_chat", "data": { "chatId": "conv_001" } }
{ "event": "typing_start", "data": { "chatId": "conv_001" } }
{ "event": "message_read", "data": { "messageIds": [...] } }

// Server → Client
{ "event": "message:new", "data": { "message": { ... } } }
{ "event": "message:read", "data": { "chatId": "...", "messageIds": [...] } }
{ "event": "booking:update", "data": { "booking": { ... } } }
{ "event": "user:online", "data": { "userId": "...", "online": true } }
{ "event": "notification", "data": { "title": "...", "body": "..." } }
```

### 5.5 External Service Integrations

| Service | Purpose | Protocol | Auth |
|---------|---------|----------|------|
| Firebase Auth | Phone OTP verification | REST SDK | Service account |
| Firebase FCM | Push notifications | REST SDK | Service account |
| Razorpay | Payments, escrow | REST API | API keys |
| AWS S3 | Photo/document storage | SDK | IAM role |
| Twilio (backup) | SMS OTP fallback | REST API | API keys |
| Google Maps | Geocoding, maps | REST API | API key |

### 5.6 Synchronous vs Asynchronous

| Operation | Sync/Async | Mechanism |
|-----------|-----------|-----------|
| Auth (OTP) | Sync | REST |
| Job CRUD | Sync | REST |
| Maid search | Sync | REST (with Redis cache) |
| Booking create | Sync | REST |
| Chat messages | Async | WebSocket (+ REST fallback) |
| Push notifications | Async | FCM (fire-and-forget) |
| Payment processing | Sync | REST (with webhook callback) |
| Photo upload | Sync (presigned URL) | S3 presigned |
| Background verification | Async | Queue → webhook |

---

## 6. Quality Attributes & SLOs

### 6.1 Performance

| Metric | Target | Measurement |
|--------|--------|-------------|
| API latency (p50) | < 200ms | Server-side timing |
| API latency (p95) | < 500ms | Server-side timing |
| API latency (p99) | < 1000ms | Server-side timing |
| App cold start | < 2 seconds | Android `reportFullyDrawn()` |
| Screen transition | < 300ms | Compose frame timing |
| Search results | < 500ms | End-to-end |
| Image load (cached) | < 100ms | Coil timing |
| Image load (network) | < 2 seconds | Coil timing |

### 6.2 Scalability

| Metric | Current | 10k Users | 100k Users |
|--------|---------|-----------|------------|
| Concurrent users | 0 | 500 | 5,000 |
| API RPS | 0 | 50 | 500 |
| DB connections | 0 | 20 | 100 |
| WebSocket connections | 0 | 200 | 2,000 |
| Storage | 0 | 5 GB | 80 GB |
| Monthly cost | $0 | $120-180 | $400-600 |

### 6.3 Reliability

| Metric | SLO | Measurement |
|--------|-----|-------------|
| API uptime | 99.9% (< 43 min/month downtime) | Health check endpoint |
| Error rate | < 0.5% of requests | 5xx / total requests |
| Data loss | Zero | Backup verification |
| Recovery time (RTO) | < 1 hour | Incident drill |
| Recovery point (RPO) | < 5 minutes | WAL archiving |

### 6.4 Security

| Metric | Target | Measurement |
|--------|--------|-------------|
| Auth failure rate | < 0.1% false positive | Login analytics |
| Token expiry | 24h access, 30d refresh | Config |
| Password/secret exposure | Zero | Secret scanning |
| OWASP Top 10 compliance | Full | Quarterly audit |

### 6.5 Maintainability

| Metric | Target | Measurement |
|--------|--------|-------------|
| Test coverage | > 80% (domain layer) | JaCoCo / Istanbul |
| Build time | < 3 minutes | CI pipeline |
| Dependency freshness | < 30 days behind latest | Dependabot |
| Code duplication | < 5% | SonarQube |

### 6.6 Observability

| Signal | Tool | Retention |
|--------|------|-----------|
| Logs | CloudWatch / Structured JSON | 30 days |
| Metrics | CloudWatch Metrics + Grafana | 90 days |
| Traces | AWS X-Ray or Jaeger | 7 days |
| Errors | Sentry | 90 days |
| Analytics | Mixpanel | 24 months |

---

## 7. Security & Compliance

### 7.1 Threat Model (STRIDE)

| Threat | Vector | Mitigation |
|--------|--------|------------|
| **Spoofing** | Fake user identity | Firebase Auth phone verification; photo verification |
| **Tampering** | Modified API requests | TLS 1.3; request signing; input validation (Zod) |
| **Repudiation** | User denies action | Audit log on all mutations; signed receipts |
| **Info Disclosure** | Data leak | Field-level encryption for PII; minimal data collection |
| **DoS** | Request flooding | Rate limiting (Redis); CloudFront WAF; autoscaling |
| **Elevation** | Unauthorized access | JWT with role claim; RBAC middleware; resource-level auth |

### 7.2 Authentication Flow

```
┌────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ Client │───►│  Backend  │───►│ Firebase │    │  Backend  │
│        │    │  /send-otp│    │  Auth    │    │  /verify  │
└────────┘    └──────────┘    └──────────┘    └─────┬────┘
                                                     │
                                                     ▼
                                              ┌──────────┐
                                              │   JWT    │
                                              │ Access + │
                                              │ Refresh  │
                                              └──────────┘

Token Structure:
- Access Token:  { sub, role, phone, exp: +24h, iat }
- Refresh Token: { sub, exp: +30d, iat } (stored in Redis)
```

### 7.3 Authorization (RBAC)

```typescript
// Middleware: authorize('CLIENT', 'MAID')
const authorize = (...roles: Role[]) => {
  return async (request: FastifyRequest) => {
    const user = request.user; // from JWT
    if (!roles.includes(user.role)) {
      throw new ForbiddenError('Insufficient permissions');
    }
  };
};

// Resource-level: only own bookings
const authorizeBookingOwner = async (request: FastifyRequest) => {
  const booking = await bookingService.findById(request.params.id);
  const user = request.user;
  if (booking.clientId !== user.id && booking.maidId !== user.id) {
    throw new ForbiddenError('Not your booking');
  }
};
```

### 7.4 Data Protection

| Data | Protection | Storage |
|------|-----------|---------|
| Phone numbers | AES-256 encryption at rest | PostgreSQL |
| ID documents | Encrypted blob; deleted after verification | S3 (encrypted) |
| Location (exact) | Fuzzed to ~500m for non-matches | PostgreSQL |
| Messages | TLS in transit; encrypted at rest | PostgreSQL |
| Auth tokens | HttpOnly secure cookie (web) / DataStore (mobile) | Redis / DataStore |
| Payment data | Never stored; delegated to Razorpay | Razorpay |

### 7.5 Compliance

| Regulation | Applicability | Status |
|-----------|--------------|--------|
| India DPDP Act 2023 | Primary market | Plan for compliance |
| GDPR | Future EU expansion | Data model supports |
| PCI-DSS | Payment processing | Delegated to Razorpay |
| Labor Laws | Domestic worker protection | Consent, fair wage display |

### 7.6 Audit Logging

```typescript
// All mutations produce audit events
interface AuditEvent {
  id: string;
  userId: string;
  action: string;        // 'CREATE_BOOKING', 'CANCEL_BOOKING'
  entityType: string;    // 'booking', 'job'
  entityId: string;
  changes: object;       // before/after snapshot
  ipAddress: string;
  userAgent: string;
  timestamp: Date;
}
```

---

## 8. Observability Plan

### 8.1 Logging Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                     LOGGING PIPELINE                        │
│                                                             │
│  App/Server  ──►  Structured JSON  ──►  CloudWatch Logs    │
│                                                             │
│  Log Levels:                                                │
│  - ERROR:   System failures, unhandled exceptions          │
│  - WARN:    Degraded performance, retry attempts           │
│  - INFO:    Request/response, business events              │
│  - DEBUG:   Detailed flow (disabled in prod)               │
│                                                             │
│  Required Fields:                                           │
│  - requestId (correlation ID)                              │
│  - userId (when authenticated)                             │
│  - duration_ms (for requests)                              │
│  - statusCode (for HTTP)                                   │
│  - error (when applicable)                                 │
└─────────────────────────────────────────────────────────────┘
```

### 8.2 Metrics

| Metric | Type | Labels |
|--------|------|--------|
| `http_requests_total` | Counter | method, path, status |
| `http_request_duration_ms` | Histogram | method, path |
| `ws_connections_active` | Gauge | — |
| `db_query_duration_ms` | Histogram | query_name |
| `cache_hit_ratio` | Gauge | cache_type |
| `auth_attempts_total` | Counter | result (success/fail) |
| `booking_state_transitions` | Counter | from_state, to_state |
| `messages_sent_total` | Counter | type (text/voice) |

### 8.3 Dashboards

| Dashboard | Panels |
|-----------|--------|
| **API Health** | RPS, latency p50/p95/p99, error rate, active connections |
| **Business** | New users/day, bookings/day, GMV, conversion funnel |
| **Infrastructure** | CPU, memory, DB connections, Redis memory, disk |
| **Security** | Failed auth attempts, rate limit hits, suspicious IPs |

### 8.4 Alerting

| Alert | Condition | Severity | Channel |
|-------|-----------|----------|---------|
| API down | Health check fails 3x | P1 Critical | PagerDuty |
| High error rate | > 2% 5xx in 5 min | P1 Critical | PagerDuty |
| High latency | p95 > 1s for 5 min | P2 Warning | Slack |
| DB connections | > 80% pool | P2 Warning | Slack |
| Disk usage | > 85% | P3 Info | Email |

---
