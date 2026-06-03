# ROAD Guide Backend

Go API server for ROAD Guide (map app, admin panel, Companion Finder).

## Run

```bash
cd road-guide-backend
go run .
```

Requires PostgreSQL (`DATABASE_URL`) and `JWT_SECRET` in `.env`.

## Project structure

```
road-guide-backend/
├── main.go              # Bootstrap: config, DB, migrate/seed, wire router
├── config/              # Env loading (config.Load)
├── models/              # Domain types and constants (User, roles, companion statuses)
├── utils/               # JSON helpers, Postgres rebind, gin wrap, geo/POI helpers
├── middleware/          # CORS, JWT auth, admin role guard
├── routers/             # Gin route registration
├── controllers/         # HTTP layer (thin delegates to services)
└── services/            # Business logic, SQL, migrations, seeds
```

| Package | Responsibility |
|---------|----------------|
| `config` | `DATABASE_URL`, `JWT_SECRET`, `APP_ADDR`, uploads, claim guidance |
| `models` | Shared structs and role/status constants |
| `utils` | `WriteJSON`, `RebindPostgres`, `GinWrap`, `URLParam` |
| `middleware` | `RequireAuth`, `RequireRole`, CORS |
| `routers` | All `/api/v1` routes |
| `controllers` | Parse HTTP → call service handlers |
| `services` | Auth, business POI, friends, places, companion, subscription, admin |

Handlers use standard `http.ResponseWriter` / `*http.Request` and are wrapped for Gin via `utils.GinWrap`. URL params use `utils.URLParam(r, "poiID")`.

## Key APIs

- `GET /api/v1/subscriptions/status` — user subscription (required for Offer Ride)
- `POST /api/v1/companion/driver-posts` — create ride (Premium required)
- `POST /api/v1/companion/driver-posts/{id}/book` — book seat (creates passenger request for admin)
- `GET /api/v1/admin/companion/passenger-requests` — admin passenger list
- `GET /api/v1/admin/companion/bookings` — admin bookings list
- `GET /api/v1/admin/subscriptions/*` — plans, content, user subscriptions

## Admin: enable Premium for a driver

`POST /api/v1/admin/subscriptions/users` with body:

```json
{
  "userId": "<user-uuid>",
  "planId": "plan-premium",
  "expiresAt": "2027-12-31T00:00:00Z"
}
```
