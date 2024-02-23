package io.pascucci.ui.search

import androidx.lifecycle.ViewModel
import io.pascucci.data.Location

class SearchResultItemViewModel (
    val location: Location
) : ViewModel() {
    val id: String
        get() = location.id
    val name
        get() = location.name
//    val link
//        get() = location.link
//    val image
//        get() = location.image
}