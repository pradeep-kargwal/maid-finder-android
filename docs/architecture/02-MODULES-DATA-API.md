# MaidFinder Architecture — Part 2: Modules, Data, APIs

---

## 3. Module & Service Decomposition

### 3.1 Android Client Modules

```
maidfinder-android/
├── app/                          # Application entry point, DI config
├── core/
│   ├── core-ui/                  # Theme, components, animations
│   ├── core-data/                # Network client, Room DB, cache
│   ├── core-domain/              # Models, use cases, repository interfaces
│   └── core-common/              # Extensions, utils, Result type
├── feature/
│   ├── feature-auth/             # Login, OTP, session management
│   ├── feature-jobs/             # Browse, post, apply to jobs
│   ├── feature-booking/          # Booking flow, booking list
│   ├── feature-chat/             # Conversations, messages, voice
│   ├── feature-profile/          # User profile, verification, settings
│   └── feature-search/           # Map view, filters, proximity
└── build-logic/                  # Convention plugins
```

**Module Responsibilities:**

| Module | Exports | Depends On | Responsibility |
|--------|---------|------------|----------------|
| `app` | Application class | All features | DI wiring, navigation root |
| `core-common` | `Result<T>`, extensions | None | Shared utilities |
| `core-domain` | Models, `UseCase<R,P>`, `Repository` interfaces | `core-common` | Business logic contracts |
| `core-data` | Repository impls, Room DAOs, Retrofit services | `core-domain` | Data access, caching |
| `core-ui` | Theme, `MaidFinderButton`, animations | `core-common` | Shared UI components |
| `feature-auth` | LoginScreen, AuthViewModel, AuthRepository | `core-*` | Authentication flow |
| `feature-jobs` | JobListScreen, JobDetailScreen, PostJobScreen | `core-*` | Job marketplace |
| `feature-booking` | BookingScreen, BookingsListScreen | `core-*` | Booking lifecycle |
| `feature-chat` | ChatScreen, MessagesScreen | `core-*` | Real-time messaging |
| `feature-profile` | ProfileScreen, VerificationScreen | `core-*` | User management |
| `feature-search` | MapScreen, FilterScreen | `core-*` | Discovery |

### 3.2 Backend Service Modules (Monolith)

```
maidfinder-api/
├── src/
│   ├── modules/
│   │   ├── auth/                 # OTP, JWT, Firebase integration
│   │   │   ├── auth.controller.ts
│   │   │   ├── auth.service.ts
│   │   │   ├── auth.routes.ts
│   │   │   └── auth.schema.ts    # Zod validation
│   │   ├── users/                # Profile CRUD, verification
│   │   ├── jobs/                 # Job CRUD, search, matching
│   │   ├── bookings/             # Booking lifecycle, escrow
│   │   ├── messages/             # WebSocket handlers, message persistence
│   │   ├── payments/             # Razorpay integration, receipts
│   │   ├── notifications/        # FCM push, in-app notifications
│   │   └── search/               # PostGIS queries, geospatial
│   ├── shared/
│   │   ├── middleware/           # Auth, rate-limit, error handler
│   │   ├── database/            # Knex migrations, seeds
│   │   ├── types/               # Shared TypeScript types
│   │   └── utils/               # Helpers, constants
│   └── app.ts                   # Fastify instance, plugin registration
├── tests/
│   ├── unit/
│   ├── integration/
│   └── e2e/
├── knexfile.ts
└── package.json
```

### 3.3 Interface Contracts (Key Boundaries)

**Repository Interface (Client → Data Layer):**

```kotlin
// core-domain: Repository interface
interface JobRepository {
    fun getJobs(filter: JobFilter): Flow<Resource<List<Job>>>
    fun getJobById(id: String): Flow<Resource<Job>>
    suspend fun createJob(request: CreateJobRequest): Result<Job>
    suspend fun applyToJob(jobId: String, message: String?): Result<Application>
}

// Resource wrapper for offline-first
sealed class Resource<T> {
    data class Success<T>(val data: T, val source: DataSource) : Resource<T>()
    data class Error<T>(val message: String, val code: Int? = null) : Resource<T>()
    class Loading<T> : Resource<T>()
}

enum class DataSource { LOCAL, REMOTE }
```

