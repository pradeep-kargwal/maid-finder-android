# MaidFinder Architecture — Part 4: Deployment, Migration, Testing, Plan

---

## 9. Deployment & Operations

### 9.1 CI/CD Pipeline

```
┌─────────────────────────────────────────────────────────────────┐
│                         CI/CD PIPELINE                          │
│                                                                 │
│  ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐       │
│  │  Push   │──►│  Build  │──►│  Test   │──►│  Deploy │       │
│  │  (Git)  │   │         │   │         │   │         │       │
│  └─────────┘   └─────────┘   └─────────┘   └─────────┘       │
│                                                                 │
│  Backend:                                                       │
│  Push → lint → typecheck → unit test → build Docker image →    │
│  push to ECR → deploy to ECS (staging) → e2e test →            │
│  manual gate → deploy to ECS (production)                       │
│                                                                 │
│  Android:                                                       │
│  Push → lint → unit test → assembleDebug → instrumented test → │
│  assembleRelease → sign → upload to Play Store (internal)      │
└─────────────────────────────────────────────────────────────────┘
```

### 9.2 Environment Segmentation

| Environment | Purpose | Backend | Database | URL |
|-------------|---------|---------|----------|-----|
| **local** | Development | localhost:3000 | Local PostgreSQL | — |
| **staging** | Integration testing | ECS (t3.micro) | RDS (db.t4g.micro) | api-staging.maidfinder.in |
| **production** | Live users | ECS (2x t3.small) | RDS (db.t4g.small, multi-AZ) | api.maidfinder.in |

### 9.3 Release Strategy

| Branch | Deploys To | Gate |
|--------|-----------|------|
| `feature/*` | — | PR review + CI |
| `develop` | staging | Auto-deploy on merge |
| `main` | production | Manual approval + CI |
| `hotfix/*` | production | Expedited review |

### 9.4 Infrastructure as Code

```yaml
# docker-compose.yml (local development)
services:
  api:
    build: ./maidfinder-api
    ports: ["3000:3000"]
    env_file: .env
    depends_on: [postgres, redis]

  postgres:
    image: postgis/postgis:16-3.4
    environment:
      POSTGRES_DB: maidfinder
      POSTGRES_PASSWORD: localdev
    ports: ["5432:5432"]
    volumes: ["pgdata:/var/lib/postgresql/data"]

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]
```

```hcl
# terraform/ecs.tf (production)
resource "aws_ecs_service" "api" {
  name            = "maidfinder-api"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.api.arn
  desired_count   = 2

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }
}
```

### 9.5 Rollback Options

| Scenario | Rollback Method | Time |
|----------|----------------|------|
| Bad backend deploy | ECS deployment rollback (1 click) | < 5 min |
| Bad DB migration | `knex migrate:rollback` | < 10 min |
| Bad Android release | Play Store halt rollout | < 1 hour |
| Data corruption | RDS point-in-time recovery | < 30 min |

---

## 10. Migration Strategy

### 10.1 Current → Target: Incremental Steps

```
Phase 0 (NOW)          Phase 1 (Week 1-4)       Phase 2 (Week 5-8)
┌──────────────┐       ┌──────────────┐         ┌──────────────┐
│ In-Memory    │       │ Room DB      │         │ Room + API   │
│ Service      │       │ Hilt DI      │         │ Backend      │
│ Locator      │  ──►  │ Clean Arch   │   ──►   │ Auth         │
│ Single       │       │ Multi-module │         │ Real data    │
│ module       │       │ Unit tests   │         │ Chat (WS)    │
│ No backend   │       │              │         │ Payments     │
└──────────────┘       └──────────────┘         └──────────────┘

Phase 3 (Week 9-12)    Phase 4 (Post-MVP)
┌──────────────┐       ┌──────────────┐
│ Production   │       │ iOS app      │
│ Deploy       │  ──►  │ ML matching  │
│ Monitoring   │       │ i18n         │
│ Beta users   │       │ Advanced     │
│ Payments     │       │ analytics    │
│ Safety feat  │       │              │
└──────────────┘       └──────────────┘
```

### 10.2 Data Migration Plan

| Step | Action | Risk | Rollback |
|------|--------|------|----------|
| 1 | Add Room entities matching current models | Low | Remove Room |
| 2 | Create DAO + repository impl wrapping Room | Low | Keep in-memory |
| 3 | Migrate from in-memory to Room (one repo at a time) | Medium | Swap back to in-memory |
| 4 | Add Retrofit services matching API spec | Low | Not connected yet |
| 5 | Add network data source behind repository | Medium | Use Room-only |
| 6 | Create backend, seed with sample data | Low | N/A |
| 7 | Switch repositories to network-first + Room cache | High | Revert to Room-only |

