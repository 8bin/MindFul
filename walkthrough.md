# Focus Profiles Walkthrough

## Overview
We have implemented the **Focus Profiles** feature, allowing users to create custom profiles (e.g., "Work", "Sleep") with specific app blocking rules.

## Changes
### Data Layer
- **New Entities**: `FocusProfileEntity` and `ProfileAppCrossRef`.
- **DAO**: `FocusProfileDao` for CRUD operations and active profile management.
- **Repository**: Updated `AppRepository` to support profile management.

### Domain Layer
- **New Use Case**: `ManageFocusProfilesUseCase` for profile logic.
- **Updated Use Case**: `GetAppLimitUseCase` now checks the *Active Profile* for limits before falling back to global limits.

### UI Layer
- **FocusProfilesScreen**: Lists all profiles, allows creating new ones, and toggling activation.
- **EditProfileScreen**: Allows renaming profiles and selecting apps to block/limit.
- **Dashboard**: Added a "Profiles" button to the top bar for easy access.

## Verification
### Automated Build Verification
- **Build Status**: Success
- **APK**: `app/build/outputs/apk/debug/app-debug.apk` generated.
- **Fixes**: Resolved `Unresolved reference` errors for `Icons.Filled.List` and `Icons.Filled.ArrowBack`.

### Manual Verification Steps
1.  **Create a Profile**:
    - Go to Dashboard -> Profiles (Person Icon).
    - Click FAB (+) to create a new profile (e.g., "Work").
2.  **Edit Profile**:
    - Click the Edit icon on the "Work" profile.
    - Select apps to block (e.g., YouTube).
    - Set limit to "Block" (0 minutes).
3.  **Activate Profile**:
    - Toggle the switch on the "Work" profile to ON.
    - Verify that "Active" label appears.
4.  **Test Blocking**:
    - Open the blocked app (YouTube).
    - It should be blocked immediately (if Accessibility Service is running).
5.  **Deactivate Profile**:
    - Toggle the switch OFF.
    - Open the app again. It should be accessible (unless global limit is reached).

## Next Steps
- Implement **Profile Scheduling** to automatically activate profiles based on time/day.
- Add more granular limits (e.g., specific time duration per profile).
