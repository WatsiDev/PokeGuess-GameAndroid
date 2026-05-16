# Project Plan

Crea un juego tipo wordle pero con tema de pokemon, utiliza la API de pokemon, crea diferentes modos de juego como adivinar el pokemon, la generacion, o el objeto. Crea una mecanica para que cada dia se actualice un personaje para adivinar, como una racha. Además de tener un modo para jugar independientemente de la racha, donde se pueda jugar y adivinar uno tras otro. La mecanica para adivinar seria buscar en tiempo real mediante una barra de busqueda el pokemon para que vaya viendose los resutlados (limitado a unos 10 max) y cada resultado tendra unas caracteristicas, que al seleccionar un resultado el juego nos mostrará que caracteristicas coinciden. Utiliza un codigo de colores para definir si una caracteristica es correcta (verde), incorrecta (rojo) o parcialmente correcta (amarillo). Si alguna caracteristica puede tener opciones de mas alto o mas bajo, por ejemplo si consideras numero de la pokedex, que además del codigo de color se coloque una ayuda visual con unas flechas para definir si la caracteristica es menor o mayor. Utiliza retrofit para consumir la API y COIL para dibujar las imagenes mediante la URL de la API. Utiliza viewmodel e inyeccion de dependencias con hilt. Arquitectura MVVM.

## Project Brief

# Project Brief: PokeGuess

PokeGuess is a vibrant, Wordle-inspired Pokémon guessing game built with modern Android standards. Players test their Pokémon knowledge by identifying a mystery target through a process of elimination, comparing attributes like types, generation, and physical stats.

## Features
- **Daily & Infinite Game Modes:** A shared daily challenge with streak tracking and a dedicated infinite mode for practice and continuous play.
- **Smart Search & Suggestions:** Real-time auto-complete search functionality allowing users to quickly find and select any Pokémon from the Pokédex.
- **Attribute Comparison System:** Feedback logic that compares guesses against the target Pokémon using Material 3 color states (Green for match, Yellow for partial/near, Red for mismatch) and directional indicators for numerical values (Height, Weight, Generation).
- **Adaptive Material 3 Interface:** A fully responsive UI designed to provide a seamless experience across handhelds, foldables, and tablets using adaptive layouts.

## High-Level Technical Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM with Hilt (Dependency Injection)
- **Navigation:** Jetpack Navigation 3 (State-driven architecture)
- **Adaptive Strategy:** Compose Material Adaptive library (supporting various window size classes)
- **Networking:** Retrofit & Moshi (Integration with PokéAPI)
- **Image Loading:** Coil (Asynchronous loading of Pokémon sprites)
- **Asynchrony:** Kotlin Coroutines & Flow
- **Data Persistence:** DataStore (for user preferences and streaks)

## Implementation Steps
**Total Duration:** 4h 14m 54s

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

