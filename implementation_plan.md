# Refine Total Screen Time Logic

## Goal Description
The user reported that "Total Screen Time" seems to include usage from the previous day ("two days"). This is likely due to `UsageStatsManager` returning daily buckets that overlap with the previous day when using `INTERVAL_DAILY`. The goal is to improve the accuracy of "Today's Usage" by using `INTERVAL_HOURLY` to aggregate usage stats, minimizing the inclusion of previous day's data. We will also ensure the time formatting in the Dashboard follows the "Xh Ym" format as requested.

## User Review Required
> [!NOTE]
> We are switching from `INTERVAL_DAILY` to `INTERVAL_HOURLY` for usage sync. This should provide more accurate "Today" stats but might result in slightly different numbers compared to system settings if the system uses a different aggregation method.

## Proposed Changes

### Data Layer

#### [MODIFY] [UsageStatsDataSource.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/data/datasource/UsageStatsDataSource.kt)
- Change `queryUsageStats` interval from `INTERVAL_DAILY` to `INTERVAL_HOURLY`.
- Implement logic to aggregate the hourly stats by package name.

#### [MODIFY] [UsageLogDao.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/data/local/dao/UsageLogDao.kt)
- Add query to fetch daily totals: `SELECT date, SUM(durationMillis) as totalDuration FROM usage_logs GROUP BY date ORDER BY date DESC`.
- Create a simple data class `DailyUsageSummary` to hold the result.

#### [MODIFY] [UsageRepository.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/domain/repository/UsageRepository.kt) & [Impl](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/data/repository/UsageRepositoryImpl.kt)
- Add `getUsageHistory(): Flow<List<DailyUsageSummary>>`.

### Domain Layer

#### [NEW] [GetUsageHistoryUseCase.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/domain/usecase/GetUsageHistoryUseCase.kt)
- Encapsulate the logic to fetch usage history.

### UI Layer

#### [MODIFY] [DashboardScreen.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/ui/dashboard/DashboardScreen.kt)
- Make the "Total Screen Time" card clickable.
- Add navigation callback `onNavigateToHistory`.

#### [NEW] [UsageHistoryScreen.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/ui/dashboard/UsageHistoryScreen.kt)
- Create a new screen to display a list of daily totals.
- Use a `LazyColumn` to show date and total duration.

#### [NEW] [UsageHistoryViewModel.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/ui/dashboard/UsageHistoryViewModel.kt)
- ViewModel for the history screen.

#### [MODIFY] [AppNavigation.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/ui/navigation/AppNavigation.kt) (or wherever navigation is defined)
- Add route for `UsageHistoryScreen`.

## Verification Plan

### Automated Tests
- Since we cannot easily mock `UsageStatsManager` in unit tests without complex setup, we will rely on manual verification.

### Manual Verification
1.  **Clear Data**: Clear app data to start fresh (optional).
2.  **Generate Usage**: Use some apps to generate usage.
3.  **Check Dashboard**: Verify "Total Screen Time" matches the sum of individual apps.
4.  **Verify "Today"**: Ensure usage from yesterday (if any) is not included. (Hard to test without waiting for tomorrow, but we can verify the logic change).
5.  **Check Formatting**: Verify time is shown as "Xh Ym" (e.g., "1h 30m").
