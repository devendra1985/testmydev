# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Start local MySQL (required for non-test runs)
docker-compose up -d

# Run the application (http://localhost:8087/testmydev)
./mvnw spring-boot:run

# Build JAR
./mvnw clean package

# Run all tests (uses H2 in-memory, no MySQL needed)
./mvnw test

# Run a single test class
./mvnw test -Dtest=TestmydevApplicationTests

# Build Docker image
docker build -t taledevendra/my-app:latest .
```

## Architecture Overview

Spring Boot 3.2.2 / Java 17 REST API for product inventory management. Standard layered architecture:

- **Controller → Service → Repository** — `ProductController` delegates to `ProductService`, which wraps `ProductRepository` (Spring Data JPA)
- **HelloController** handles non-product endpoints (health, greeting) with no DB involvement
- **ProductRepository** uses derived query methods (no custom JPQL needed) — see `findBySku`, `findByCategory`, `findByNameContainingIgnoreCase`, `findByQuantityLessThan`

## Database

| Environment | DB | DDL mode |
|---|---|---|
| Local / Production | MySQL 8.0 on port 3306, `inventory_db` | `update` |
| Test | H2 in-memory (`testdb`) | `create-drop` |

`schema.sql` and `data.sql` in `src/main/resources/` initialize the schema and seed 10 sample products on startup (`spring.sql.init.mode=always`). Tests do not run these scripts (separate `src/test/resources/application.properties`).

Override DB connection at runtime via environment variables: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

## Kubernetes & GitOps

Deployment uses **Argo Rollouts** (not a standard Deployment) with a canary strategy (20% → 40% → 60% → 80% → 100%, 30s pauses). Two Kubernetes Services are defined: a stable NodePort and a canary ClusterIP.

Argo CD monitors `kube/manf.yaml` on the `main` branch with auto-sync, prune, and selfHeal enabled. The Jenkins CI pipeline (`Jenkinsfile`) builds the Docker image, pushes to Docker Hub as `taledevendra/my-app:{buildNumber}`, then commits the updated image tag back to `kube/manf.yaml` to trigger Argo CD.

Apply Argo CD resources in this order: `argocd/app-project.yaml` → `argocd/application.yaml`.

## Key Configuration

- App port: `8087`, context path: `/testmydev`
- Actuator endpoints (health, info, metrics) at `/testmydev/actuator/`
- Readiness/liveness probes in k8s point to `/testmydev/actuator/health`
- MySQL credentials in k8s are stored in a Secret (`kube/manf.yaml`) — base64-encoded values are `rootpassword` / `inventory_db` / `root`
- The `User` entity and `users` table exist in the schema but have no controller or service layer

## CI/CD Requirements (Jenkins)

Three credentials must be configured in Jenkins:
- `docker-hub-credentials` — DockerHub login
- `sonarqube-token` — SonarQube token (server at `http://host.docker.internal:9002`)
- `github-credentials` — GitHub token for committing the manifest update
