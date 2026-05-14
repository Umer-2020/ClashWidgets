# ClashyWidgets: Development Plan

This document outlines the step-by-step implementation plan for building the ClashWidgets Android application.

## Phase 1: Project Setup & Core Architecture
1. **Architecture:** The app will be built using the **MVVM (Model-View-ViewModel)** architecture pattern.
2. **Orientation:** The app will strictly enforce **Portrait Mode** for both the main app and widgets. Landscape mode will not be supported.
3. **Initialize Project:** Ensure the project is set up for **Java**. Note: Jetpack Compose and Jetpack Glance are Kotlin-only frameworks and cannot be used in a pure Java project. We will use standard XML Layouts and Android AppWidgets (RemoteViews).
4. **Dependencies:** Add required dependencies in `build.gradle`:
   * Room (Local Database - Java support via Annotation Processors)
   * AppWidgetProvider (Standard Android Homescreen Widgets)
   * Navigation Component (For XML/Fragment navigation)

## Phase 2: Data Layer (Room Database)
We need to store the state of the builders and their current upgrades.
1. **Entities:**
   * `WorkerSlot`: Represents a specific builder/lab (e.g., `id = 1`, `name = "Home Builder 1"`, `village = "HOME"`, `type = "BUILDER"`).
   * `UpgradeTask`: Represents an active upgrade (e.g., `id = 10`, `slotId = 1`, `buildingName = "Archer Tower"`, `endTime = [timestamp]`).
   * `AppSettings`: A simple table or DataStore to save global states like "Goblin Event Active".
2. **DAOs (Data Access Objects):** Create queries to get active tasks, available builders, and calculate widget data.
3. **Repository:** Create a repository class to act as the single source of truth for the UI and the Widgets.

## Phase 3: The Main App UI (XML Layouts)
This is where the user will input data.
1. **Dashboard Screen:** A RecyclerView displaying all `WorkerSlots` grouped by village.
   * If a slot is empty, it shows "Available".
   * If a slot has an active `UpgradeTask`, it shows the building name and a countdown.
2. **Add Task Dialog/Screen:** When tapping an empty slot, open a bottom sheet or new screen.
   * Input: Building Name (Optional).
   * Input: Duration (Number pickers for Days, Hours, Minutes).
   * Logic: App calculates `endTime = currentTime + duration` and saves to DB.
3. **Helper Boost Logic:** Add a "Boost" button to active tasks. Pressing it subtracts hours (e.g., 8 hours for an 8x Apprentice) from the task's `endTime`.
4. **Settings Screen:** A simple screen with a toggle for the "Goblin Builder/Researcher Event".

## Phase 4: Homescreen Widgets (RemoteViews & AppWidgetProvider)
1. **Widget Receiver & State:** Set up `AppWidgetProvider` classes to listen to broadcasts and database changes.
2. **Widget #1 (Minimal):** 
   * Create standard XML layouts for the 2-row widget.
   * Query the DB to count active vs. total tasks using `RemoteViews` to update the UI.
   * Handle the Goblin Event toggle to switch the denominator to `?/7` and `?/2`.
3. **Widget #2 (Detailed):** 
   * Create detailed XML layouts.
   * Use Android's standard `Chronometer` view within `RemoteViews` so the timer ticks down automatically without draining battery.

## Phase 5: Alarms (AlarmManager)
1. **Alarm Scheduling:** Whenever a new `UpgradeTask` is created, use Android's `AlarmManager` (via `setExactAndAllowWhileIdle` or `setAlarmClock`) to schedule an alarm exactly 1 minute before the `endTime`.
2. **Alarm Trigger & Custom Sound:** The alarm will fire an intent that triggers a high-priority notification or a full-screen alarm activity playing the custom Clash of Clans "upgrade finished" sound effect.
3. **Boost Adjustments:** Ensure that if a user applies a Helper Boost, the scheduled alarm is cancelled and rescheduled for the new `endTime` minus 1 minute.

## Phase 6: Polish & Theming
1. **Assets:** Import Clash of Clans style icons for Builders, Labs, Pets, etc.
2. **Styling:** Apply custom fonts, colors (Gold, Elixir, Dark Elixir), and progress bar designs to match the game's aesthetic.