**API Service Interface (Client → Network):**

```kotlin
// core-data: Retrofit service
interface JobApiService {
    @GET("api/v1/jobs")
    suspend fun getJobs(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radiusKm: Int,
        @Query("type") type: String?,
        @Query("page") page: Int
    ): ApiResponse<PaginatedResponse<JobDto>>

    @POST("api/v1/jobs")
    suspend fun createJob(@Body request: CreateJobRequest): ApiResponse<JobDto>

    @POST("api/v1/jobs/{id}/apply")
    suspend fun applyToJob(
        @Path("id") jobId: String,
        @Body request: ApplyRequest
    ): ApiResponse<ApplicationDto>
}
```

**Backend Controller Interface:**

```typescript
// Fastify route with Zod validation
const createJobSchema = z.object({
  jobType: z.enum(['PART_TIME', 'FULL_TIME', 'ONE_TIME']),
  title: z.string().min(3).max(200),
  location: z.object({
    lat: z.number().min(-90).max(90),
    lng: z.number().min(-180).max(180),
    address: z.string().max(500)
  }),
  dateStart: z.string().datetime(),
  budgetMin: z.number().positive(),
  budgetMax: z.number().positive(),
  shifts: z.array(z.object({
    type: z.enum(['MORNING', 'AFTERNOON', 'EVENING']),
    start: z.string().regex(/^\d{2}:\d{2}$/),
    end: z.string().regex(/^\d{2}:\d{2}$/)
  })).min(1)
});

fastify.post('/api/v1/jobs', {
  preHandler: [authenticate, validateBody(createJobSchema)],
  handler: jobController.createJob
});
```

---

## 4. Data Design

### 4.1 Persistence Strategy

| Layer | Technology | Purpose | TTL |
|-------|-----------|---------|-----|
| **Network** | Retrofit + OkHttp | API calls | Request-scoped |
| **Memory Cache** | In-memory `Map` | ViewModel state | Process-scoped |
| **Disk Cache** | Room (SQLite) | Offline access, recent data | 7 days (configurable) |
| **Shared Prefs** | DataStore | Auth tokens, settings | Until logout |
| **Backend DB** | PostgreSQL + PostGIS | Source of truth | Permanent |
| **Backend Cache** | Redis | Session, rate-limit, hot data | 15 min – 24h |

### 4.2 Room Database Schema (Client)

```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val phone: String,
    val role: String,           // CLIENT or MAID
    val displayName: String,
    val photoUrl: String?,
    val createdAt: Long,
    val lastSyncedAt: Long
)

@Entity(
    tableName = "jobs",
    indices = [Index("status"), Index("clientId"), Index("createdAt")]
)
data class JobEntity(
    @PrimaryKey val id: String,
    val clientId: String,
    val clientName: String,
    val jobType: String,
    val title: String,
    val description: String,
    val locationLat: Double,
    val locationLng: Double,
    val locationAddress: String,
    val dateStart: String,
    val dateEnd: String?,
    val budgetMin: Double,
    val budgetMax: Double,
    val budgetType: String,
    val status: String,
    val applicantCount: Int,
    val createdAt: Long,
    val lastSyncedAt: Long
)

@Entity(
    tableName = "bookings",
    foreignKeys = [ForeignKey(
        entity = JobEntity::class,
        parentColumns = ["id"],
        childColumns = ["jobId"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class BookingEntity(
    @PrimaryKey val id: String,
    val clientId: String,
    val maidId: String,
    val jobId: String?,
    val status: String,
    val dateStart: String,
    val dateEnd: String?,
    val agreedRate: Double,
    val totalAmount: Double,
    val createdAt: Long,
    val lastSyncedAt: Long
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val receiverId: String,
    val type: String,
    val content: String,
    val isRead: Boolean,
    val isSent: Boolean,       // false = queued for send
    val createdAt: Long
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,    // "message", "application", "booking"
    val entityId: String,
    val action: String,        // "create", "update", "delete"
    val payload: String,       // JSON
    val createdAt: Long,
    val retryCount: Int = 0
)
```

