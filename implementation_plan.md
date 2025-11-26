# Implementing Focus Profiles

Focus Profiles allow users to define different blocking rules for different contexts (e.g., "Work", "Sleep", "Study"). A profile consists of a name and a set of apps with specific limits or blocking rules.

## User Review Required
> [!NOTE]
> For this iteration, we will implement the CRUD operations for Profiles and the ability to manually activate a profile. Scheduling profiles will be a separate task.

## Proposed Changes

### Data Layer
#### [NEW] [FocusProfileEntity.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/data/local/entity/FocusProfileEntity.kt)
- `id`, `name`, `icon`, `isActive`

#### [NEW] [ProfileAppCrossRef.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/data/local/entity/ProfileAppCrossRef.kt)
- Many-to-many relationship between Profile and Apps (Packages).
- Additional fields: `limitDurationMinutes` (specific limit for this profile).

#### [MODIFY] [AppDatabase.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/data/local/AppDatabase.kt)
- Add new entities.

#### [NEW] [FocusProfileDao.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/data/local/dao/FocusProfileDao.kt)
- CRUD for profiles.
- Query to get active profile and its rules.

#### [MODIFY] [AppRepository.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/domain/repository/AppRepository.kt)
- Add methods for Profile management.

### Domain Layer
#### [NEW] [ManageFocusProfilesUseCase.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/domain/usecase/ManageFocusProfilesUseCase.kt)
- Create, Update, Delete, Activate profiles.

#### [MODIFY] [GetAppLimitUseCase.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/domain/usecase/GetAppLimitUseCase.kt)
- Update logic to check the *Active Profile* first. If a profile is active, use its limits. If not, fall back to global limits.

### UI Layer
#### [NEW] [FocusProfilesScreen.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/ui/profiles/FocusProfilesScreen.kt)
- List of profiles.
- "Create New Profile" button.
- Toggle to activate/deactivate.

#### [NEW] [EditProfileScreen.kt](file:///c:/App/MindFul/app/src/main/java/com/mindfulscrolling/app/ui/profiles/EditProfileScreen.kt)
- Edit name.
- Select apps and set limits for this profile.

## Verification Plan

### Manual Verification
1.  Create a profile "Work".
2.  Add "YouTube" to "Work" with a 0-minute limit (Block).
3.  Activate "Work" profile.
4.  Open YouTube -> Should be blocked immediately.
5.  Deactivate "Work" profile.
6.  Open YouTube -> Should work (or follow global limit).
