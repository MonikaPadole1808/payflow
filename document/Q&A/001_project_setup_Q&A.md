# 001_Project_Setup_QA.md

# PayFlow - Project Setup Interview Questions

> **Version:** 1.0  
> **Topic:** Project Initialization

---

# Q1. Why did you create a separate `dev` branch?

### Answer

The `dev` branch is used for daily development and experimentation. It allows incomplete or in-progress work to remain isolated from the stable codebase.

The `main` branch always contains tested, production-ready code.

---

# Q2. Why not develop directly on the `main` branch?

### Answer

Developing directly on the `main` branch increases the risk of introducing unstable code.

Using a dedicated development branch ensures:

- Stable production code
- Safer collaboration
- Easier rollback
- Cleaner release history

---

# Q3. Why are Notes and Q&A maintained separately?

### Answer

They serve as a learning resource throughout the project.

Notes capture technical understanding, while Q&A prepares for interviews by documenting common questions and concise answers.

---

# Q4. What is the purpose of `CLAUDE.md`?

### Answer

`CLAUDE.md` provides project-specific guidance for AI-assisted development, including architecture, coding conventions, and development workflow.

---

# Q5. Why create the database schema before implementing business logic?

### Answer

A well-designed schema provides a solid foundation for entity relationships and business rules, reducing refactoring later in development.

---

# Q6. What Git workflow is followed in PayFlow?

### Answer

```text
Create Feature
      │
      ▼
Develop on dev
      │
      ▼
Commit frequently
      │
      ▼
Test locally
      │
      ▼
Merge to main
```

---

# Q7. Why are Notes and Q&A not committed to the `main` branch?

### Answer

The `main` branch represents the production-ready application.

Notes and interview material are developer documentation intended only for the development branch.

---

# Q8. What was completed during Project Setup?

### Answer

- Project initialization
- Repository configuration
- Documentation creation
- Database initialization
- Git branching strategy
- Development workflow definition

---

# Summary

The project now has a stable technical foundation with a defined Git strategy, documentation process, and development workflow, allowing future features to be implemented in a structured and maintainable manner.