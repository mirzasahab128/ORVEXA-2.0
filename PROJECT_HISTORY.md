# ORVEXA — Project History & Technical Specification

**Application Name:** ORVEXA  
**Application ID:** `com.aistudio.ideaintel.vqzmtk`  
**Package Namespace:** `com.example`  
**Platform:** Android (API 24 to API 36)  
**Primary Language:** Kotlin (with Jetpack Compose Material 3)  
**Build System:** Gradle 9.3.1 (AGP 9.1.1)  

---

## 1. Executive Summary & Vision

**ORVEXA** is an AI-powered Idea Intelligence and Validation Platform designed for founders, venture architects, and product managers. Rather than treating ideas as static notes, ORVEXA transforms raw business concepts into validated, quantified, and actionable execution plans.

Key capabilities include:
- **Heuristic & AI Scoring:** Real-time computation of Feasibility, Opportunity, Market Fit, and Timing scores.
- **Google Search-Grounded Market Validation:** Live retrieval of search trends, competitor signals, and market velocity directly from Google Search.
- **Resilient Dual Storage:** Fast, fully offline-first local SQLite via Android Room with automatic cloud reconciliation to Firebase Firestore.
- **Zero Data-Loss Lifecycle:** Instant undo deletion snackbar paired with a dedicated Recycle Bin for recovering discarded concepts.
- **Haptic & Hands-Free Ergonomics:** Low-latency voice capture with deduplication, tactile feedback on all controls, and responsive UI density tuned for tall, high-DPI displays.

---

## 2. Chronological Development History

### Phase 1: Core Architecture & Data Modeling
- **Local Room Database:** Established `IdeaIntelligenceDatabase` and `IdeaDao` supporting entities for `Idea`, `ActionItem`, `ValidationExperiment`, `SmartReminder`, `ChatMessage`, and `ScoreHistoryEntry`.
- **Heuristic Scoring Engine:** Implemented algorithms assessing Opportunity, Market, Feasibility, Timing, Personal Fit, and Health scores (0–100 scale).
- **Core Screen Suite:**
  - `DashboardScreen`: Executive KPIs, portfolio distribution, and recent ideas.
  - `MyIdeasScreen`: Filterable idea grid/list view with swipe-to-delete.
  - `MatrixScreen`: Effort vs. Impact 2x2 quadrant visualization canvas.
  - `IdeaDetailScreen`: Deep-dive thesis breakdown, assumptions, and validation workflows.
  - `ExperimentsScreen` & `ActionsScreen`: Operational tracking for 7-day validation tests.

### Phase 2: Hybrid Cloud & Google Authentication
- **Firebase Firestore Integration:** Added zero-trust synchronization (`FirebaseSyncManager`) enabling cross-device backup and restoration.
- **Google Sign-In via Credential Manager:** Replaced mock sign-in with Jetpack `CredentialManager` (`signInWithGoogle`) and `GoogleIdTokenCredential`.
- **Offline-First Resilience:** Users can use the complete app offline without an account; signing in seamlessly links and backs up local data.
- **Background Auto-Reconciliation:** Implemented `NetworkConnectivityObserver` and `AutoSyncService` using Android `ConnectivityManager.NetworkCallback` to detect network availability and reconcile local Room changes with Firestore automatically.

### Phase 3: Hardware Controls, Voice STT & Tactile Haptics
- **Speech-to-Text Transcription:** Integrated Android `SpeechRecognizer` for voice capture of idea titles, theses, and notes.
- **Speech Duplication Bug Fix:**
  - Resolved an issue where partial recognition hypotheses repeated words 3 to 4 times.
  - Implemented `RobustVoiceInputController`: maintains a strict single prefix buffer (`basePrefix`), deduplicates delta phrases, and eliminates runaway auto-retry loops on silence timeouts.
- **Centralized Haptic System:** Built `HapticManager` providing standardized haptic feedback across the entire app:
  - `performClick()`: Subtle tactile feedback for buttons, chips, and tabs.
  - `performHeavyClick()`: Firm pulse for deletions, purge actions, and primary actions.
  - `performSuccess()`: Double pulse confirming recoveries, saves, and sync completions.
  - `performTick()`: Tactile ticks for toggles and filter chips.

