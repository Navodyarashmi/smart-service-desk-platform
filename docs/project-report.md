# HelpHub project report

## Purpose

HelpHub is a full-stack service desk platform designed to demonstrate production-oriented software engineering. It replaces informal support requests with authenticated tickets, guarded workflows, collaboration history, notifications, attachments, and role-specific operational views.

## Architecture

The React and TypeScript single-page application communicates with a stateless Spring Boot REST API. Spring Security validates short-lived HMAC-signed JWTs. JPA persists identity, ticket, activity, notification, and attachment metadata in PostgreSQL. Flyway owns repeatable schema evolution. Nginx serves the production frontend and proxies same-origin API traffic.

## Role capabilities

| Role | Main capabilities |
| --- | --- |
| Employee | Register, create and search tickets, edit or cancel eligible requests, comment, attach files, and follow notifications |
| Technician | Review the operations queue, claim work, add public or internal notes, and progress guarded ticket states |
| Administrator | Oversee every ticket, manage account roles and access state, and use all staff collaboration tools |

## Engineering controls

- BCrypt password hashing and 15-minute JWT access tokens
- Ownership-scoped requester queries and role-scoped staff/admin endpoints
- Strict validation, consistent API errors, security headers, and non-public attachment storage
- File type allowlisting, signature validation, randomized storage names, and 5 MB upload limits
- PostgreSQL constraints, optimistic locking, and Flyway migrations
- Backend integration tests, native frontend logic tests, linting, production builds, container builds, and CI
- Health checks, non-root application containers, persistent volumes, and deployment documentation

## Outcome

The project demonstrates frontend development, REST API design, relational modelling, authentication and authorization, secure file handling, automated testing, documentation, containerization, CI, and release preparation in one coherent portfolio application.

## CV summary

Built a role-based smart service desk using React, TypeScript, Spring Boot, PostgreSQL, JWT, Flyway, Docker, Nginx, and GitHub Actions. Implemented guarded ticket workflows, administrator access management, comments, internal notes, audit history, notifications, secure attachments, analytics, integration tests, and production deployment controls.
