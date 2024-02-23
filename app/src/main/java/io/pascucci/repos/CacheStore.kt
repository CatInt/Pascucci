package io.pascucci.repos

import androidx.collection.LruCache
import javax.inject.Inject

@Suppress("unused")
class CacheStore @Inject constructor() {
    private val cache = LruCache<String, LongArray>(32)

    fun getResults(query: String) = cache[query]

    fun setResults(query: String, results: LongArray) {
        cache.put(query, results)
    }
}