### Phase 4: UI/UX Responsiveness & Density Overhaul
- **High-Density Display Tuning:** Fixed layout overflow and exaggerated visual elements on high-DPI and tall aspect ratio screens (e.g., OnePlus 13 / 13s).
- **Pinned TopAppBar (`PinnedTopAppBar`):**
  - Replaced scrolling headers with a fixed M3 top bar attached to `Scaffold(topBar = { ... })`.
  - Added edge-to-edge system insets via `.statusBarsPadding()`.
  - Standardized font scale: 18sp titles and 11sp metadata to retain an authentic native app layout rather than a sprawling web-feed appearance.

### Phase 5: Safety Architecture & Recycle Bin
- **5-Second Persistent Undo:** Built `UndoSnackbarListener` to keep a deletion snackbar active for 5 seconds, triggering an instant database restoration upon tapping **UNDO**.
- **Dedicated Recycle Bin (`RecycleBinScreen`):**
  - Added soft-delete support (`isDeleted = true`, `deletedAt = Long`).
  - Displays all deleted ideas with formatted deletion timestamps and category badges.
  - Provides one-tap **"Recover Idea"** with green accent and tactile confirmation.
  - Provides **"Delete Forever"** and **"Empty All"** with safety confirmation dialogs.
  - Integrated empty state illustration when the recycle bin has no items.

### Phase 6: Advanced AI Intelligence & Google Search Grounding
- **AI Analyzer Template Prompts (`AnalyzerTemplateChipsBar`):**
  - Added categorized starter prompts (*Risk & Assumptions*, *Pricing Models*, *7-Day Validation*, *Beachhead Market*, *Moat & Defensibility*) to unblock users when formulating prompts.
- **Gemini Idea Scorer (`GeminiIdeaScorer`):**
  - Connects to Google's `gemini-3.5-flash` model via REST API (`BuildConfig.GEMINI_API_KEY`).
  - Computes structured Feasibility (0–100), Innovation (0–100), and Market Viability scores with actionable recommendations.
  - Implemented deterministic heuristic fallback for offline scoring.
- **Visual Gauge Components (`ScoreVisualGauge` & `DualScoreGaugeBadge`):**
  - Animated canvas arc gauges rendering Feasibility and Innovation percentages on each idea card with color coding (Emerald Green >=75, Cyan/Blue 50–74, Amber 35–49, Coral Red <35).
- **Market Trend Analyzer (`MarketTrendAnalyzer` & Dialog):**
  - Leverages Gemini 3.5 Flash equipped with **Google Search Grounding** (`tools: [{"google_search": {}}]`).
  - Fetches real-time market search volume, trend velocity, sentiment (Bullish/Neutral/Bearish), competitor movements, and execution opportunity windows.
  - Accessible via the **"Market Trends"** button on each idea card.

### Phase 7: Real-Time Room Search & Tag Categorization
- **Local Room Search (`OrvexaSearchBar`):**
  - Live search input filtering ideas across title, thesis, description, and tag labels in real-time with instant debounce and voice search trigger.
- **Tag-Based Categorization (`TagFilterBar`):**
  - Filter chips for organizing ideas by project name, priority level (High, Medium, Normal), or custom user themes (`#tag`).
  - Seamlessly updates the main list without network latency.

---

## 3. Technology Stack & Dependencies

| Layer | Technology | Details |
|---|---|---|
| **Language** | Kotlin 2.2.10 | Coroutines, Flow, StateFlow, Suspend Functions |
| **UI Framework** | Jetpack Compose | Material Design 3 (M3), Scaffold, Canvas, Animation |
| **Local Storage** | Room 2.7.0 | SQLite database, DAO, TypeConverters |
| **Cloud Storage** | Firebase Firestore | Zero-trust document sync, real-time listeners |
| **Authentication** | Jetpack Credential Manager 1.5.0 | Google Sign-In (`GetSignInWithGoogleOption`) + Firebase Auth |
| **AI & LLM** | Gemini API (`gemini-3.5-flash`) | JSON Schema output, structured prompting |
| **Market Data** | Google Search Grounding | Live web trends and industry competitor telemetry |
| **Network & HTTP** | OkHttp 4.10.0 | High-performance HTTP client with timeout guards |
| **Audio & Voice** | Android SpeechRecognizer | Hands-free transcription with deduplication |
| **Haptics** | Android Vibrator / VibratorManager | Multi-tier haptic feedback |

