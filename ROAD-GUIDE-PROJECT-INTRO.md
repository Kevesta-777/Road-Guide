# ROAD Guide — Project Introduction

**Document location:** repository root (outside app subprojects)  
**Repository:** `e:\Road-Guide`  
**Last updated:** June 2026  

This document introduces the **ROAD Guide** monorepo for readers who do not need to open individual subprojects first. It explains what each part does, how the pieces work together, and the main functional roles of the system.

---

## 1. What is ROAD Guide?

ROAD Guide is a **map-first mobility and exploration platform** centered on an Android application. It combines:

- **Apple Maps–style navigation** (search, directions, turn-by-turn, 3D map)
- **Offline-capable mapping** (bundled vector tiles + optional imported routing graphs)
- **Social and business features** (friends, business POI claims, ride-sharing)
- **Gold Hunt** — an offline exploration game on the map (treasures, secret places, credits, hoards)

Supporting services include a **Go REST API** (PostgreSQL) and a **React admin panel** for operators.

External infrastructure (not shipped in this repo) typically includes **Headway**: Martin tileserver, Pelias geocoding, and Valhalla routing via Docker.

---

## 2. Repository layout and roles

| Folder / asset | Role |
|----------------|------|
| **`road-guide-kotlin/`** | Primary **Android app** (`com.example.roadguideapp`). Jetpack Compose UI, MapLibre map, Gold Hunt, offline routing, auth, companion finder. |
| **`road-guide-backend/`** | **Go API server** (Gin, PostgreSQL, JWT). Users, business POIs, claims, friends, companion rides, subscriptions, admin APIs. |
| **`road-guide-admin/`** | **Operator web UI** (React, TypeScript, Vite, Tailwind). Dashboard for users, POIs, claims, panoramas, companion, subscriptions. |
| **`GreaterLondon.pmtiles`** | Low-zoom **bundled map tiles** copied into the Android APK at build time. |
| **`graph data/`** | GraphHopper 7 libraries (`_gh7core/`, `_gh7api/`, `_gh7src/`), zips, and recovery/restore scripts for offline routing work. |
| **`road-guide-kotlin/scripts/`** | Map asset tooling (Mapnik/Node); not part of the runtime app. |

**Detailed references inside the repo:**

| Topic | Path |
|-------|------|
| Android modules & APIs | `road-guide-kotlin/PROJECT_FUNCTIONS_AND_ROLES.md` |
| Android build / Headway | `road-guide-kotlin/README.md` |
| Backend API | `road-guide-backend/README.md` |

---

## 3. System architecture (high level)

```
┌─────────────────────────────────────────────────────────────────┐
│                         Clients                                  │
│  Android app (road-guide-kotlin)    Admin panel (road-guide-admin)│
└───────────────┬─────────────────────────────┬───────────────────┘
                │                             │
                ▼                             ▼
┌───────────────────────────┐     ┌───────────────────────────────┐
│  road-guide-backend (Go)  │     │  Headway stack (external Docker) │
│  PostgreSQL, JWT, REST    │     │  Martin :8000, nginx :8080,     │
└───────────────────────────┘     │  Valhalla, Pelias               │
                                    └───────────────────────────────┘
                ▲                             ▲
                │                             │
┌───────────────┴─────────────────────────────┴───────────────────┐
│              On-device (Android, offline-capable)                 │
│  PMTiles overview │ GraphHopper graph │ Room DB (Gold Hunt)       │
└───────────────────────────────────────────────────────────────────┘
```

| Integration | Default URL (emulator → host) | Purpose |
|-------------|-------------------------------|---------|
| Tileserver (Martin) | `http://10.0.2.2:8000` | Vector map tiles, styles, sprites (zoom 11+) |
| Valhalla | `http://10.0.2.2:8080/valhalla` | Online turn-by-turn routing |
| Main backend | `http://10.0.2.2:8090` | Auth, business, friends, companion, subscriptions |
| Pelias | Via Headway frontend | Search autocomplete and geocoding |

---

## 4. Android application — functions and roles

**Entry point:** `MainActivity` → full-screen `MapLibreMbTilesMap` (single-activity, edge-to-edge).

### 4.1 Map and display

| Function | Description |
|----------|-------------|
| **Vector map** | MapLibre Native; Standard / Hybrid / Satellite styles from tileserver. |
| **Dual-tier tiles** | Zoom 0–10: bundled `GreaterLondon.pmtiles`. Zoom 11+: live tileserver. |
| **Offline fallback** | Full map from PMTiles when tileserver is unavailable. |
| **3D buildings** | Fill-extrusion layer; camera tilt ~58°; emulator-safe guards. |
| **Day / night** | User-toggleable appearance; synced map chrome and Gold Hunt UI. |
| **Map chrome** | Compass, scale bar, zoom controls, layers/settings, Look Around entry. |
| **Fog of discovery** | Gold Hunt fog overlay on unexplored grid cells. |

