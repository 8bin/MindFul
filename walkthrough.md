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
- **Fixes**: 
    - Resolved `Unresolved reference` errors for `Icons.Filled.List` and `Icons.Filled.ArrowBack`.
    - Fixed `WorkManagerInitializer` lint error in `AndroidManifest.xml`.
    - Added `fallbackToDestructiveMigration()` to `DatabaseModule` to prevent crashes on schema updates.

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

### Usage History Verification
1.  **Check Dashboard**:
    - Verify "Total Screen Time" card is clickable.
    - Click on the card.
2.  **Verify History Screen**:
    - Ensure "Usage History" screen opens.
    - Verify list of dates and durations are displayed.
    - Verify the back button works.

### Focus Profile Verification
1.  **Blocking Logic**:
    - Activate a profile with a blocked app.
    - Open the blocked app.
    - Verify it is blocked immediately (Overlay shown).
2.  **Custom Time Limits**:
    - Edit a profile.
    - Select an app and choose "Custom".
    - Enter a time limit (e.g., 5 minutes).
    - Activate profile.
    - Use app for 5 minutes.
    - Verify it gets blocked after limit is reached.
3.  **App Sorting**:
    - Edit a profile.
    - Select a few apps.
    - Verify that selected apps move to the top of the list.
4.  **UI Clarity**:
    - Verify the label says "Daily Limit" instead of just "Limit".
    - Verify the dialog title says "Set Daily Time Limit".
5.  **Multiple Profiles**:
    - Create two profiles (e.g., "Work" and "Study").
    - Activate "Work". Verify it is active.
    - Activate "Study". Verify BOTH are active (switches ON).
    - Verify apps blocked in EITHER profile are blocked.
6.  **Compact UI**:
    - Edit a profile.
    - Verify "Block", "Allow", "Custom" chips are to the RIGHT of the app name.

## Next Steps
- **Refine Usage Stats**: Investigate `queryEvents` for more accurate "Today" stats if `INTERVAL_DAILY` proves insufficient.
- **Schedule Profiles**: Implement automatic profile activation.
