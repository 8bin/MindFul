# UsageStatsManager Sync Walkthrough

I have implemented the logic to sync historical usage data from Android's `UsageStatsManager` to the local database. This ensures that even if the Accessibility Service is disabled, we can still get accurate usage data (albeit with less granularity and real-time updates).

## Changes

### Data Layer
- **UsageStatsDataSource**: Created to fetch usage stats from the system.
- **UsageRepository**: Added `syncUsage` method to merge system stats with local logs.
- **UsageRepositoryImpl**: Implemented the merge logic (currently trusting system stats if they are higher).

### Domain Layer
- **SyncUsageUseCase**: Orchestrates the fetching and syncing process.

### Worker
- **SyncUsageWorker**: A WorkManager worker that runs `SyncUsageUseCase`.
- **Hilt Configuration**: Configured `MindfulScrollingApp` to provide a `HiltWorkerFactory` so we can inject dependencies into workers.

## Verification
- **Build Verification**: The project compiles successfully (`./gradlew assembleDebug`).
- **Dependency Check**: Added `androidx.hilt:hilt-work` and `androidx.hilt:hilt-compiler` to support Hilt with WorkManager.

## Next Steps
- Schedule the `SyncUsageWorker` to run periodically (e.g., every 15 minutes) in `MainActivity` or `Application` class.
- Handle the `PACKAGE_USAGE_STATS` permission request in the UI.
