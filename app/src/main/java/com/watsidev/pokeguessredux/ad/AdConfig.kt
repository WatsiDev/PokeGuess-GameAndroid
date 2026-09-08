package com.watsidev.pokeguessredux.ad

/**
 * Centralized AdMob Configuration.
 *
 * To switch the entire app between Test IDs and Production IDs,
 * simply change [IS_TEST_MODE] to `true` (for testing) or `false` (for production).
 */
object AdConfig {

    /**
     * Set to `true` for development and testing.
     * Set to `false` when building for production release.
     */
    const val IS_TEST_MODE = true

    // App ID
    const val APP_ID_TEST = "ca-app-pub-3940256099942544~3347511713"
    const val APP_ID_PROD = "ca-app-pub-9489490067134108~2811599779"
    val ADMOB_APP_ID: String get() = if (IS_TEST_MODE) APP_ID_TEST else APP_ID_PROD

    // App Open Ad ID
    private const val APP_OPEN_TEST = "ca-app-pub-3940256099942544/9257395921"
    private const val APP_OPEN_PROD = "ca-app-pub-9489490067134108/9339253176"
    val APP_OPEN_AD_UNIT_ID: String get() = if (IS_TEST_MODE) APP_OPEN_TEST else APP_OPEN_PROD

    // Rewarded Ad ID
    private const val REWARDED_TEST = "ca-app-pub-3940256099942544/5224354917"
    private const val REWARDED_PROD = "ca-app-pub-9489490067134108/5667420297"
    val REWARDED_AD_UNIT_ID: String get() = if (IS_TEST_MODE) REWARDED_TEST else REWARDED_PROD

    // Banner Ad IDs
    private const val BANNER_TEST = "ca-app-pub-3940256099942544/6300978111"

    private const val BANNER_HOME_PROD = "ca-app-pub-9489490067134108/7404385243"
    private const val BANNER_GENERATION_PROD = "ca-app-pub-9489490067134108/3900254505"
    private const val BANNER_MEMORY_PROD = "ca-app-pub-9489490067134108/5548000791"
    private const val BANNER_POKEDEX_PROD = "ca-app-pub-9489490067134108/5851253126"

    val BANNER_HOME_ID: String get() = if (IS_TEST_MODE) BANNER_TEST else BANNER_HOME_PROD
    val BANNER_GENERATION_ID: String get() = if (IS_TEST_MODE) BANNER_TEST else BANNER_GENERATION_PROD
    val BANNER_MEMORY_ID: String get() = if (IS_TEST_MODE) BANNER_TEST else BANNER_MEMORY_PROD
    val BANNER_POKEDEX_ID: String get() = if (IS_TEST_MODE) BANNER_TEST else BANNER_POKEDEX_PROD
}
