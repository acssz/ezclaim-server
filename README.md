# Ezclaim

A Spring Boot service for managing claims with MongoDB storage, S3-compatible object storage for photos, and a clean MVC/API structure. Includes Bruno API tests and dev tooling for quick startup.

## Tech Stack
- Spring Boot 3.5.x (Java 21+/24 target)
- MongoDB (Spring Data Mongo)
- S3-compatible object storage via AWS SDK v2 (works with AWS S3, MinIO, etc.)
- Build: Maven Wrapper (`./mvnw`)
- API tests: Bruno (`.bru` files under `bruno/`)

## Quick Start (Dev)
1) Prereqs: Docker, Java 21+ (build is set to Java 24), optional direnv.
2) Start dev services (MongoDB 8 + MinIO):
   - `docker compose -f docker-compose.dev.yml up -d`
3) Load project env (dev profile):
   - Install direnv, then in repo root: `direnv allow` (loads `.envrc` => `SPRING_PROFILES_ACTIVE=dev`).
4) Run the app:
   - `./mvnw spring-boot:run`

Dev URLs
- API base: `http://localhost:8080`
- MinIO Console: `http://localhost:9001`
- S3 endpoint: `http://localhost:9000`
- Mongo: `mongodb://...` from `application-dev.yml`

## Configuration
Spring profiles are used to separate dev and prod configuration:
- `src/main/resources/application-dev.yml` (local dev)
- `src/main/resources/application-prod.yml` (deployment)

Mongo (dev)
- `spring.data.mongodb.uri=mongodb://ezclaim:E2Claim@localhost:27017/ezclaim?authSource=admin`
  - If authentication fails, clear the docker volume or align the URI with the existing root user in your volume.

Object Store (generic S3)
- Properties prefix: `app.objectstore.*`
  - `endpoint`: omit for AWS S3; set to `http://localhost:9000` for MinIO dev
  - `region`: e.g. `us-east-1`
  - `access-key`, `secret-key`: credentials
  - `bucket`: default bucket name
  - `path-style`: `true` for S3-compatible services like MinIO
  - `ensure-bucket`: set `true` in dev to auto-create; keep `false` in prod

Prod environment variables expected (see `application-prod.yml`):
- `SPRING_DATA_MONGODB_URI`
- `APP_OBJECTSTORE_ENDPOINT` (omit for AWS S3)
- `APP_OBJECTSTORE_REGION`
- `APP_OBJECTSTORE_ACCESS_KEY`
- `APP_OBJECTSTORE_SECRET_KEY`
- `APP_OBJECTSTORE_BUCKET`
- `APP_OBJECTSTORE_PATH_STYLE`
- `APP_OBJECTSTORE_ENSURE_BUCKET`

## Domain & API
Entities
- Claim: `title`, `description`, `status`, `createdAt`, `updatedAt`, references to `photos[]`, `tags[]` (stored separately)
- Photo: `bucket`, `key`, `uploadedAt` (S3 metadata)
- Tag: `label`, `color`

Key Endpoints (REST)
- Tags: `GET/POST/PUT/DELETE /api/tags[/id]`
- Photos:
  - `POST /api/photos/presign-upload` → returns presigned PUT URL
  - `POST /api/photos` → create a Photo record (after you upload)
  - `GET /api/photos/{id}/download-url` → presigned GET URL
  - `GET/DELETE /api/photos[/id]`
- Claims: `GET/POST/PUT/DELETE /api/claims[/id]` (accepts `photoIds[]` and `tagIds[]`)
 - Audit Events:
   - `GET /api/audit-events` (filters: `entityType`, `entityId`, `action`, `from`, `to`; paging `page`, `size`; sorting `sort=field,asc|desc`)
   - `GET /api/audit-events/{id}`

Auth (for Audit Events)
- `POST /api/auth/login` with JSON `{ "username": "admin", "password": "ezclaim-password" }`
- Returns `{ token, tokenType: "Bearer", expiresAt }`
- Include header `Authorization: Bearer <token>` for all `/api/audit-events/**` requests.

## Bruno API Tests
- Collection root: `bruno/`
- Environments: `bruno/environments/dev.bru`, `bruno/environments/prod.bru` (uses `baseUrl`)
- Requests grouped in `bruno/Tags`, `bruno/Photos`, `bruno/Claims`, `bruno/Audit Events`
- Open the `bruno/` folder in Bruno, choose an environment, then run tests in order (Auth → Tags → Photos → Claims → Audit Events). The Audit Events folder includes list, paginated list, filter example (with default variables), and get-by-id. The list/filter scripts capture the first event id to `{{auditEventId}}` for convenience.

## Build & Test
- Unit tests: `./mvnw test`
- Package: `./mvnw -DskipTests package`

## Notes
- If the app starts before MinIO, bucket provisioning waits up to ~60s with retries.
- For Mongo auth errors in dev, the most common cause is a reused volume created with different credentials; reset the volume or align the URI.

## License
This project is licensed under the WTFPL. See `LICENCE` for details.
