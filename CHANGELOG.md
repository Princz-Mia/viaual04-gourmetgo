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

## [0.2.0] - 2025-12-09
### Backend
#### Added
- **Feature (rewards):** implement comprehensive reward points system with points earning, category bonuses, redemption system and admin controls.
  - Added `RewardPoint`, `RewardTransaction`, and `CategoryBonus` entities for data modeling.
  - Created `IRewardService` interface and `RewardService` implementation for business logic.
  - Built repositories: `RewardPointRepository`, `RewardTransactionRepository`, `CategoryBonusRepository`.
  - Implemented `RewardController` with REST endpoints for points management.
  - Added DTOs: `RewardPointDto`, `RewardTransactionDto` for API responses.
- **Feature (promotions):** implement happy hour promotion system with time-based discounts, notifications and event handling.
  - Added `HappyHour` entity for promotion data modeling with time-based constraints.
  - Created `HappyHourNotificationService` for automated customer notifications.
  - Built `HappyHourRepository` for promotion data access and queries.
  - Implemented event-driven architecture with `HappyHourEvent` and `HappyHourEventListener`.
  - Added `PromotionController` with REST endpoints for promotion management.
- **Feature (analytics):** implement comprehensive dashboard statistics with charts, real-time data, and business insights.
  - Added `VisitStatistics` entity for tracking user interactions and platform metrics.
  - Created `IStatisticsService` interface and `StatisticsService` implementation for analytics business logic.
  - Built `VisitStatisticsRepository` for data aggregation and statistical queries.
  - Implemented event-driven analytics with `StatisticsEvent` for real-time data collection.
  - Added `StatisticsController` with REST endpoints for dashboard data retrieval.
  - Created DTOs: `AdminDashboardDto`, `RestaurantDashboardDto` for structured analytics responses.
- **Feature (reports):** implement PDF report generation with customizable templates and business insights.
  - Added `IPdfReportService` interface for report generation business logic.
  - Created `PdfReportService` implementation with PDF creation capabilities.
  - Integrated with existing statistics and analytics services for data aggregation.
  - Implemented customizable report templates for different business needs.
  - Added support for scheduled report generation and delivery.
- **Feature (chat):** implement real-time WebSocket chat system for customer support with persistent history.
  - Added `ChatMessage` and `ChatConversation` entities for persistent chat data storage.
  - Created `IChatService` interface and `ChatService` implementation for chat business logic.
  - Built repositories: `ChatMessageRepository`, `ChatConversationRepository` for data persistence.
  - Implemented `WebSocketConfig` for real-time WebSocket communication setup.
  - Added `ChatController` with REST endpoints for chat history and conversation management.
  - Created DTOs: `ChatMessageDto`, `ChatConversationDto` for structured chat responses.

#### Refactored
- **Refactor (authentication):** enhance JWT system with refresh tokens, session management and improved security.
  - Enhanced `JWTTokenProvider` with refresh token generation and validation capabilities.
  - Created `JWTAuthenticationFilter` for improved token processing and validation.
  - Added `AuthController` with dedicated authentication endpoints for login, refresh, and logout.
  - Implemented `SessionManagementService` for active session tracking and management.
  - Added `ActiveSession` entity for persistent session storage and monitoring.
  - Built `ActiveSessionRepository` for session data persistence and cleanup operations.

### Frontend
#### Added
- **Feature (rewards):** implement reward points UI components and pages.
  - Created `rewardApi.js` for API communication.
  - Built UI components: `RewardBalance`, `RewardHistory`, `RewardRedemption`.
  - Added customer-facing `RewardsPage` and admin `AdminRewards` page.
  - Integrated reward system into order flow and user dashboard.
- **Feature (promotions):** implement happy hour promotion UI components.
  - Created `promotionApi.js` for API communication with promotion endpoints.
  - Built UI components: `HappyHourBanner` for promotion display, `HappyHourModal` for details.
  - Integrated real-time promotion notifications and customer alerts.
  - Added responsive design for mobile and desktop promotion viewing.