---

## 4. Source Tree & Component Manifest

```
app/src/main/
├── AndroidManifest.xml
├── res/
│   ├── values/
│   │   ├── strings.xml         # app_name = "ORVEXA"
│   │   ├── colors.xml
│   │   └── styles.xml
│   ├── drawable/
│   └── mipmap/
└── java/com/example/
    ├── MainActivity.kt         # Edge-to-edge entry point
    ├── OrvexaApplication.kt    # Base Application instance
    ├── data/
    │   ├── model/
    │   │   ├── Idea.kt         # Core domain entity (scores, tags, thesis)
    │   │   ├── ActionItem.kt   # Operational tasks
    │   │   ├── ValidationExperiment.kt # 7-day hypothesis tests
    │   │   ├── SmartReminder.kt
    │   │   ├── ChatMessage.kt  # AI advisor scratchpad history
    │   │   └── UserProfile.kt  # Sync state and preferences
    │   ├── local/
    │   │   ├── IdeaDao.kt      # Room queries, filters, soft-delete
    │   │   ├── Converters.kt   # JSON/List converters
    │   │   └── IdeaIntelligenceDatabase.kt # SQLite database definition
    │   ├── remote/
    │   │   ├── GeminiIdeaScorer.kt       # Gemini 3.5 Flash scoring service
    │   │   ├── MarketTrendAnalyzer.kt    # Google Search Grounding tool
    │   │   ├── FirebaseSyncManager.kt    # Firestore and Auth manager
    │   │   ├── ConnectivityObserver.kt   # Network state flow observer
    │   │   └── AutoSyncService.kt        # Auto reconciliation background service
    │   └── repository/
    │       └── IdeaRepository.kt         # Single source of truth repository
    └── ui/
        ├── IdeaViewModel.kt              # Central state engine
        ├── util/
        │   └── HapticManager.kt          # Tactile confirmation manager
        ├── components/
        │   ├── PinnedTopAppBar.kt        # Fixed density top bar
        │   ├── EnhancedIdeaCard.kt       # Card with visual gauges & market button
        │   ├── ScoreVisualGauge.kt       # Animated canvas arc gauge
        │   ├── MarketTrendAnalyzerDialog.kt # Live trend modal dialog
        │   ├── OrvexaSearchBar.kt        # Real-time Room query bar
        │   ├── TagFilterBar.kt           # Project, priority & tag filter chips
        │   ├── AnalyzerTemplatePrompts.kt # AI prompt template chips
        │   ├── UndoSnackbarHost.kt       # 5-second undo restore listener
        │   └── VoiceInputController.kt   # Deduplicated speech recognizer
        └── screens/
            ├── DashboardScreen.kt        # Metrics, portfolio overview & list
            ├── MyIdeasScreen.kt          # Full list with search & tagging
            ├── RecycleBinScreen.kt       # Deleted ideas recovery & purge
            ├── AuthScreen.kt             # Google Sign-in & offline Room screen
            ├── AIAnalyzerScreen.kt       # Strategic thesis analysis & prompts
            ├── MatrixScreen.kt           # 2x2 Effort vs Impact matrix canvas
            ├── ExperimentsScreen.kt      # Validation experiments
            ├── ActionsScreen.kt          # Immediate execution roadmaps
            ├── RemindersScreen.kt        # Review trigger alerts
            └── ProfileScreen.kt          # User settings & cloud sync status
```

---

## 5. Build, Verification & Artifact Deployment

- **Keystore:** Signed with standard debug keystore (`androiddebugkey`, SHA-256 verified).
- **Signature Schemes:** Both v2 and v3 signature schemes enabled.
- **Alignment:** 4-byte zipaligned (`zipalign -c 4`).
- **Build Verification:** Successful incremental build via `compile_applet`.
- **Packaging:** Output synchronized to:
  - `app/build/outputs/apk/debug/app-debug.apk`
  - `.build-outputs/app-debug.apk`
  - `ORVEXA-debug.apk`

---
*Generated: October 2026 • AI Studio Build Engine*
