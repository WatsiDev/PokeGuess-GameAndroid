package com.watsidev.pokeguessredux.ui.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun playPokemonCry(pokemonId: Int) {
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
            Log.e("AudioHelper", "Error playing Pokémon cry for ID: $pokemonId", e)
        }
    }

    companion object {
        fun playPokemonCry(context: Context, pokemonId: Int) {
            AudioHelper(context).playPokemonCry(pokemonId)
        }
    }
}
