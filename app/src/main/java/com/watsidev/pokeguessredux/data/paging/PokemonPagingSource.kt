package com.watsidev.pokeguessredux.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.watsidev.pokeguessredux.data.remote.PokeApiService

class PokemonPagingSource(
    private val apiService: PokeApiService,
    private val maxPokemonCount: Int = 1025
) : PagingSource<Int, String>() {

    override fun getRefreshKey(state: PagingState<Int, String>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(20)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(20)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> {
        val offset = params.key ?: 0

        if (offset >= maxPokemonCount) {
            return LoadResult.Page(
                data = emptyList(),
                prevKey = if (offset == 0) null else offset - params.loadSize,
                nextKey = null
            )
        }

        val limit = minOf(params.loadSize, maxPokemonCount - offset)

        return try {
            val response = apiService.getPokemonList(limit = limit, offset = offset)
            val names = response.results.take(limit).map { it.name }
            
            val nextOffset = offset + names.size

            LoadResult.Page(
                data = names,
                prevKey = if (offset == 0) null else offset - limit,
                nextKey = if (nextOffset >= maxPokemonCount || names.isEmpty()) null else nextOffset
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
