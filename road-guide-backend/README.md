# Road Guide Backend (Main API)

This service implements:

- JWT-based authentication (`visitor`, `business`, `admin`)
- Business claim flow for POIs
- Admin approval workflow to promote users and assign POIs
- Ownership-based authorization for all business POI management APIs
- Business detail editing and media uploads (photo + panorama)

## Run

```bash
go run ./cmd/api
```

Default server: `http://localhost:8090`

Default PostgreSQL connection:

- `postgres://postgres:postgres@localhost:5432/road_guide?sslmode=disable`

## Seeded Admin Account

- Identifier: `admin`
- Password: `admin1234`

Override via environment variables:

- `ADMIN_SEED_IDENTIFIER`
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
