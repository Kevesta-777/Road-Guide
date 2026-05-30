# Road Guide Backend (Main API)

This service implements:

- JWT-based authentication (`visitor`, `business`, `admin`)
- Business claim flow for POIs
- Admin approval workflow to promote users and assign POIs
- Ownership-based authorization for all business POI management APIs
- Business detail editing and media uploads (photo + panorama)
- Admin review queue for uploaded panoramas (`pending` → `approved` / `rejected`)

## Run

```bash
go run ./cmd/api
```

Default server: `http://localhost:8090`

Default PostgreSQL connection:

- `postgres://postgres:postgres@localhost:5432/road_guide?sslmode=disable`

## Seeded Admin Account

- Email: `admin@roadguide.local`
- Password: `admin1234`

Override via environment variables:

- `ADMIN_SEED_EMAIL`
- `ADMIN_SEED_PASSWORD`
- `ADMIN_SEED_NAME`

## Key Environment Variables

- `APP_ADDR` (default `:8090`)
- `DATABASE_URL` (default `postgres://postgres:postgres@localhost:5432/road_guide?sslmode=disable`)
- `JWT_SECRET` (default `dev-secret-change-me`)
- `UPLOAD_DIR` (default `./uploads`)
- `PUBLIC_UPLOAD_PREFIX` (default `/uploads`)
- `CLAIM_CONTACT_PHONE`
- `CLAIM_AGENT_ADDRESS`
- `CLAIM_HOURS`
- `CLAIM_INSTRUCTIONS`

## Mobile Integration Contract

The app should call:

- `GET /api/v1/business-claims/{poiID}` when tapping **Claim This Place**
  - If assigned business owner:
    - `claimButtonAction.type = "navigate_business_edit"`
    - `claimButtonAction.targetPath` and API fields are returned
  - If not assigned:
    - `claimButtonAction.type = "show_registration_info"`
    - `registrationGuidance` contains contact phone, address, hours, instructions
    - `claimButtonAction.requestClaimApi` is returned

- `GET /api/v1/business-pois/{poiID}` for edit screen bootstrap
  - returns `poi`, `media`, and `editContract` endpoint definitions

The mobile app opens an in-app **Business Detail Edit** screen when an assigned business user taps **Claim This Place**.

### Panorama admin review

- Business uploads with `kind=panorama` are stored with `status=pending`.
- Admin panel **360° Images** loads `GET /api/v1/admin/panoramas` (`{ items: [...] }`).
- Approve: `POST /api/v1/admin/panoramas/{mediaID}/approve`
- Reject: `POST /api/v1/admin/panoramas/{mediaID}/reject` with `{ "adminNote": "..." }`
- Mobile **Look Around** loads approved panoramas: `GET /api/v1/places/panoramas?externalRef=...` (no auth)
