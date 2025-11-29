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
7.  **Strict Mode (Override)**:
    - Trigger a block (limit exceeded).
    - Click "5 More Minutes".
    - Verify the card content changes to a Math Challenge (Emergency Override).
    - Enter wrong answer -> Verify error message.
    - Enter correct answer -> Verify overlay disappears and app is usable.
    
### Profile Scheduling Verification
1.  **Configure Schedule**:
    - Create/Edit a profile.
    - Toggle "Enable Schedule" ON.
    - Set Start Time to 1 minute from now.
    - Set End Time to 1 hour from now.
    - Select current day (e.g., "T" for Tuesday).
    - Save/Back.
2.  **Verify Activation**:
    - Wait for the start time.
    - Verify profile shows "Scheduled: ..." on the list.
    - Verify apps in the profile are blocked/limited.
3.  **Verify Deactivation**:
    - Change End Time to 1 minute ago.
    - Verify profile is no longer effective (apps accessible).
4.  **Overnight Schedule**:
    - Set Start: 23:00, End: 07:00.
    - Verify it is active if current time is 23:30 or 06:00.

### UI Improvements Verification
1.  **Search Apps**:
    - Open Edit Profile.
    - Type in the search bar.
    - Verify app list filters correctly.
2.  **Delete Profile**:
    - Click Delete icon on a profile.
    - Verify confirmation dialog appears.
    - Cancel -> No delete.
    - Confirm -> Profile deleted.
3.  **Edit Profile**:
    - Click anywhere on a profile card (not the switch/delete).
    - Verify it opens the Edit Profile screen.
    - Verify there is no separate "Edit" icon.

### Take a Break Mode Verification
1.  **Activation & Custom Duration**:
    - Tap "Take a Break" on Dashboard.
    - Enter a custom duration (e.g., 0 Days, 1 Hour, 30 Mins).
    - Verify Dashboard shows "Break Active" and correct remaining time.
2.  **Enforcement & Countdown**:
    - Open a blocked app.
    - Verify Overlay shows "Take a Break" and a countdown in DD:HH:MM:SS format.
3.  **Breathing Exercise**:
    - Tap "Start Breathing Exercise" on the overlay.
    - Verify the breathing animation/text cycles (In/Hold/Out).
    - Tap "Finish Exercise" to return to the countdown.
4.  **Whitelist**:
    - Open Settings or Phone.
    - Verify they are accessible.
5.  **Cancellation**:
    - Tap "Tap to Stop" on Dashboard.
    - Verify break ends and apps are accessible.

## Next Steps
- **Refine Usage Stats**: Investigate `queryEvents` for more accurate "Today" stats if `INTERVAL_DAILY` proves insufficient.
- **Schedule Profiles**: Implement automatic profile activation.
