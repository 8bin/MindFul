# Mindful Scrolling App – Feature Overview

## Core Functionality
- **Screen Time Monitoring**: Continuously tracks app usage via an Accessibility Service and `UsageStatsManager`.
- **App & Website Limits**: Users can set daily time limits per app, website, or category.
- **Focus Profiles**: Create custom profiles (e.g., "Work", "Sleep") with specific blocked apps and schedules.
- **Strict Mode**: Advanced protection to prevent bypassing limits (e.g., PIN, complex tasks to unlock, preventing uninstall).
- **Take a Break Mode**: Temporarily suspend usage with a configurable break timer.
- **Night‑Mode Scheduling**: Enforce stricter limits during defined night hours.
- **Reel Scrolling Restriction**: Detect rapid scrolling patterns and intervene with a calming overlay.

## User Interface
- **Dashboard**: Daily/weekly/monthly usage statistics with charts, graphs, and a **Timeline View**.
- **Limits & Profiles**: Manage per‑app limits and configure Focus Profiles.
- **Settings**: Permission onboarding, strict mode configuration, and general preferences.
- **Intervention Overlay**: System‑alert window that appears when limits are exceeded.
- **Strict Unlock**: Challenges (Math, Typing) required to override limits in Strict Mode.

## Background Services
- **AccessibilityInterventionService**: Core engine that monitors foreground app changes and scrolling events.
- **UsageStatsManager Integration**: Syncs historical usage data for analytics.
- **Foreground Service**: Keeps the “Take a Break” mode active when the app is in the background.

## Technical Stack
- **Kotlin** with **Jetpack Compose** for UI.
- **Hilt** for dependency injection.
- **Room** for local persistence of limits, groups, usage logs, and override logs.
- **Coroutines** for asynchronous operations.
- **DataStore** (planned) for user preferences.

---
*This file provides a concise overview of the app’s planned and implemented features for quick reference.*
