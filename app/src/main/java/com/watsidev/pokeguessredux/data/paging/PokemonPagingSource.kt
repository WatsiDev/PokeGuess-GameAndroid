package com.watsidev.pokeguessredux.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.watsidev.pokeguessredux.data.remote.PokeApiService

class PokemonPagingSource(
    private val apiService: PokeApiService
) : PagingSource<Int, String>() {

    override fun getRefreshKey(state: PagingState<Int, String>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(20)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(20)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> {
        val offset = params.key ?: 0
        val limit = params.loadSize

        return try {
            val response = apiService.getPokemonList(limit = limit, offset = offset)
            val names = response.results.map { it.name }
            
            LoadResult.Page(
                data = names,
                prevKey = if (offset == 0) null else offset - limit,
                nextKey = if (response.results.isEmpty()) null else offset + limit
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
