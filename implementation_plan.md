# Implementing UsageStatsManager Sync

The goal is to sync historical usage data from Android's `UsageStatsManager` to our local `UsageLog` database. This ensures data accuracy even if the Accessibility Service was disabled or the app was killed.

## User Review Required
> [!IMPORTANT]
> This feature requires the `PACKAGE_USAGE_STATS` permission, which is a special permission that the user must grant in system settings. We need to handle the case where this permission is not granted.

## Proposed Changes

### Data Layer
#### [NEW] [UsageStatsDataSource.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/data/datasource/UsageStatsDataSource.kt)
- Helper class to interact with `UsageStatsManager`.
- Methods to query usage stats for a given time range.

#### [MODIFY] [UsageRepository.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/domain/repository/UsageRepository.kt)
- Add method `syncUsage(usageStats: Map<String, Long>, date: Long)`.

#### [MODIFY] [UsageRepositoryImpl.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/data/repository/UsageRepositoryImpl.kt)
- Implement `syncUsage` to update local DB with system stats.
- Logic: If system stat > local stat, update local. (Trust system stats for historical data).

### Domain Layer
#### [NEW] [SyncUsageUseCase.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/domain/usecase/SyncUsageUseCase.kt)
- Orchestrates the sync process.
- Gets stats from `UsageStatsDataSource`.
- Calls `UsageRepository.syncUsage`.

### Worker Layer
#### [NEW] [SyncUsageWorker.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/worker/SyncUsageWorker.kt)
- WorkManager worker to run periodically (e.g., every 15 minutes).
- Calls `SyncUsageUseCase`.

### DI
#### [MODIFY] [AppModule.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/di/AppModule.kt)
- Provide `UsageStatsManager`.

## Verification Plan

### Manual Verification
1.  Grant Usage Access permission to the app.
2.  Use some apps (e.g., Chrome) for a few minutes.
3.  Trigger the worker manually (or wait for it).
4.  Check the database (via logs or UI) to see if usage is reflected.