- **Feature (analytics):** implement dashboard statistics UI components and analytics pages.
  - Created `statisticsService.js` for API communication with analytics endpoints.
  - Built reusable chart components: `LineChart`, `PieChart`, `StatCard` for data visualization.
  - Implemented dashboard components: `DashboardHeader`, `DateRangePicker`, `RealTimeStats`.
  - Added admin management components: `CompensationRewardModal`, `PromotionManagement`, `PromotionRewardModal`.
  - Created comprehensive dashboard pages: `AdminDashboard`, `RestaurantDashboard`, `BusinessInsights`.
  - Built specialized analytics pages: `UserAnalytics`, `RestaurantCustomerAnalytics`, `RestaurantMenuAnalytics`.
- **Feature (reports):** implement PDF report generation UI components and business insights.
  - Created `reportService.js` for API communication with report generation endpoints.
  - Built `ReportCustomizer` component for interactive report configuration.
  - Added `RestaurantBusinessTips` page with actionable business insights.
  - Integrated report download and preview functionality.
  - Implemented responsive design for report management interface.
- **Feature (chat):** implement real-time WebSocket chat UI components for customer support.
  - Created `chatService.js` for API communication and WebSocket connection management.
  - Built chat components: `CustomerChatWidget` for customer interface, `AdminChatPanel` for admin support.
  - Implemented `ChatContext` for global chat state management and real-time updates.
  - Added dedicated pages: `AdminChat` for support dashboard, `ChatTest` for development testing.
  - Integrated real-time message delivery with typing indicators and online status.
  - Added responsive design for mobile and desktop chat interfaces.

#### Refactored
- **Refactor (authentication):** enhance JWT authentication frontend with automatic token refresh and session management.
  - Enhanced `authService.js` with automatic token refresh and session management.
  - Improved `AuthContext` with better state management and authentication flow.
  - Created `jwt.js` utility for token handling, validation, and automatic refresh.
  - Integrated seamless token refresh without user interruption.
  - Added session timeout handling and automatic logout functionality.

> Notes:
> - **External behavior:** API/contract changes - new reward endpoints: `/api/rewards/*`, `/api/admin/rewards/*`; new promotion endpoints: `/api/promotions/*`, `/api/admin/promotions/*`; new statistics endpoints: `/api/statistics/*`, `/api/admin/dashboard/*`, `/api/restaurant/dashboard/*`; new report endpoints: `/api/reports/*`, `/api/admin/reports/*`, `/api/restaurant/reports/*`; new chat endpoints: `/api/chat/*`, `/api/admin/chat/*`; WebSocket endpoints: `/ws/chat`, `/ws/admin`; enhanced authentication endpoints: `/api/auth/refresh`, `/api/auth/logout`, `/api/auth/sessions`.
> - **Database schema:** New tables: `reward_points`, `reward_transactions`, `category_bonuses`, `happy_hours`, `visit_statistics`, `chat_conversations`, `chat_messages`, `active_sessions`.
> - **Migration required:** Database migration for reward, promotion, statistics, chat system, and session management tables required.

---

## [0.1.2] - 2025-11-01
### Backend
#### Added
- Implement logging strategy using **SLF4J** with **Logback** for improved observability and production monitoring.
- Structured logging with **correlation IDs** included in log entries.
- Profile-specific configurations:
  - `logback-spring.xml` with dev/prod profiles.
  - `application-dev.yml` and `application-prod.yml` for environment-specific settings.

#### Changed
- Updated `application.properties` to support logging properties and correlation ID propagation.
- Updated `.gitignore` to exclude sensitive or environment-specific configuration files.

### Frontend
- _No changes._

> Notes:
> - **External behavior:** No API/contract changes; no error payload or status code changes.
> - **Operations:** No migrations required (config/DB/env). Logs available in console and `logs/gourmetgo.log` depending on profile.

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
