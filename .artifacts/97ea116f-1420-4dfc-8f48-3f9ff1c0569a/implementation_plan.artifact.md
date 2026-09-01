# Implementación de Anuncios Recompensados para Pistas

Añadir una funcionalidad de "Pistas" (Hints) que los usuarios pueden desbloquear viendo un anuncio recompensado de AdMob. La lógica asegurará que solo se revelen atributos no adivinados previamente.

## User Review Required

> [!IMPORTANT]
> El ID de anuncio recompensado de prueba es `ca-app-pub-3940256099942544/5224354917`. Se usará este ID por ahora.

## Proposed Changes

### [Ad Infrastructure]

#### [NEW] [RewardedAdManager.kt](file:///C:/Users/ojon/AndroidStudioProjects/PokeGuess-GameAndroid/app/src/main/java/com/watsidev/pokeguessredux/ad/RewardedAdManager.kt)
Clase encargada de cargar y mostrar anuncios recompensados. Gestionará el pre-loading para que el anuncio esté listo cuando el usuario lo solicite.

#### [NEW] [AdModule.kt](file:///C:/Users/ojon/AndroidStudioProjects/PokeGuess-GameAndroid/app/src/main/java/com/watsidev/pokeguessredux/di/AdModule.kt)
Módulo de Hilt para proveer la instancia de `RewardedAdManager` como un Singleton.

### [Domain Layer]

#### [NEW] [HintType.kt](file:///C:/Users/ojon/AndroidStudioProjects/PokeGuess-GameAndroid/app/src/main/java/com/watsidev/pokeguessredux/domain/model/HintType.kt)
Enum que define los atributos que pueden ser revelados como pistas (GEN, STAGE, TYPES, HEIGHT, WEIGHT).

### [UI Layer]

#### [MODIFY] [GameViewModel.kt](file:///C:/Users/ojon/AndroidStudioProjects/PokeGuess-GameAndroid/app/src/main/java/com/watsidev/pokeguessredux/ui/game/GameViewModel.kt)
- Inyectar `RewardedAdManager`.
- Añadir lógica para seleccionar una pista aleatoria basada en el estado actual.
- Actualizar `GameUiState` con las pistas reveladas.

#### [MODIFY] [GameScreen.kt](file:///C:/Users/ojon/AndroidStudioProjects/PokeGuess-GameAndroid/app/src/main/java/com/watsidev/pokeguessredux/ui/game/GameScreen.kt)
- Añadir el botón de pista en la UI.
- Mostrar las pistas desbloqueadas sobre la lista de intentos.

## Verification Plan

### Automated Tests
- No hay tests automáticos planeados para la carga de anuncios (depende de SDK externo), pero se verificará la lógica de selección de pistas en el ViewModel.

### Manual Verification
- Abrir la pantalla de juego.
- Hacer un par de intentos fallidos.
- Pulsar el botón de pista.
- Verificar que se muestra el anuncio de prueba.
- Al terminar el anuncio, verificar que un atributo se revela y se mantiene visible.
- Verificar que no se repiten pistas.
