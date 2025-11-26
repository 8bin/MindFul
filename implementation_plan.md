# Implementing Dashboard UI

The goal is to create a functional Dashboard UI that shows the user's daily usage stats and allows them to navigate to other features.

## User Review Required
> [!NOTE]
> The dashboard will initially show data from the local `UsageLog` database. We will need to ensure the data is refreshed reactively.

## Proposed Changes

### UI Layer
#### [MODIFY] [DashboardScreen.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/ui/dashboard/DashboardScreen.kt)
- **Header**: "Today's Usage" with total time.
- **Usage List**: List of apps with their usage duration and progress bar.
- **Navigation**: Buttons/Cards to navigate to "App Limits" and "Settings".
- **ViewModel**: `DashboardViewModel` to fetch usage data from `UsageRepository`.

#### [NEW] [DashboardViewModel.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/ui/dashboard/DashboardViewModel.kt)
- Fetches usage for the current day.
- Exposes `StateFlow<DashboardUiState>`.

### Domain Layer
#### [NEW] [GetDailyUsageUseCase.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/domain/usecase/GetDailyUsageUseCase.kt)
- Returns a flow of usage data for the current day, sorted by duration.

## Verification Plan

### Manual Verification
1.  Open the app.
2.  Verify that the dashboard shows the total usage time.
3.  Verify that the list shows apps used today.
4.  Verify navigation to "App Limits" and "Settings".
