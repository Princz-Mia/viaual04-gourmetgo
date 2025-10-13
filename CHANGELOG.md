# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

> Versioning guidance:
> - If changes do **not** alter externally observable behavior, release as **PATCH**.
> - If there are **backward-compatible** external changes, release as **MINOR**.
> - Reserve **MAJOR** for breaking changes.

## [Unreleased]
### Backend
- _No changes yet._
### Frontend
- _No changes yet._

---

## [0.1.1] - 2025-10-13
### Backend
#### Changed
- **Refactor (structure):** migrate from package-by-feature to package-by-layer to clarify responsibilities (e.g., `business/`, `data/`, `web/`).
- **Refactor (error handling):** unify and streamline error handling. Introduce standardized `ErrorResponse` schema (e.g., `timestamp`, `path`, `errorType`, `message`) and `ErrorType` taxonomy; add domain-specific exceptions (e.g., `BusinessRuleException`, `ValidationException`); revise `RestExceptionHandler`.
- **Statuses:** expanded/normalized mapping (e.g., 400 validation, 404 not found, 409 conflict, 500 internal).

### Frontend
- _No changes._

> Notes: Treated as **MINOR** due to externally visible but backward-compatible error response improvements. If any client relies on the previous error payload shape or specific status codes, review and adjust client-side handling accordingly.

## [0.1.0] - 2025-10-13
### Added
- Initial pre-release baseline (state used to complete the 'Project Laboratory' (VIAUAL04) course). This marks the transition to a structured, industry-oriented development process.
