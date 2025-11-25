# Mindful Scrolling App – Feature Overview

## Core Functionality
- **Screen Time Monitoring**: Continuously tracks app usage via an Accessibility Service and `UsageStatsManager`.
- **App‑Specific Limits**: Users can set daily time limits per app or per app group.
- **App Groups**: Combine multiple apps (e.g., social media) under a single limit.
- **Take a Break Mode**: Temporarily suspend usage with a configurable break timer.
- **Night‑Mode Scheduling**: Enforce stricter limits during defined night hours.
- **Reel Scrolling Restriction**: Detect rapid scrolling patterns and intervene with a calming overlay.

## User Interface
- **Dashboard**: Daily/weekly/monthly usage statistics with charts and graphs.
- **Limits Screen**: Manage per‑app and group limits.
- **Settings**: Permission onboarding, night‑mode configuration, and general preferences.
- **Intervention Overlay**: System‑alert window that appears when limits are exceeded, offering options to dismiss or go home.
- **Emergency Override**: Quick access to bypass limits in urgent situations.

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