### 10.3 Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Room migration breaks existing UI | Medium | High | Feature-flagged; test with existing screens |
| Backend API contract mismatch | Medium | Medium | Contract tests; shared OpenAPI spec |
| Performance regression from Room | Low | Medium | Benchmark queries; index optimization |
| Hilt migration introduces bugs | Medium | Medium | Migrate one ViewModel at a time |
| Firebase Auth region issues (India) | Low | High | Test early; Twilio SMS fallback |

---

## 11. Testing Strategy

### 11.1 Testing Pyramid

```
                    ┌─────────┐
                    │  E2E    │  5%   (Critical flows only)
                    │  Tests  │
                   ┌┴─────────┴┐
                   │Integration │ 15%  (API contract, DB)
                   │   Tests    │
                  ┌┴────────────┴┐
                  │  Unit Tests   │ 80%  (Domain, ViewModel)
                  │               │
                  └───────────────┘
```

### 11.2 Test Categories

| Category | Scope | Tool | Target Coverage |
|----------|-------|------|-----------------|
| **Unit** | UseCases, ViewModels, repositories | JUnit 5 + MockK | > 80% |
| **Integration** | Room DAOs, Retrofit services | Robolectric + MockWebServer | > 60% |
| **Contract** | API request/response format | Pact or manual OpenAPI validation | 100% endpoints |
| **UI** | Compose screens | Compose UI Test | Critical flows |
| **E2E** | Full user journeys | Espresso + backend test env | Login, booking, chat |

### 11.3 Test Examples

**Unit Test (ViewModel):**
```kotlin
@Test
fun `loadMaids updates state with results`() = runTest {
    val fakeRepo = FakeMaidRepository(sampleMaids)
    val vm = MaidListViewModel(fakeRepo)

    vm.loadMaids()

    val state = vm.uiState.value
    assertFalse(state.isLoading)
    assertEquals(5, state.maids.size)
}
```

**Integration Test (API):**
```kotlin
@Test
fun `getJobs returns paginated results`() = runTest {
    mockWebServer.enqueue(MockResponse().setBody(jobsJson).setResponseCode(200))

    val response = apiService.getJobs(lat = 17.38, lng = 78.48, radius = 5, page = 1)

    assertTrue(response.success)
    assertEquals(5, response.data.size)
}
```

**Backend Unit Test:**
```typescript
test('createJob validates input and returns job', async () => {
  const response = await app.inject({
    method: 'POST',
    url: '/api/v1/jobs',
    headers: { authorization: `Bearer ${testToken}` },
    payload: { jobType: 'PART_TIME', title: 'Cleaning', ... }
  });

  expect(response.statusCode).toBe(201);
  expect(response.json().data.id).toBeDefined();
});
```

### 11.4 Recommended Tooling

| Purpose | Android | Backend |
|---------|---------|---------|
| Test runner | JUnit 5 | Vitest / Jest |
| Mocking | MockK | jest.mock / sinon |
| HTTP mocking | MockWebServer | fastify.inject |
| Coverage | JaCoCo | c8 / Istanbul |
| CI | GitHub Actions | GitHub Actions |
| E2E | Espresso | Supertest |

---

## 12. Implementation Plan

### 12.1 Phased Milestones

#### Phase 1: Foundation (Weeks 1-4) — "Solid Ground"

| Week | Task | Owner | Deliverable |
|------|------|-------|-------------|
| 1 | Set up multi-module Gradle structure | Android dev | 6 modules compiling |
| 1 | Integrate Hilt DI | Android dev | All ViewModels injected |
| 2 | Add Room database + entities | Android dev | Local persistence working |
| 2 | Migrate repositories to Room-backed | Android dev | Data survives process death |
| 3 | Add unit tests for domain layer | Android dev | >80% domain coverage |
| 3 | Set up backend skeleton (Fastify) | Backend dev | Health endpoint live |
| 4 | Add Retrofit + API service interfaces | Android dev | Network layer ready |
| 4 | Backend: Auth module (Firebase + JWT) | Backend dev | OTP flow working |

**Phase 1 Exit Criteria:**
- [ ] App persists data locally (Room)
- [ ] Hilt provides all dependencies
- [ ] Backend accepts requests and returns mock data
- [ ] Auth flow works end-to-end
- [ ] 80%+ unit test coverage on domain

#### Phase 2: Core Features (Weeks 5-8) — "Connected"

