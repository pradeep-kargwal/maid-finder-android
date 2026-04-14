# MaidFinder — Enhanced Architecture Proposal

**Version:** 1.0
**Date:** 2026-03-31
**Author:** Senior Software Architect
**Status:** Proposed
**Audience:** Engineering Team, Product, Stakeholders

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [High-Level Target Architecture](#2-high-level-target-architecture)
3. [Module & Service Decomposition](#3-module--service-decomposition)
4. [Data Design](#4-data-design)
5. [API & Integration Design](#5-api--integration-design)
6. [Quality Attributes & SLOs](#6-quality-attributes--slos)
7. [Security & Compliance](#7-security--compliance)
8. [Observability Plan](#8-observability-plan)
9. [Deployment & Operations](#9-deployment--operations)
10. [Migration Strategy](#10-migration-strategy)
11. [Testing Strategy](#11-testing-strategy)
12. [Implementation Plan](#12-implementation-plan)
13. [Quick Wins & Roadmap](#13-quick-wins--roadmap)
14. [Decision Log](#14-decision-log)
15. [Acceptance Criteria & Success Metrics](#15-acceptance-criteria--success-metrics)

---

## 1. Executive Summary

### 1.1 Problem Context

MaidFinder is a two-sided marketplace connecting Clients seeking domestic help with Maids seeking employment. The current implementation is a **45-file single-module Android app** with:

- **No backend** — all data is hardcoded in-memory
- **No persistence** — data lost on process death
- **No offline support** — app requires network but has no cache
- **No tests** — zero automated verification
- **No security** — simulated auth, no encryption

The app is a functional UI prototype but cannot serve real users.

### 1.2 Recommended Architectural Pattern

**Clean Architecture + MVVM on Android with a Modular Backend (Node.js + PostgreSQL)**

```
┌─────────────────────────────────────────────────────────────┐
│                    ARCHITECTURE PATTERN                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Android Client              Backend Services               │
│  ┌─────────────────┐        ┌─────────────────┐            │
│  │  Presentation   │        │   API Gateway   │            │
│  │  (Compose + VM) │◄──────►│   (Express)     │            │
│  ├─────────────────┤  HTTP  ├─────────────────┤            │
│  │  Domain         │  WS    │   Services      │            │
│  │  (Use Cases)    │◄──────►│   (Auth, Job,   │            │
│  ├─────────────────┤        │    Booking,     │            │
│  │  Data           │        │    Message)     │            │
│  │  (Repos + Cache)│        ├─────────────────┤            │
│  └─────────────────┘        │   Data Layer    │            │
│                             │   (PostgreSQL   │            │
│                             │    + Redis)     │            │
│                             └─────────────────┘            │
│                                                             │
│  Pattern: Repository → UseCase → ViewModel → Compose       │
│  Backend: Controller → Service → Repository → Database     │
│  Sync: REST + WebSocket (real-time) + Offline Queue         │
└─────────────────────────────────────────────────────────────┘
```

### 1.3 Rationale

| Decision | Rationale |
|----------|-----------|
| Clean Architecture | Enforces separation; domain logic independent of framework; testable in isolation |
| MVVM (not MVI) | Familiar to team; Compose state management maps naturally to StateFlow; lower boilerplate |
| Node.js backend | JavaScript/TypeScript ecosystem; fast iteration; team can share types via Kotlin Multiplatform later |
| PostgreSQL | ACID compliance for bookings/payments; PostGIS for geospatial queries; mature ecosystem |
| Repository pattern (client) | Swappable data sources (in-memory → Room → network); enables offline-first |
| WebSocket for real-time | Chat and booking status updates require push; Socket.IO is battle-tested |
| Modular single-app (not microservices) | 3-person team; 100k users doesn't justify microservice overhead; single deploy unit reduces ops burden |

### 1.4 Key Trade-offs

| Trade-off | Chosen | Alternative | Why |
|-----------|--------|-------------|-----|
| Monolith vs Microservices | Monolith | Microservices | Team size (3); 100k users; operational simplicity |
| Room vs SQLDelight | Room | SQLDelight | Larger community; Google-maintained; better Compose integration |
| Retrofit vs Ktor Client | Retrofit | Ktor Client | Industry standard; interceptor ecosystem; team familiarity |
| Express vs Fastify | Fastify | Express | 2x performance; schema validation; TypeScript-first |
| Manual DI vs Hilt | Hilt | Manual ServiceLocator | Compile-time safety; Android lifecycle awareness; scalability |

---

## 2. High-Level Target Architecture

### 2.1 Architecture Overview Diagram

```
┌──────────────────────────────────────────────────────────────────────────┐
│                           CLIENT LAYER                                   │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐  │
│  │                    Android App (Kotlin + Compose)                  │  │
│  │                                                                    │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │  │
│  │  │  Feature  │  │  Feature  │  │  Feature  │  │  Feature  │         │  │
│  │  │  :auth    │  │  :jobs    │  │  :booking │  │  :chat    │         │  │
│  │  └─────┬────┘  └─────┬────┘  └─────┬────┘  └─────┬────┘         │  │
│  │        │              │              │              │              │  │
│  │  ┌─────┴──────────────┴──────────────┴──────────────┴──────────┐  │  │
│  │  │                    :core (shared)                           │  │  │
│  │  │  ┌─────────┐  ┌──────────┐  ┌──────────┐  ┌────────────┐  │  │  │
│  │  │  │  domain  │  │   data   │  │   ui     │  │ navigation │  │  │  │
│  │  │  │ (models, │  │ (repos,  │  │ (theme,  │  │            │  │  │  │
│  │  │  │ usecases)│  │  cache)  │  │ compose) │  │            │  │  │  │
│  │  │  └─────────┘  └──────────┘  └──────────┘  └────────────┘  │  │  │
│  │  └─────────────────────────────────────────────────────────────┘  │  │
│  │                                                                    │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │  │
│  │  │   Room   │  │ Retrofit │  │  Socket  │  │  Hilt    │         │  │
│  │  │    DB    │  │  + OkHttp│  │   .IO    │  │   DI     │         │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │  │
│  └────────────────────────────────────────────────────────────────────┘  │
│                                                                          │
└──────────────────────────────┬───────────────────────────────────────────┘
                               │
                    HTTPS + WSS (TLS 1.3)
                               │
┌──────────────────────────────┴───────────────────────────────────────────┐
│                           BACKEND LAYER                                   │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐  │
│  │              Node.js Monolith (Fastify + TypeScript)              │  │
│  │                                                                    │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │  │
│  │  │   Auth   │  │   Job    │  │ Booking  │  │  Chat    │         │  │
│  │  │  Module  │  │  Module  │  │  Module  │  │  Module  │         │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │  │
│  │                                                                    │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │  │
│  │  │ Payment  │  │   User   │  │  Search  │  │  Notif   │         │  │
│  │  │  Module  │  │  Module  │  │  Module  │  │  Module  │         │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │  │
│  │                                                                    │  │
│  │  ┌──────────────────────────────────────────────────────────────┐ │  │
│  │  │               Shared: middleware, utils, types               │ │  │
│  │  └──────────────────────────────────────────────────────────────┘ │  │
│  └────────────────────────────────────────────────────────────────────┘  │
│                                                                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐               │
│  │PostgreSQL│  │  Redis   │  │    S3    │  │  Firebase │               │
│  │ + PostGIS│  │ (cache + │  │ (media)  │  │(Auth+FCM) │               │
│  │          │  │  pubsub) │  │          │  │           │               │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘               │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Deployment Topology

```
┌─────────────────────────────────────────────────────────────────┐
│                        AWS ap-south-1 (Mumbai)                  │
│                                                                 │
│  ┌───────────────┐    ┌──────────────────────────────────────┐  │
│  │   CloudFront  │    │          ECS Fargate Cluster         │  │
│  │   (CDN + SSL) │    │                                      │  │
│  └───────┬───────┘    │  ┌────────────┐  ┌────────────┐     │  │
│          │            │  │  API Task  │  │  API Task  │     │  │
│          │            │  │  (256 CPU, │  │  (256 CPU, │     │  │
│  ┌───────┴───────┐    │  │  512 MB)   │  │  512 MB)   │     │  │
│  │     ALB       │───►│  └────────────┘  └────────────┘     │  │
│  │ (Load Balance)│    │                                      │  │
│  └───────────────┘    │  ┌────────────┐                      │  │
│                       │  │  Worker    │ (background jobs)    │  │
│  ┌───────────────┐    │  └────────────┘                      │  │
│  │  RDS Aurora   │◄───┴──────────────────────────────────────┘  │
│  │  PostgreSQL   │                                               │
│  │  (db.t4g.micro│    ┌──────────────┐  ┌──────────────┐       │
│  │   .multi-AZ)  │    │ ElastiCache  │  │     S3       │       │
│  └───────────────┘    │ Redis 7      │  │   (media)    │       │
│                       │ (cache.t3    │  └──────────────┘       │
│                       │  .micro)     │                          │
│                       └──────────────┘                          │
│                                                                 │
│  ┌───────────────┐    ┌──────────────┐                          │
│  │   Firebase    │    │  CloudWatch  │                          │
│  │ Auth + FCM    │    │  (logs +     │                          │
│  └───────────────┘    │   metrics)   │                          │
│                       └──────────────┘                          │
└─────────────────────────────────────────────────────────────────┘

Cost Estimate (10k users): ~$120-180/month
Cost Estimate (100k users): ~$400-600/month
```

### 2.3 Data Flow: Client → Backend

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  Compose │───►│ ViewModel│───►│ Use Case │───►│Repository│
│  Screen  │    │          │    │          │    │          │
└──────────┘    └──────────┘    └──────────┘    └─────┬────┘
                                                      │
                              ┌────────────────────────┤
                              │                        │
                         ┌────┴────┐             ┌─────┴─────┐
                         │  Room   │             │  Retrofit │
                         │  Cache  │             │  Client   │
                         └─────────┘             └─────┬─────┘
                                                       │
                                                  HTTPS/TLS
                                                       │
                                                 ┌─────┴─────┐
                                                 │   Fastify  │
                                                 │   Server   │
                                                 └─────┬─────┘
                                                       │
                                                 ┌─────┴─────┐
                                                 │ PostgreSQL │
                                                 └───────────┘
```

---
