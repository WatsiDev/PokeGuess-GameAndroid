package com.watsidev.pokeguessredux.ui.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri

object AudioHelper {
    fun playPokemonCry(context: Context, pokemonId: Int) {
        try {
            val cryUrl = "https://raw.githubusercontent.com/PokeAPI/cries/main/cries/pokemon/latest/$pokemonId.ogg"
            val mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, Uri.parse(cryUrl))
                prepareAsync()
                setOnPreparedListener { mp ->
                    mp.start()
                }
                setOnCompletionListener { mp ->
                    mp.release()
                }
                setOnErrorListener { mp, _, _ ->
                    mp.release()
                    true
                }
            }
        } catch (e: Exception) {
            // Ignore cry play errors
        }
    }
}
