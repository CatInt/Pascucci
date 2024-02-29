package io.pascucci.repos.search

import androidx.lifecycle.LiveData
import io.pascucci.data.Location

interface ISearchRepository {
    val destinationsObservable : LiveData<List<Location>>
    suspend fun search(query: String)
}
