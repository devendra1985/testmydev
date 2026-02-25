# Argo CD GitOps CRDs

Apply these manifests to configure Argo CD declaratively.

## Apply Order

```bash
# 1. Create the AppProject first
kubectl apply -f argocd/app-project.yaml

# 2. Create the Application (demosvc)
kubectl apply -f argocd/application.yaml

# 3. (Optional) ApplicationSet for staging/prod - creates inventory-app-staging, inventory-app-prod
kubectl apply -f argocd/application-set.yaml
```

## Files

| File | Purpose |
|------|---------|
| `app-project.yaml` | `dev` project - restricts repos and namespaces |
| `application.yaml` | `demosvc` - deploys kube/ to dev namespace |
| `application-set.yaml` | Generates `inventory-app-staging`, `inventory-app-prod` |

## Note

- If `demosvc` was created via the Argo CD UI, applying `application.yaml` will update it to use the `dev` project and auto-sync.
- The ApplicationSet creates apps for staging/prod. Remove or comment out those environments if you don't need them yet.
