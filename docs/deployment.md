# Production deployment

HelpHub ships as three containers: Nginx/React, Spring Boot, and PostgreSQL. The production Compose file exposes only Nginx; the API and database remain on a private Docker network.

## Host requirements

- A Linux host or container platform with Docker Engine and Compose
- HTTPS termination through the platform load balancer or a trusted reverse proxy
- Persistent volumes for PostgreSQL and `/app/data/attachments`
- Automated backups for both persistent volumes

## Prepare secrets

Copy `.env.production.example` to `.env.production` and replace every placeholder. Generate `JWT_SECRET` from 32 cryptographically random bytes. Keep demo seeding disabled.

```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

Never commit `.env.production`.

## Build and start

```bash
docker compose --env-file .env.production -f compose.production.yaml config --quiet
docker compose --env-file .env.production -f compose.production.yaml up --build -d
docker compose --env-file .env.production -f compose.production.yaml ps
```

Open `http://HOST:APP_PORT/health` for the web health check. The platform should terminate TLS and forward HTTPS traffic to `APP_PORT`.

## Release checks

1. Confirm the database and attachment volumes are persistent.
2. Confirm only the frontend port is public.
3. Confirm `/actuator/health` is not exposed through Nginx.
4. Confirm HTTPS, backup schedules, log retention, and monitoring alerts.
5. Run a restore drill before treating backups as reliable.

## Upgrade and rollback

Back up both volumes, pull the approved commit, build new images, and start the stack. Flyway applies forward-only database migrations during backend startup. For rollback, restore the matching application version and database backup together; do not manually reverse production migrations.