| Week | Task | Owner | Deliverable |
|------|------|-------|-------------|
| 5 | Backend: Jobs CRUD + search (PostGIS) | Backend dev | Job API live |
| 5 | Android: Connect job screens to real API | Android dev | Jobs load from backend |
| 6 | Backend: Booking lifecycle | Backend dev | Booking API live |
| 6 | Android: Connect booking screens | Android dev | Bookings sync |
| 7 | Backend: WebSocket chat server | Backend dev | Real-time messages |
| 7 | Android: Chat with real-time sync | Android dev | Chat works end-to-end |
| 8 | Backend: Payments (Razorpay) | Backend dev | Payment flow live |
| 8 | Android: Payment integration | Android dev | Booking with payment |

**Phase 2 Exit Criteria:**
- [ ] All CRUD operations work with real backend
- [ ] Chat messages sync in real-time
- [ ] Payment flow works (test mode)
- [ ] Offline queue syncs on reconnect

#### Phase 3: Production Ready (Weeks 9-12) — "Launch"

| Week | Task | Owner | Deliverable |
|------|------|-------|-------------|
| 9 | Infrastructure (AWS setup, CI/CD) | DevOps/Backend | Staging environment |
| 9 | Security hardening | All | Encryption, rate limiting |
| 10 | Integration + E2E tests | All | Critical path coverage |
| 10 | Performance optimization | All | SLOs met |
| 11 | Beta testing (50 users) | All | Feedback collected |
| 11 | Bug fixes + polish | All | Stability |
| 12 | Production deployment | DevOps | Live app |
| 12 | Monitoring + alerting | DevOps | Dashboards active |

### 12.2 Team Responsibilities

| Role | Primary | Secondary |
|------|---------|-----------|
| **Android Developer** | Client architecture, Room, Compose, tests | API contract validation |
| **Backend Developer** | API, database, WebSocket, payments | Infrastructure, CI/CD |
| **Full-Stack / Lead** | Architecture decisions, code review, integration | Testing strategy, security |

### 12.3 Estimated Effort

| Phase | Effort (person-weeks) | Calendar Time |
|-------|----------------------|---------------|
| Phase 1: Foundation | 8 pw | 4 weeks (2 devs) |
| Phase 2: Core | 10 pw | 4 weeks (2.5 devs) |
| Phase 3: Production | 8 pw | 4 weeks (2 devs) |
| **Total** | **26 pw** | **12 weeks** |

---

## 13. Quick Wins & Roadmap

### 13.1 Immediate Quick Wins (Deploy This Week)

| # | Action | Effort | Impact |
|---|--------|--------|--------|
| 1 | Add Room database for offline caching | 2 days | Data survives app restart |
| 2 | Replace ServiceLocator with Hilt | 1 day | Compile-time DI safety |
| 3 | Add unit tests for ViewModels | 2 days | Confidence in refactoring |
| 4 | Add structured logging (Timber) | 0.5 day | Debug production issues |
| 5 | Add API error handling + retry | 1 day | Resilience |
| 6 | Add crash reporting (Sentry) | 0.5 day | Visibility into crashes |

### 13.2 Long-Term Roadmap

| Quarter | Milestone |
|---------|-----------|
| Q2 2026 | MVP launch (Android + backend, India) |
| Q3 2026 | iOS app (Kotlin Multiplatform shared logic) |
| Q3 2026 | i18n (Hindi, Telugu, Tamil) |
| Q4 2026 | ML-based matching, recurring bookings |
| Q1 2027 | International expansion (UAE, Philippines) |
| Q2 2027 | Video profiles, skill certification |

---

## 14. Decision Log

| # | Decision | Options Considered | Chosen | Rationale |
|---|----------|-------------------|--------|-----------|
| D1 | Android architecture | MVI, MVVM, MVP | MVVM | Team familiarity; Compose state maps to StateFlow naturally |
| D2 | DI framework | Hilt, Koin, Manual | Hilt | Compile-time safety; Google-maintained; lifecycle-aware |
| D3 | Local database | Room, SQLDelight, Realm | Room | Largest community; best Compose integration; Google-backed |
| D4 | Network client | Retrofit, Ktor Client | Retrofit | Industry standard; OkHttp interceptor ecosystem |
| D5 | Backend runtime | Node.js, Python, Go | Node.js (Fastify) | Type-safe (TS); fast iteration; shared types possible |
| D6 | Backend database | PostgreSQL, MongoDB | PostgreSQL | ACID for bookings/payments; PostGIS for geospatial |
| D7 | Real-time | WebSocket, SSE, Polling | WebSocket (Socket.IO) | Bidirectional; chat requires push; battle-tested |
| D8 | Auth | Firebase Auth, Custom OTP | Firebase Auth | Phone verification built-in; reduces custom SMS code |
| D9 | Payments | Razorpay, Stripe | Razorpay | Best India coverage; escrow support; UPI native |
| D10 | Monolith vs Microservices | Both | Monolith | 3-person team; 100k users; operational simplicity |
| D11 | Multi-module vs Single | Both | Multi-module | Faster build times; clear boundaries; parallel development |
| D12 | Offline strategy | Cache-first, Network-first | Network-first with Room cache | Fresh data preferred; offline is fallback |

