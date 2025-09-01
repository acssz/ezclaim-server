This folder contains Bruno-native tests (.bru) plus dev/prod environments.

Structure
- `collection.bru`: collection metadata
- `environments/dev.bru` and `environments/prod.bru`: set `baseUrl`
- Requests organized by feature in subfolders: Tags, Photos, Claims, Audit Events

Usage (Bruno)
- Open Bruno and add this folder as a collection
- Choose Environment (dev/prod) in the top bar
- Run requests top-to-bottom in each folder:
  - Tags: Create → Get → Update → List
  - Photos: Presign Upload → (upload via returned URL) → Create Photo Record → Get/List → Presign Download → Delete
  - Claims: Create (uses tagId/photoId from earlier) → Get → Update → List → Delete
  - Audit Events: List → Filter → Get by id

Notes
- Variables like `{{baseUrl}}`, `{{tagId}}`, `{{photoId}}`, `{{claimId}}` are stored in Bruno environment/runtime.
  - For Audit Events, you can set optional `entityType`, `action`, `from`, `to`, and `auditEventId`. The filter request supplies sensible defaults if not set.
- “Presign Download” may return 404 if the object hasn’t actually been uploaded to S3-compatible storage yet.
