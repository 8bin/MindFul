# Implementing Core Monitoring Engine

The goal is to upgrade the `AccessibilityInterventionService` from a simple event listener to a robust monitoring engine that tracks app usage in real-time and enforces limits.

## User Review Required
> [!IMPORTANT]
> This implementation relies on `AccessibilityService` which requires the user to manually enable the service in Android Settings. We need to ensure the user is guided to do this (handled in a future task, but good to keep in mind).

## Proposed Changes

### Service Layer
#### [MODIFY] [AccessibilityInterventionService.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/service/AccessibilityInterventionService.kt)
- Implement a `Job` or `Handler` for periodic checks (e.g., every 1 second) when a monitored app is in the foreground.
- Track `startTime` when entering a package.
- On switching apps or periodic tick:
    - Calculate `duration`.
    - Update `UsageLog` via Repository.
    - Check if limit is exceeded.
    - Trigger `OverlayManager` if needed.

### Domain Layer
#### [NEW] [UpdateUsageUseCase.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/domain/usecase/UpdateUsageUseCase.kt)
- Create this new use case to handle safe updates to the usage log.

### Data Layer
#### [MODIFY] [UsageRepository.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/data/repository/UsageRepository.kt)
- Ensure methods exist to increment usage safely.

## Verification Plan

### Manual Verification
1.  Build and install the app.
2.  Enable Accessibility Service for "Mindful Scrolling".
3.  Set a limit for a specific app (e.g., Chrome) to 1 minute.
4.  Open Chrome and use it.
5.  Verify that usage is being logged (we can check logs or database).
6.  Verify that after 1 minute, the overlay appears.
