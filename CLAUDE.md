# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
./mvnw clean compile          # Compile
./mvnw spring-boot:run        # Run locally (port 8087, context path /testmydev)
./mvnw test                   # Run all tests
./mvnw clean package          # Build JAR
java -jar target/testmydev-0.0.1-SNAPSHOT.jar  # Run JAR directly
```

**Docker (app + MySQL):**
```bash
docker-compose up
docker build -t taledevendra/my-app:<tag> .
```

**SonarQube:**
```bash
./mvnw clean verify sonar:sonar -Dsonar.host.url=<url> -Dsonar.login=<token>
```

## Architecture

Standard Spring Boot layered architecture: `Controller → Service → Repository → JPA Entity`

- **Controllers** (`controller/`): `HelloController` (greeting/health), `ProductController` (product CRUD), `AuthController` (form-based signup/login REST API at `/api/auth/*`)
- **Services** (`service/`): `ProductService` (CRUD, search, low-stock), `UserService` (form auth — plain-text password comparison), `SamlUserService` (auto-provisions users on first SAML login via `ApplicationListener<AuthenticationSuccessEvent>`)
- **Security** (`config/SecurityConfig.java`): Spring Security 6 with SAML2 SSO. The `RelyingPartyRegistrationRepository` bean is built statically (no network call at startup) using `RelyingPartyRegistration.withRegistrationId()`. Keycloak endpoints are configured from `KEYCLOAK_HOST` / `KEYCLOAK_REALM` env vars (defaults: `http://localhost:8080` / `myrealm`).
- **Models** (`model/`): `Product` (inventory: SKU, category, price, quantity), `User` (auth: username, password nullable for SAML users, samlNameId, samlProvider)

## Authentication: Two Parallel Flows

1. **Form login (REST):** `POST /api/auth/login` → `UserService.login()` → localStorage on client. CSRF disabled for `/api/auth/**`.
2. **SAML SSO:** `GET /saml2/authenticate/keycloak` → Keycloak → POST to `/login/saml2/sso/keycloak` → `SamlUserService` provisions/links user → redirect to `dashboard.html`.

The login page (`src/main/resources/static/login.html`) has both a form and a "Sign in with Keycloak SSO" button.

## Data

- **Local dev:** MySQL at `localhost:3306/inventory_db` (override via `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`). Schema auto-updated by Hibernate (`ddl-auto=update`), seeded by `schema.sql` + `data.sql`.
- **H2:** Not the default — MySQL connector is used even locally. Use `docker-compose up` to start MySQL.

## Deployment Pipeline

**CI/CD:** Jenkins (`Jenkinsfile`) → Docker build/push to Docker Hub (`taledevendra/my-app:<BUILD_NUMBER>`) → clones the **separate config repo** (`testmydev-config`) and runs `kustomize edit set image` in `overlays/dev` → commits/pushes the tag bump there. The app repo no longer contains Kubernetes manifests. Requires the `kustomize` CLI on the Jenkins agent.

**GitOps:** ArgoCD (`argocd/application.yaml`) watches `overlays/dev` in the `testmydev-config` repo on `main` and auto-syncs to cluster. Kustomize is auto-detected from `kustomization.yaml`.

**Kubernetes** (in `testmydev-config`): Kustomize `base/` (env-agnostic) + `overlays/dev/` (namespace + image tag). Resources: Argo Rollouts canary strategy (20% → 40% → 60% → 80%), MySQL deployment, Istio Gateway/DestinationRule/VirtualService, readiness/liveness probes at `/testmydev/actuator/health`, PodDisruptionBudget min 1 available. The image transformer rewrites the Rollout's image via Kustomize's wildcard `[noKind]` fieldspecs (no custom config needed).

## SAML / Keycloak Local Setup

Start Keycloak:
```bash
docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest start-dev
```

In Keycloak admin (`http://localhost:8080`): create realm `myrealm`, create SAML client with entity ID `http://localhost:8087/testmydev`, ACS URL `http://localhost:8087/testmydev/login/saml2/sso/keycloak`. SP metadata available at `http://localhost:8087/testmydev/saml2/metadata` once the app is running.