**Main package:** `com.example.roadguideapp.map` (~115 files).

### 4.2 Search and places

| Function | Description |
|----------|-------------|
| **Pelias search** | Autocomplete, full search, reverse geocode. |
| **Nearby browse** | Category shortcuts (food, fuel, parking, etc.) with map markers. |
| **Place detail sheet** | Hours, address enrichment, claim eligibility, directions entry. |
| **Map tap → POI** | Vector-tile POI pick; enlarged icon and label. |
| **Look Around** | 360° street panoramas when approved in backend. |

### 4.3 Directions and navigation

| Function | Description |
|----------|-------------|
| **Multi-stop routing** | Origin + waypoints; stop order optimization. |
| **Routing priority** | 1) Offline GraphHopper graph → 2) Valhalla HTTP → 3) straight-line preview. |
| **Travel modes** | Drive, walk, bicycle. |
| **Turn-by-turn** | Route overlay, navigation camera, vehicle marker, instructions. |
| **Offline graph import** | User imports routing graph (ZIP or folder) via file picker. |
| **Gold Hunt routing gate** | Entering Gold Hunt requires offline graph or reachable Valhalla. |

**Main packages:** `map` (directions), `offlinegraph`.

### 4.4 Authentication and social

| Function | Description |
|----------|-------------|
| **Offline-first auth** | Local session; works without network. |
| **Backend sync** | Register/login → JWT; profile refresh when online. |
| **Friends** | Add/remove friends; QR code scan (CameraX + ML Kit + ZXing). |

**Main package:** `auth`.

### 4.5 Business POI claims

| Function | Description |
|----------|-------------|
| **Claim eligibility** | Checks whether a map place can be claimed. |
| **Registration flow** | Submits claim to backend; admin guidance messages. |
| **Business edit** | Owners edit POI details and media. |

### 4.6 Companion Finder (Premium)

| Function | Description |
|----------|-------------|
| **Offer ride** | Drivers post rides (Premium subscription required). |
| **Browse / book** | Passengers find and book seats. |

### 4.7 Panorama viewer

| Function | Description |
|----------|-------------|
| **OpenGL 360° viewer** | `PanoramaViewerActivity` for street-view spheres. |

**Main package:** `panorama`.

---

## 5. Gold Hunt — game mode (Android, offline)

Gold Hunt is a **self-contained exploration game** on the map. Progress is stored locally in **Room SQLite** (`goldhunt.db`). No server is required for gameplay.

**Main package:** `com.example.roadguideapp.goldhunt` (~97 files).

### 5.1 How to enter

- Tap the **Gold Hunt** diamond button (bottom-left on the map).
- Requires **location permission** and **routing availability** (offline graph or Valhalla).
- Optional **intro workflow alert** on first play.

### 5.2 Core mechanics

| Mechanic | Description |
|----------|-------------|
| **Play region** | Default Greater London bounds; procedural content on a grid. |
| **Exploration grid** | GPS (or navigation) marks explored cells; fog hides unexplored areas. |
| **Treasures** | Procedural markers; visible and collectible at **zoom ≥ 17**. |
| **Collection** | Tap treasure or walk/drive within radius (100 m walk / 200 m drive). |
| **Credits** | Per-type rewards; profile and score bar update. |
| **Secret places** | Catalog JSON + procedural spawns; unlock after **10 gifts** collected. |
| **Treasure hoard** | ~**50 treasures** after secret discovery; first two at secret site, later in random London sub-regions. |
| **Search disabled** | Map search is off while Gold Hunt is active. |

### 5.3 Treasure types and unlock progression

| Type | Unlock condition |
|------|------------------|
| **Star** | Always available |
| **Flower** | 10 stars collected |
| **Crystal** | 10 flowers collected |
| **Gift** | 10 crystals collected |
| **Secret places** | 10 gifts collected |

### 5.4 Generation ratios

| Spawn type | Ratio |
|------------|-------|
| Gold star | 43% |
| Flower | 27% |
| Crystal | 17% |
| Gift | 9% |
| Secret place | 4% |

Weights are normalized among **unlocked** types only. Procedural treasure slot spawn chance: **20%** per L1 cell slot.

### 5.5 Visual effects

