# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

> Versioning guidance:
> - If these refactors do **not** change externally observable behavior (HTTP status codes, error payload schema), release as **PATCH** (`0.1.1`).
> - If there are **backward-compatible** external changes (e.g., a new/structured error payload), release as **MINOR** (`0.2.0`).
> - Reserve **MAJOR** for breaking changes.

## [Unreleased]
> Collect changes here in your development branches. On release, move them to a new version block (e.g., `0.1.1` or `0.2.0`) with a date.

### Backend
#### Changed
- **Refactor (structure):** migrate from package-by-feature to package-by-layer to clarify responsibilities (e.g., `business/`, `data/`, `web/`).
- **Refactor (error handling):** unify and streamline error handling. Introduce standardized `ErrorResponse` shape and `ErrorType` enum; add domain-specific exceptions (e.g., `BusinessRuleException`, `ValidationException`); revise `RestExceptionHandler`.

### Frontend
- _No changes yet._

---

## [0.1.0] - 2025-10-13
### Added
- Initial pre-release baseline (state used to complete the 'Project Laboratory' (VIAUAL04) course). This marks the transition to a structured, industry-oriented development process.
