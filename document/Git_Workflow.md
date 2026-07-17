# Git Workflow

---

# Branches

## main

Purpose

Stable production-ready branch.

Only tested and completed milestones are merged here.

---

## dev

Purpose

Daily development branch.

Every implementation, documentation update and bug fix is committed here first.

---

# Development Flow

Requirement

↓

Design

↓

Implementation

↓

Testing

↓

Notes Update

↓

Q&A Update

↓

Commit to dev

↓

Merge to main (Milestone Only)

---

# Commit Strategy

## dev

Frequent commits.

Example

- feat(auth): create user entity
- fix(payment): null pointer issue
- docs(notes): update authentication notes

---

## main

Milestone commits only.

Example

- release(v1): authentication completed
- release(v2): payment module completed