<p align="center">
  <h1 align="center">🧘 MindFul Scrolling</h1>
  <p align="center">
    <strong>Take back control of your screen time.</strong>
  </p>
  <p align="center">
    A digital wellness Android app that monitors usage, enforces limits, and helps you build healthier phone habits — all with a beautiful Material 3 interface.
  </p>
  <p align="center">
    <img src="https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" />
    <img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose&logoColor=white" alt="Compose" />
    <img src="https://img.shields.io/badge/Min_SDK-26-brightgreen" alt="SDK" />
    <img src="https://img.shields.io/badge/Architecture-Clean_MVVM-orange" alt="Architecture" />
    <img src="https://img.shields.io/badge/License-MIT-blue" alt="License" />
  </p>
</p>

---

## ✨ Features

### 📊 Smart Dashboard
- **Real-time screen time** powered by Android's `UsageStatsManager` — the same API as Digital Wellbeing
- **Animated progress ring** tracking your daily goal (4h default)
- **7-day sparkline** showing your usage trend at a glance
- **20 personalized insights** that adapt to your behavior:
  - *"📉 3 days of declining screen time — keep it going!"*
  - *"⚠️ 82% of your daily goal used — maybe take a break?"*
  - *"⭐ 35% below your weekly average — excellent!"*

### ⏰ App & Group Limits
- Set **per-app daily limits** (e.g., Instagram: 30 min/day)
- **Group limits** for app categories (e.g., Social Media: 1h total)
- Beautiful **full-screen overlay** blocks apps when limits are exceeded
- **Emergency override** with math/typing challenge (not too easy!)

### 🧘 Take a Break Mode
- Select a duration → all non-essential apps are blocked
- **Focus Profile integration** — multi-select profiles to auto-whitelist their apps
- **Breathing animation overlay** with 3-phase cycle (Breathe In → Hold → Breathe Out)
- **Live countdown timer** with second-by-second updates
- 📞 **Phone** and 📷 **Camera** always accessible at bottom corners
- **Incoming calls never blocked** — InCallUI and Telecom are system-whitelisted

### 🎯 Focus Profiles
- Create named profiles (e.g., "Work", "Sleep", "Study")
- Per-app rules: **Block**, **Unlimited**, or **Custom limit**
- **Schedule-based** activation by time of day and day of week
- Integrates directly with Take a Break for quick whitelisting

### 📈 Detailed Analytics
- **Hourly usage chart** with normalized data matching system totals
- Per-app breakdown with **usage bars and percentages**
- **App launches**, **screen unlocks**, and **session timeline**
- Navigate between days with date picker

### 🔔 Smart Notifications
- Periodic usage alerts at configurable intervals
- Per-app notification thresholds
- Milestone-based warnings (50%, 75%, 100% of goal)

---

## 🏗️ Architecture

```
Clean Architecture  ·  MVVM  ·  Hilt DI  ·  Single-Activity (Compose Navigation)
```

```
┌─────────────────────────────────────────────────────┐
│  UI Layer (Compose Screens + ViewModels)             │
│  ┌─────────┐ ┌──────────┐ ┌────────┐ ┌───────────┐ │
│  │Dashboard│ │Analytics │ │Profiles│ │  Overlay  │ │
│  └────┬────┘ └────┬─────┘ └───┬────┘ └─────┬─────┘ │
├───────┼───────────┼───────────┼─────────────┼───────┤
│  Domain Layer (Use Cases + Repository Interfaces)    │
│  17 Use Cases · ManageBreak · CheckLimitExceeded     │
├───────┼───────────┼───────────┼─────────────┼───────┤
│  Data Layer                                          │
│  ┌────────┐ ┌──────────────┐ ┌───────────┐         │
│  │Room DB │ │UsageStatsAPI │ │ DataStore │         │
│  │6 tables│ │(System API)  │ │(Prefs)    │         │
│  └────────┘ └──────────────┘ └───────────┘         │
├─────────────────────────────────────────────────────┤
│  Services                                            │
│  AccessibilityService · ForegroundService · WorkMgr  │
└─────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **DI** | Hilt (KSP) |
| **Database** | Room (6 entities, 5 DAOs) |
| **Preferences** | DataStore |
| **Background** | AccessibilityService, Foreground Service, WorkManager |
| **Navigation** | Compose Navigation (single NavHost) |
| **Async** | Kotlin Coroutines + Flow |
| **Build** | Gradle KTS + Version Catalog |

---

## 📱 Screens

| Dashboard | Take a Break | Analytics | Focus Profiles |
|-----------|-------------|-----------|---------------|
| Screen time ring + sparkline | Duration picker + profile chips | Hourly bar chart | Profile cards with app rules |
| Personalized insights | Breathing animation overlay | Per-app breakdown | Schedule configuration |
| Quick analytics cards | Essential apps launcher | Session timeline | Multi-profile activation |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android device or emulator (API 26+)

### Build & Run

```bash
# Clone the repository
git clone https://github.com/8bin/MindFul.git
cd MindFul

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### Required Permissions

The app will guide you through granting these during onboarding:

| Permission | Purpose |
|-----------|---------|
| **Usage Stats Access** | Read app usage data from system |
| **Display Over Other Apps** | Show intervention overlay |
| **Accessibility Service** | Monitor foreground app changes |
| **Notifications** | Usage alerts and reminders |

---

## 📁 Project Structure

```
app/src/main/java/com/mindfulscrolling/app/
├── data/           # Room DB, DataStore, UsageStatsManager wrapper
├── di/             # Hilt modules (Database + Repository)
├── domain/         # Use cases, models, repository interfaces
├── service/        # Accessibility, Foreground, Notification services
├── ui/             # Compose screens, ViewModels, theme
│   ├── dashboard/  # Dashboard, Take a Break
│   ├── analytics/  # Charts, breakdowns
│   ├── overlay/    # Blocking & break overlays
│   ├── profiles/   # Focus profile management
│   └── theme/      # Material 3 colors, typography
└── worker/         # Periodic usage sync (WorkManager)
```

---

## 🔑 Key Design Decisions

- **System API as single source of truth** — Dashboard reads from `queryAndAggregateUsageStats()`, the same API Android Digital Wellbeing uses. No stale Room DB data.
- **30-second auto-refresh** — Dashboard metrics update automatically without manual pull-to-refresh.
- **Break state in DataStore** — Fast reads from the Accessibility Service (not Room).
- **Normalized hourly data** — Event-based hourly breakdown is scaled to match aggregate totals, preventing graph/total mismatches.
- **System whitelist for calls** — Phone, InCallUI, Telecom, and Camera are never blocked, even during breaks.

---

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  <strong>Built with ❤️ for digital wellness</strong>
  <br/>
  <sub>Because your time matters.</sub>
</p>