### 4.3 PostgreSQL Schema (Backend)

```sql
-- Users
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone VARCHAR(15) UNIQUE NOT NULL,
    phone_verified BOOLEAN DEFAULT FALSE,
    role VARCHAR(10) NOT NULL CHECK (role IN ('CLIENT', 'MAID')),
    display_name VARCHAR(100) NOT NULL,
    photo_url TEXT,
    status VARCHAR(15) DEFAULT 'ACTIVE',
    fcm_token TEXT,
    language VARCHAR(10) DEFAULT 'en',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Profiles (extended for maids)
CREATE TABLE profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    skills TEXT[] DEFAULT '{}',
    experience_years INT DEFAULT 0,
    hourly_rate DECIMAL(10,2),
    languages TEXT[] DEFAULT '{}',
    work_type VARCHAR(10),
    availability JSONB,
    rating_avg DECIMAL(3,2) DEFAULT 0,
    rating_count INT DEFAULT 0,
    verified_photo BOOLEAN DEFAULT FALSE,
    verified_id BOOLEAN DEFAULT FALSE,
    bio TEXT
);

-- Locations (with PostGIS)
CREATE TABLE locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lat DECIMAL(10,8) NOT NULL,
    lng DECIMAL(11,8) NOT NULL,
    address TEXT,
    city VARCHAR(100),
    geog GEOGRAPHY(POINT, 4326) GENERATED ALWAYS AS (ST_MakePoint(lng, lat)) STORED,
    is_primary BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_locations_geog ON locations USING GIST(geog);

-- Jobs
CREATE TABLE jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL REFERENCES users(id),
    job_type VARCHAR(15) NOT NULL,
    title VARCHAR(200),
    description TEXT,
    location_lat DECIMAL(10,8),
    location_lng DECIMAL(11,8),
    location_address TEXT,
    date_start DATE NOT NULL,
    date_end DATE,
    shifts JSONB,
    budget_min DECIMAL(10,2),
    budget_max DECIMAL(10,2),
    budget_type VARCHAR(10),
    status VARCHAR(15) DEFAULT 'ACTIVE',
    applicant_count INT DEFAULT 0,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_created ON jobs(created_at DESC);

-- Bookings
CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL REFERENCES users(id),
    maid_id UUID NOT NULL REFERENCES users(id),
    job_id UUID REFERENCES jobs(id),
    status VARCHAR(15) DEFAULT 'PENDING',
    date_start DATE NOT NULL,
    date_end DATE,
    agreed_rate DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Messages
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id VARCHAR(100) NOT NULL,
    sender_id UUID NOT NULL REFERENCES users(id),
    receiver_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(15) DEFAULT 'TEXT',
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_messages_conv ON messages(conversation_id, created_at DESC);
```

### 4.4 Caching Strategy

| Data Type | Client Cache (Room) | Backend Cache (Redis) | Invalidation |
|-----------|--------------------|-----------------------|-------------|
| User Profile | 24h TTL | 1h TTL | On update |
| Job Listings | 15 min | 5 min | On create/update |
| Maid Search | 10 min | 10 min | On profile change |
| Bookings | Until status change | 30 min | On status change |
| Messages | Permanent (local) | Not cached | Append-only |
| Auth Token | DataStore | Session object | On logout |

### 4.5 Offline-First Sync Strategy

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Online    │         │  Offline    │         │  Reconnect  │
│             │         │             │         │             │
│ API → Room  │   ──►   │ Read Room   │   ──►   │ Push Queue  │
│ Room → UI   │         │ Write Queue │         │ Pull Latest │
│             │         │             │         │ Resolve     │
└─────────────┘         └─────────────┘         └─────────────┘

Conflict Resolution:
- Messages: Append-only (no conflict)
- Bookings: Server-authoritative (local is read cache)
- Profile: Last-write-wins (server timestamp)
- Jobs: Server-authoritative
```

---
