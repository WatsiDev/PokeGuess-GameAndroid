# Project Plan

Rediseño total de PokeGuess para que parezca una Pokédex real. Incluye pantalla Home, Game Screen con intentos y modal de victoria, Pokédex mejorada con siluetas y progreso, y pantalla de Ajustes con temas y reinicio. Estética minimalista en rojo/blanco/negro.

## Project Brief

# Project Brief: PokeGuess Redux

PokeGuess Redux is a minimalist, Pokédex-inspired guessing game. It combines the strategic deduction of Wordle with a comprehensive collection system, featuring a sleek red-and-white aesthetic and smooth, device-like animations.

## Features
- **Dual Play Modes:** Includes a "Daily" challenge to maintain streaks and an "Infinite" mode for unlimited practice sessions.
- **Smart Game Engine:** Features an attempt counter, real-time autocomplete search for Pokémon selection, and a victory modal to celebrate successful catches.
- **Interactive Pokédex Grid:** A collection system showing discovered Pokémon in a grid, utilizing silhouettes for undiscovered entries and tracking overall completion progress.
- **Pokédex Aesthetic UI:** A specialized Material 3 theme utilizing a bold red, white, and black palette, designed to look and feel like a high-tech handheld device.
- **Progress Management:** Integrated settings for light/dark/auto themes and a secure way to reset game progress and local discovery data.

## High-Level Technical Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM with Hilt (Dependency Injection)
- **Navigation:** Jetpack Navigation 3 (State-driven)
- **Adaptive Strategy:** Compose Material Adaptive (for responsive grid and list-detail layouts)
- **Networking:** Retrofit & Moshi (Integration with PokéAPI)
- **Persistence:** Room (Required for the local Pokédex cache and progress tracking)
- **Asynchrony:** Kotlin Coroutines & Flow
- **Image Loading:** Coil (Handling both sprites and silhouette transformations)

## Implementation Steps
**Total Duration:** 6h 30m 53s

### Task_1_Setup_Infrastructure: Set up Hilt dependency injection, Retrofit for PokeAPI, and define data models for Pokémon and items.
- **Status:** COMPLETED
- **Updates:** Hilt and Retrofit are configured. Data models for Pokemon and Item are created. PokemonRepository and UserPreferencesRepository are implemented. Project structure follows MVVM. Build successful.
- **Acceptance Criteria:**
  - Hilt is correctly configured in build files
  - Retrofit service for PokeAPI is implemented
  - Models for Pokémon attributes (types, generation, stats) are created
  - Repository pattern is established to fetch and cache data
  - Project builds successfully

### Task_2_Core_Game_Logic_ViewModel: Implement the game logic engine for comparing guesses and the ViewModel to manage game states and search suggestions.
- **Status:** COMPLETED
- **Updates:** Comparison engine (ComparePokemonUseCase) implemented with MatchState and Direction. GameViewModel manages Daily/Infinite modes, real-time search (max 10), and streaks. Unit tests for comparison logic implemented and passing. Domain models defined.
- **Acceptance Criteria:**
  - Comparison logic handles Green/Yellow/Red states and numerical arrows
  - Search logic provides real-time filtered results (max 10)
  - ViewModel exposes UI state for current guess list and game status (Daily/Infinite)
  - Unit tests for comparison logic pass
- **Duration:** 1h 1m 3s

### Task_3_Compose_UI_Implementation: Develop the UI using Jetpack Compose and Material 3, focusing on the search bar, attribute grid, and game modes.
- **Status:** COMPLETED
- **Updates:** Full UI implemented with Jetpack Compose. Search bar with auto-complete, attribute grid with color-coding and arrows, and support for Daily/Infinite modes are functional. Material 3 theme and adaptive icon integrated. Project follows MVVM and builds successfully.
- **Acceptance Criteria:**
  - Search bar with auto-complete suggestions is functional
  - Attribute grid displays color-coded comparison results (Green/Yellow/Red) and arrows
  - Coil is used to load Pokémon sprites
  - Material 3 theme with vibrant colors and edge-to-edge display is applied
  - UI matches the vibrant Pokémon aesthetic
- **Duration:** 1h 2m 18s

### Task_4_Persistence_and_Daily_Features: Implement DataStore for persistence, daily rotation logic, and streak tracking.
- **Status:** COMPLETED
- **Updates:** Persistence and daily features are fully implemented. DataStore stores streaks and daily guesses. Target Pokémon is date-seeded. Infinite mode is independent. App restores state correctly on launch. Navigation between modes verified.
- **Acceptance Criteria:**
  - Daily target Pokémon updates every 24 hours
  - User streaks are saved and loaded using DataStore
  - Infinite mode allows continuous play without affecting the daily streak
  - Navigation between modes works correctly
- **Duration:** 1h 3m 57s

### Task_5_Polish_and_Verification: Finalize the app with adaptive layouts, an app icon, and a comprehensive run and verify step.
- **Status:** COMPLETED
- **Updates:** The application has undergone final polish and verification. Adaptive layouts for phones and tablets are implemented. The adaptive app icon is active. Edge-to-edge display is verified. The critic agent confirmed the app is stable, functionally complete, and visually compliant with all Material 3 and Pokémon-specific requirements. All game modes and persistence logic are working as intended.
- **Acceptance Criteria:**
  - App supports adaptive layouts for different screen sizes
  - Adaptive app icon is created and applied
  - Edge-to-edge display is verified across all screens
  - Application is stable, no crashes during gameplay
  - Full compliance with user requirements for color coding and visual aids
- **Duration:** 1h 7m 36s

### Task_6_Pokedex_System_and_Room: Implement Room database for discovery tracking and build the Pokédex screen with a silhouette-based collection grid.
- **Status:** COMPLETED
- **Updates:** Room database and DAO implemented for discovery tracking. Pokedex screen built with a grid of all 1025+ entries. Silhouette logic using Coil ColorFilter implemented for undiscovered entries. Progress bar with percentage display added. Integration with GameViewModel to save wins automatically.
- **Acceptance Criteria:**
  - Room database correctly persists discovered Pokémon IDs
  - Pokédex screen displays a grid with silhouettes for undiscovered entries
  - Completion progress percentage is calculated and displayed
  - Coil transformations are used for silhouette effects
- **Duration:** 1h 12m 1s

### Task_7_Redux_UI_and_Navigation: Apply the minimalist Pokédex aesthetic (Red/White/Black), build the Home/Settings screens, and refine the Game UI with victory modals and attempt counters.
- **Status:** COMPLETED
- **Updates:** Pokedex Redux UI overhaul complete. Home screen added with Daily, Infinite, Pokedex, and Settings cards. Settings screen implements theme selection, progress reset, and app info. Game screen enhanced with attempt counter and high-impact victory modal. Material 3 theme updated with Pokedex-specific colors (Red/White/Black). Application navigation flow is verified.
- **Acceptance Criteria:**
  - Material 3 theme updated to minimalist red, white, and black palette
  - Home screen serves as the main entry point with navigation
  - Settings screen functional for theme switching and progress reset
  - Game screen includes attempt counter and celebratory victory modal
  - Application is stable, builds successfully, and does not crash
- **Duration:** 1h 3m 58s

