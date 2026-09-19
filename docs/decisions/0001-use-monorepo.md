# ADR 0001: Use a Monorepo

- Status: Accepted
- Date: 2026-09-18

## Context

The Smart Service Desk Platform contains a React frontend, a Spring Boot backend, database configuration, documentation, and deployment configuration.

These parts must be developed, tested, versioned, and deployed together.

## Decision

The project will use one Git repository with separate `frontend`, `backend`, and `docs` directories.

## Reasons

- Keeps related frontend and backend changes in one commit.
- Simplifies Docker Compose configuration.
- Simplifies GitHub Actions workflows.
- Makes project setup easier for contributors.
- Keeps technical documentation close to the source code.

## Consequences

- Frontend and backend build commands remain separate.
- CI workflows must build and test both applications.
- The repository may become larger as the project grows.