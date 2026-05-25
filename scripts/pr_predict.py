"""
Runs inside GitHub Actions on every PR.
Fetches PR facts, runs prediction, posts result as a PR comment.
Exits with code 0 always — this is advisory, not a gate.

Required env vars:
  GITHUB_TOKEN       - token with repo + PR comment permissions
  GITHUB_REPOSITORY  - owner/repo  (set automatically by Actions)
  PR_NUMBER          - PR number   (set by the workflow)
  PREV_LABEL         - label of the last completed build (0/1), default 0
  BUILDS_SINCE       - consecutive failures before this PR, default 0
"""

import json
import os
import sys
from pathlib import Path

import joblib
import numpy as np
from github import Auth, Github

# ── Load model ────────────────────────────────────────────────────────────────
MODEL_PATH = Path(__file__).parent.parent / "model" / "artifacts" / "model.joblib"
if not MODEL_PATH.exists():
    print(f"::error::Model not found at {MODEL_PATH}. Commit model/artifacts/model.joblib to the repo.")
    sys.exit(1)

clf = joblib.load(MODEL_PATH)

FEATURE_COLS = [
    "additions", "deletions", "changed_files", "commit_count",
    "touches_infra", "touches_tests", "touches_src", "touches_kube",
    "is_direct_push", "review_count", "requested_reviewers", "has_pr",
    "build_hour", "is_off_hours", "is_weekend",
    "prev_label", "builds_since_last_success",
]

INFRA_PATTERNS = ("Jenkinsfile", "Dockerfile", "docker-compose", "pom.xml")
TEST_PATTERNS  = ("Test", "test", "spec", "Spec")

# ── GitHub context ─────────────────────────────────────────────────────────────
token      = os.environ["GITHUB_TOKEN"]
repo_slug  = os.environ["GITHUB_REPOSITORY"]
pr_number  = int(os.environ["PR_NUMBER"])
prev_label = int(os.environ.get("PREV_LABEL", "0"))
builds_since = int(os.environ.get("BUILDS_SINCE", "0"))

g    = Github(auth=Auth.Token(token))
repo = g.get_repo(repo_slug)
pr   = repo.get_pull(pr_number)

# ── Extract features from the PR ──────────────────────────────────────────────
files = [f.filename for f in pr.get_files()]
dt    = pr.created_at
hour  = dt.hour

features = {
    "additions":               pr.additions,
    "deletions":               pr.deletions,
    "changed_files":           pr.changed_files,
    "commit_count":            pr.commits,
    "touches_infra":           int(any(p in f for f in files for p in INFRA_PATTERNS)),
    "touches_tests":           int(any(p in f for f in files for p in TEST_PATTERNS)),
    "touches_src":             int(any("src/" in f for f in files)),
    "touches_kube":            int(any("kube/" in f for f in files)),
    "is_direct_push":          0,
    "review_count":            pr.review_comments,
    "requested_reviewers":     len(list(pr.get_review_requests()[0])),
    "has_pr":                  1,
    "build_hour":              hour,
    "is_off_hours":            int(hour < 8 or hour >= 20),
    "is_weekend":              int(dt.weekday() >= 5),
    "prev_label":              prev_label,
    "builds_since_last_success": builds_since,
}

X = np.array([[features[c] for c in FEATURE_COLS]])
prob = float(clf.predict_proba(X)[0][1])
risk = "high" if prob >= 0.7 else "medium" if prob >= 0.4 else "low"
prediction = "FAILURE" if prob >= 0.5 else "SUCCESS"

RISK_BADGE = {
    "high":   "![high](https://img.shields.io/badge/risk-HIGH-red)",
    "medium": "![medium](https://img.shields.io/badge/risk-MEDIUM-yellow)",
    "low":    "![low](https://img.shields.io/badge/risk-LOW-brightgreen)",
}
RISK_ICON = {"high": "🔴", "medium": "🟡", "low": "🟢"}

# ── Build reasons list ────────────────────────────────────────────────────────
reasons = []
if features["touches_kube"]:              reasons.append("`kube/` manifests changed")
if features["touches_infra"]:             reasons.append("infra files changed (`Jenkinsfile`/`Dockerfile`/`pom.xml`)")
if prev_label == 1:                       reasons.append("previous build **FAILED**")
if builds_since > 0:                      reasons.append(f"{builds_since} consecutive failure(s) before this PR")
if features["is_off_hours"]:              reasons.append(f"PR opened off-hours (UTC {hour:02d}:xx)")
if features["is_weekend"]:                reasons.append("PR opened on a weekend")
if pr.additions > 100:                    reasons.append(f"large change ({pr.additions} lines added)")
if features["touches_tests"] == 0 and features["touches_src"]: reasons.append("source changed but no test files touched")

reason_lines = "\n".join(f"- {r}" for r in reasons) if reasons else "- No strong risk signals detected."

rec = {
    "high":   "⚠️ **Recommendation:** Review carefully. Consider splitting into smaller PRs or running the build locally first.",
    "medium": "⚡ **Recommendation:** Monitor the build closely after merge.",
    "low":    "✅ **Recommendation:** Looks good — low chance of CI failure.",
}

comment_body = f"""## CI Failure Predictor {RISK_BADGE[risk]}

| | |
|---|---|
| **Failure probability** | `{prob*100:.1f}%` |
| **Prediction** | `{prediction}` |
| **Risk level** | {RISK_ICON[risk]} `{risk.upper()}` |

### Why
{reason_lines}

{rec[risk]}

<sub>Predicted from {pr.changed_files} changed file(s), {pr.additions}+/{pr.deletions}- lines, commit history context. Model trained on {repo_slug} Jenkins build history.</sub>
"""

# ── Delete previous bot comment (avoid duplicate spam) ───────────────────────
for c in pr.get_issue_comments():
    if "CI Failure Predictor" in c.body:
        c.delete()

pr.create_issue_comment(comment_body)
print(f"Posted prediction to PR #{pr_number}: {prob*100:.1f}% failure ({risk.upper()})")

# Emit as GitHub Actions step summary
summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
if summary_path:
    with open(summary_path, "a") as f:
        f.write(comment_body)