---

## 15. Acceptance Criteria & Success Metrics

### 15.1 Architecture Validation Checklist

| # | Criterion | Measurable | Target |
|---|-----------|-----------|--------|
| 1 | Multi-module builds successfully | `./gradlew assembleDebug` | < 3 min |
| 2 | All repositories backed by Room | Data survives `adb shell am force-stop` | Yes/No |
| 3 | Hilt provides all dependencies | No manual instantiation in ViewModels | 0 ServiceLocator refs |
| 4 | Backend responds to health check | `GET /health` returns 200 | < 200ms |
| 5 | Auth flow works end-to-end | OTP → JWT → protected endpoint | Yes/No |
| 6 | CRUD operations use real API | Jobs/Bookings load from backend | Yes/No |
| 7 | Chat works in real-time | Message delivered < 1s on same network | Yes/No |
| 8 | Offline queue syncs | Message sent offline, delivered on reconnect | Yes/No |
| 9 | Unit test coverage > 80% | JaCoCo report | domain layer |
| 10 | API latency p95 < 500ms | CloudWatch metrics | < 500ms |
| 11 | App cold start < 2s | `reportFullyDrawn()` | < 2000ms |
| 12 | Zero P1 security findings | OWASP scan | 0 findings |

### 15.2 Business Success Metrics (Post-Launch)

| Metric | 30-Day Target | 90-Day Target |
|--------|---------------|---------------|
| Registered users | 1,000 | 10,000 |
| Weekly active users | 200 | 2,000 |
| Successful bookings/week | 50 | 500 |
| App store rating | ≥ 4.0 | ≥ 4.3 |
| Client retention (Week 4) | 30% | 40% |
| Maid retention (Week 4) | 40% | 50% |
| API uptime | 99.9% | 99.9% |

### 15.3 How to Validate

```bash
# Build validation
./gradlew assembleDebug                    # Must pass
./gradlew testDebugUnitTest                # Must pass, >80%
./gradlew connectedDebugAndroidTest        # Critical flows

# Backend validation
cd maidfinder-api && npm test              # Must pass
npm run test:integration                   # DB + API tests

# Performance validation
curl -w "@curl-format.txt" api.maidfinder.in/health
adb shell am start -W com.maidfinder.app/.MainActivity

# Security validation
npm audit                                  # 0 critical
trivy image maidfinder-api:latest          # 0 critical
```

---

## Appendix A: API Contract Sketch

```typescript
// POST /api/v1/auth/send-otp
interface SendOtpRequest {
  phone: string;       // "+919876543210"
  channel: "sms" | "voice";
}
interface SendOtpResponse {
  success: boolean;
  expiresInSeconds: number;
}

// GET /api/v1/maids/search?lat=17.38&lng=78.48&radius=5
interface MaidSearchResponse {
  data: Array<{
    id: string;
    displayName: string;
    photoUrl: string | null;
    skills: string[];
    hourlyRate: number;
    languages: string[];
    ratingAvg: number;
    ratingCount: number;
    distanceKm: number;
    isVerified: boolean;
  }>;
  pagination: { page: number; total: number; hasMore: boolean };
}

// POST /api/v1/bookings
interface CreateBookingRequest {
  maidId: string;
  jobId?: string;
  dateStart: string;    // ISO date
  dateEnd?: string;
  agreedRate: number;
}
interface CreateBookingResponse {
  data: {
    id: string;
    status: "PENDING";
    paymentUrl: string;  // Razorpay checkout URL
  };
}
```

## Appendix B: Module Dependency Graph

```
app
├── feature-auth ───────► core-domain, core-data, core-ui
├── feature-jobs ───────► core-domain, core-data, core-ui, feature-booking
├── feature-booking ────► core-domain, core-data, core-ui
├── feature-chat ───────► core-domain, core-data, core-ui
├── feature-profile ────► core-domain, core-data, core-ui
├── feature-search ─────► core-domain, core-data, core-ui
│
├── core-ui ────────────► core-common
├── core-data ──────────► core-domain, core-common
├── core-domain ────────► core-common
└── core-common ────────► (none)
```

---

**Document generated:** 2026-03-31
**Review cycle:** Monthly or on major architectural change
**Next review:** 2026-04-30