| Effect | When |
|--------|------|
| **Mode entry** | Zoom + treasure pop-in on entering Gold Hunt. |
| **Zoom 17 flash** | Crossing treasure zoom threshold; day = blue, night = gold; upward surge. |
| **Treasure appear** | Pop-in, fade, rise when zooming in past 17. |
| **Treasure disappear** | Shrink, fade, float up when zooming out below 17. |
| **Collect sparkle** | Soft tap animation with **56 tiny flying stars** and type-colored particles. |
| **Workflow alerts** | Intro, first star/flower/crystal/gift/secret, hoard announcement. |

### 5.6 Key Gold Hunt modules

| Submodule | Role |
|-----------|------|
| `goldhunt.treasure` | Generation, hoard, collection, types |
| `goldhunt.secretplace` | Catalog, procedural secrets, reveal policy |
| `goldhunt.engine` | Grid, play region, GPS sampling |
| `goldhunt.overlays` | MapLibre treasure and secret symbol layers |
| `goldhunt.database` | Room entities and DAOs |
| `goldhunt.ui` | HUD, score bar, sparkle and flash effects |
| `goldhunt.rewards` | Credit dispatchers |

---

## 6. Backend API — functions and roles

**Stack:** Go, Gin, PostgreSQL, JWT. **Default:** `:8090`.

### 6.1 API domains (`/api/v1`)

| Domain | Functions |
|--------|-----------|
| **Auth** | Register, login, current user |
| **Places** | Panoramas, place detail (public) |
| **Business** | Claims, POI CRUD, media upload |
| **Friends** | List, add, remove |
| **Companion** | Driver posts, booking, passenger requests |
| **Subscriptions** | User status (Premium gating) |
| **Admin** | Users, roles, POIs, panoramas, companion, plans |

### 6.2 Backend package roles

| Package | Role |
|---------|------|
| `config` | Environment variables |
| `models` | Domain types and roles |
| `middleware` | CORS, JWT, admin guard |
| `routers` / `controllers` | HTTP layer |
| `services` | Business logic, SQL, migrations |

---

## 7. Admin panel — functions and roles

**Stack:** React 19, TypeScript, Vite, Tailwind, MapLibre GL.

| Area | Role |
|------|------|
| **Dashboard** | Registration requests, users |
| **Business accounts** | Business users and POIs |
| **User management** | Roles and accounts |
| **POI management** | Map-linked business POIs |
| **360° images** | Panorama approval |
| **Companion Finder** | Rides and bookings |
| **Premium & subscriptions** | Plans and user subscriptions |

Some sidebar items (analytics, ads, OTA) are UI placeholders and may not be fully backend-connected.

---

## 8. Offline and data components

| Component | Role |
|-----------|------|
| **GreaterLondon.pmtiles** | Bundled overview tiles (z0–10) |
| **GraphHopper 7** | On-device offline routing graph |
| **Valhalla** | Online routing via Headway |
| **Pelias** | Online search/geocoding |
| **Room (`goldhunt.db`)** | Gold Hunt progress |
| **SharedPreferences** | Auth, friends, map style, Gold Hunt prefs |

---

## 9. User roles (product level)

| Role | Capabilities |
|------|--------------|
| **App user** | Map, search, directions, Gold Hunt, friends, claims |
| **Business user** | Manage claimed POIs (after approval) |
| **Premium subscriber** | Companion Finder rides |
| **Admin** | Web console for approvals and content |

---

## 10. Configuration quick reference

### Android (`road-guide-kotlin`)

| Setting | Purpose |
|---------|---------|
| `HEADWAY_TILESERVER_BASE_URL` | Martin tileserver |
| `HEADWAY_VALHALLA_BASE_URL` | Valhalla routing |
| `MAIN_BACKEND_BASE_URL` | Go API |

### Backend (`road-guide-backend/.env`)

| Setting | Purpose |
|---------|---------|
| `DATABASE_URL` | PostgreSQL |
| `JWT_SECRET` | Auth tokens |
| `APP_ADDR` | Listen address |

### Admin (`road-guide-admin`)

| Setting | Purpose |
|---------|---------|
| `VITE_API_BASE_URL` | Backend for dev proxy |

---

## 11. Typical development run order

1. PostgreSQL + **road-guide-backend**
2. **Headway Docker** (tileserver, Valhalla, Pelias)
3. Optional: **road-guide-admin**
4. **road-guide-kotlin** on emulator/device (`adb reverse` for ports if needed)

---

## 12. Summary

ROAD Guide is a **three-part product**: an offline-capable **Android map app** with **Gold Hunt**, a **Go backend** for accounts and business features, and a **React admin** console. Map detail and online routing use external **Headway** services; overview tiles and Gold Hunt work offline on device.

For file-level Android documentation, see `road-guide-kotlin/PROJECT_FUNCTIONS_AND_ROLES.md`. This file is the **monorepo-level introduction** placed at the repository root, outside the individual app subprojects.
