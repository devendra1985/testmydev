# Argo CD Setup Analysis — demosvc Application

## Current Configuration (from screenshot)

| Setting | Value |
|---------|-------|
| **Application** | demosvc |
| **Project** | default |
| **Status** | Healthy ✓ Synced ✓ |
| **Repository** | https://github.com/devendra1985/testmydev.git |
| **Path** | kube |
| **Target Revision** | HEAD (latest commit on default branch) |
| **Destination** | in-cluster |
| **Namespace** | dev |
| **Last Sync** | ~6 minutes ago |

---

## How It Benefits You

### 1. **GitOps flow is working**

```
Jenkins (build → push image → update kube/manf.yaml → push to Git)
                                    ↓
Argo CD (watches repo → detects change → syncs to cluster)
                                    ↓
Kubernetes (dev namespace) — MySQL + inventory-app deployed
```

- Jenkins updates the image tag in `kube/manf.yaml` and pushes to Git.
- Argo CD sees the change and syncs to the cluster.
- No manual `kubectl apply` needed.

### 2. **Single source of truth**

- Desired state lives in Git (`kube/manf.yaml`).
- Argo CD keeps the cluster in sync with that state.
- Drift is visible in the Argo CD UI.

### 3. **Visibility and control**

- Argo CD UI shows sync status and health.
- One-click sync and refresh.
- Easy rollback by reverting a Git commit and syncing.

### 4. **Namespace isolation**

- App is deployed to `dev`, not `default`.
- Keeps dev workloads separate from system namespaces.

---

## What Your Manifest Deploys (in `dev` namespace)

| Resource | Purpose |
|----------|---------|
| **mysql-secret** | DB credentials (from Secret) |
| **mysql** Deployment + Service | MySQL 8.0 for inventory_db |
| **inventory-app** Rollout | Spring Boot app with canary strategy |
| **inventory-app** Service (NodePort) | Stable traffic |
| **inventory-app-canary** Service (ClusterIP) | Canary traffic during rollout |

---

## Important: Argo Rollouts dependency

Your manifest uses `argoproj.io/v1alpha1` **Rollout** (canary deployment). That is managed by **Argo Rollouts**, not Argo CD.

- **Argo CD** — syncs manifests from Git to the cluster.
- **Argo Rollouts** — runs the canary logic (20% → 40% → 60% → 80% → 100%).

If Argo Rollouts is not installed, the Rollout resource will be created but the canary steps will not run. In that case you can either:

1. Install Argo Rollouts: `kubectl apply -n argo-rollouts -f https://github.com/argoproj/argo-rollouts/releases/latest/download/install.yaml`
2. Or switch back to a standard `Deployment` if you don’t need canary.

---

## Recommendations

### 1. **Use a fixed revision for production**

- `HEAD` tracks the latest commit, which can be risky.
- For production, consider `targetRevision: main` or a specific tag/branch.

### 2. **Enable auto-sync (optional)**

- If you want automatic sync on every Git push, enable it in the Application:
  ```yaml
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
  ```

### 3. **Create an AppProject for dev**

- Use a dedicated project (e.g. `dev`) instead of `default`.
- Restrict it to your repo and `dev` namespace for clearer RBAC.

### 4. **Verify Argo Rollouts**

- Check if Argo Rollouts is installed:
  ```bash
  kubectl get pods -n argo-rollouts
  ```
- If not, install it or replace the Rollout with a Deployment.

---

## Summary

| Question | Answer |
|----------|--------|
| **Is Argo CD helping?** | Yes. It syncs your manifests from Git to the cluster. |
| **Is the flow correct?** | Yes. Jenkins pushes to Git → Argo CD syncs to `dev`. |
| **What to watch?** | Ensure Argo Rollouts is installed if you want canary deployments. |
| **Next steps** | Consider auto-sync, fixed revision for prod, and a dedicated AppProject. |
