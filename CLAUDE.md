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

**Docker:**
```bash
docker-compose up             # Start app + MySQL locally
docker build -t taledevendra/my-app:<tag> .
```

**SonarQube:**
```bash
./mvnw clean verify sonar:sonar -Dsonar.host.url=<url> -Dsonar.login=<token>
```

## Architecture

Standard Spring Boot layered architecture: `Controller → Service → Repository → JPA Entity`

- **Controllers** (`controller/`): `HelloController` (greeting/health endpoints), `ProductController` (full CRUD for products)
- **Service** (`service/ProductService`): Business logic — CRUD, name search, category filter, low-stock alerts
- **Repository** (`repository/ProductRepository`): Spring Data JPA with custom query methods
- **Models** (`model/`): `Product` (inventory entity with SKU, category, price, quantity), `User` (id, name, email)

## Data

- **Local dev:** H2 in-memory database, auto-initialized from `src/main/resources/schema.sql` and `data.sql` (10 sample products)
- **Production:** MySQL 8.0 at `jdbc:mysql://localhost:3306/inventory_db`
- JPA DDL auto-mode is enabled; Hibernate formats SQL in logs

## Deployment Pipeline

**CI/CD:** Jenkins (`Jenkinsfile`) → SonarQube analysis → Docker build/push to Docker Hub → updates image tag in `kube/manf.yaml`

**GitOps:** ArgoCD (`argocd/application.yaml`) watches the `kube/` folder on `main` branch and auto-syncs to the cluster

**Kubernetes** (`kube/manf.yaml`): Uses Argo Rollouts with canary strategy (20% → 40% → 60% → 80%), MySQL deployment, readiness/liveness probes at `/testmydev/actuator/health`, PodDisruptionBudget requiring min 1 available pod
