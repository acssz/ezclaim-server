# ACSSZ EzClaim Server

## Overview
The ACSSZ EzClaim platform powers reimbursement for the Association of Chinese Students and Scholars in Zurich. The server provides a secure REST API that replaces manual bookkeeping with automated claim processing, audit logging, and attachment handling.

## Highlights
- Spring Boot 3 service with MongoDB persistence and Kafka-based audit ingestion
- JWT-protected endpoints for claim, tag, photo, and audit management
- S3-compatible object storage integration for receipt uploads and presigned URLs
- Comprehensive OpenAPI definition (`api.json`) shared across the web and admin clients

## Getting Started
```bash
./mvnw spring-boot:run
```
The dev profile expects MongoDB, Kafka/Redpanda, and an S3-compatible store (see `docker-compose.dev.yml`). Generated OpenAPI can be viewed at `http://localhost:8080/swagger-ui/index.html` once the service is running.

## License
Licensed under the Do What The Fuck You Want To Public License (WTFPL). See [`LICENCE`](LICENCE).
