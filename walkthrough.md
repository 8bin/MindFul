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
    - Verify Overlay appears almost instantly (< 200ms).
    - **Verify Overlay is stable and does NOT flicker.**
    - Verify Overlay shows "Take a Break" and a countdown in DD:HH:MM:SS format updating every second.
3.  **Breathing Exercise**:
    - Tap "Start Breathing Exercise" on the overlay.
    - Verify the breathing animation (circle expanding/shrinking) and text fading (Crossfade) are smooth.
    - Tap "Finish Exercise" to return to the countdown.
4.  **System Whitelist**:
    - Open Settings, Phone, or use the Keyboard.
    - Verify they are NOT blocked (even without adding to manual whitelist).
5.  **Auto-Dismiss**:
    - Wait for the break to expire (or stop it from Dashboard).
    - Verify the overlay automatically disappears if an app was blocked.

### Settings & Permissions Verification
1.  **Theme Switching**:
    - Go to Settings -> General -> Theme.
    - Change to Dark. Verify app goes dark immediately.
    - Change to Light. Verify app goes light.
    - Change to System. Verify it matches system setting.
2.  **Strict Mode - Enable**:
    - Go to Settings -> Strict Mode.
    - Toggle ON.
    - Verify "Set PIN" dialog appears.
    - Enter PIN (e.g., 1234). Confirm PIN.
    - Verify Switch is ON.
3.  **Strict Mode - Enforcement**:
    - Try to turn Strict Mode OFF.
    - Verify "Enter PIN" dialog appears.
    - Enter wrong PIN. Verify error.
    - Enter correct PIN. Verify Switch turns OFF.
4.  **About Section**:
    - Verify Version is displayed as "1.0.0".

### Bug Fixes & Improvements Verification
1.  **Time Tracking**:
    - Check "Usage History". Verify total time for "Today" is reasonable (e.g., < 24h).
    - Compare with system Digital Wellbeing. The values should now be very close (within a few minutes).
    - Ensure no system apps (like "Android System") are appearing in the list unless they have a launch intent.
2.  **Keyboard**:
    - Trigger "Emergency Override" (math challenge).
    - Click on the answer field.
    - Verify keyboard appears.
3.  **Strict Mode**:
    - Enable Strict Mode. Set PIN.
    - Verify "Change PIN" option appears.
    - Click "Change PIN". Enter Old PIN, then New PIN.
    - Verify PIN is changed (try to disable with old PIN -> fail, new PIN -> success).
    - Enter wrong PIN when disabling. Verify error message.
4.  **Take a Break Enhancements**:
    - **Default Profile**: Clear app data or reinstall. Verify "Essential" profile exists in "Focus Profiles".
    - **New Screen**: Click "Take a Break" on Dashboard. Verify it opens a full screen (not popup).
    - **Profile Selection**: Select "Essential" profile. Verify it shows allowed apps (Phone, etc.).
    - **Start Break**: Select duration and start. Verify break starts and only profile apps are allowed.
5.  **UI Overhaul**:
    - **Bottom Navigation**: Verify Dashboard, Modes, and Analytics tabs are visible and clickable.
    - **Dashboard Sections**:
        - **Welcome**: Check for "Hello, User".
        - **Permissions**: Check for "All Systems Go".
        - **Analytics**: Scroll horizontally to see all cards (Screen Time, App Launches, etc.).
        - **Take a Break**: Click and verify it opens the full screen.
        - **Strictness**: Verify "Moderate" level is shown.
        - **Quick Actions**: Verify 5 buttons (Apps, Sites, Keyword, Adult, Reels) are present.
        - **Profiles**: Click "Focus Profiles" and verify it opens the profile list.
6.  **UI Refinements**:
    - **Settings Button**: Verify it is now at the top-left corner of the Dashboard.
    - **Modes Screen**:
        - Navigate to "Modes" tab.
        - Verify 3 cards: Normal, Lock, Strict.
        - Toggle switches and verify visual state changes.
7.  **Analytics Enhancement**:
    - **Dashboard**: Verify "Usage Time" is renamed to "Usage Timeline".
    - **Navigation**:
        - Click "Screen Time" card -> Verify Analytics Screen opens with "Screen Time" tab selected.
        - Click "Usage Timeline" card -> Verify Analytics Screen opens with "Usage Timeline" tab selected.
    - **Analytics Screen**:
        - Verify 4 sections: Date Selector, Tabs, Graph, Usage Overview.
        - Scroll tabs horizontally.
        - Verify clicking tabs updates the selected tab state.
8.  **Strictness Navigation**:
    - **Dashboard**: Click "Strictness Level" card.
    - **Verification**: Verify it navigates to the "Modes" tab.

## Next Steps
- **Refine Usage Stats**: Investigate `queryEvents` for more accurate "Today" stats if `INTERVAL_DAILY` proves insufficient.
- **Schedule Profiles**: Implement automatic profile activation.
