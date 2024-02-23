/*
 * *******************************************************************************
 *  ** Copyright (C), 2014-2021, OnePlus Mobile Comm Corp., Ltd
 *  ** All rights reserved.
 *  *******************************************************************************
 */

package io.pascucci.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.pascucci.R
import io.pascucci.data.Location
import io.pascucci.databinding.SearchReultItemBinding

class SearchResultListAdapter(private val onClick: (Location) -> Unit) :
    ListAdapter<Location, SearchResultViewHolder>(ChannelDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder =
        SearchResultViewHolder.create(parent, onClick)

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class SearchResultViewHolder(
    private val binding: SearchReultItemBinding,
    onClick: (Location) -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {

    init {
//        binding.setClickListener { view ->
//            binding.viewModel?.let {
//                navigateToLocation(it.location, view)
//            }
//        }
        binding.setClickListener {
            onClick(binding.viewModel!!.location)
        }
    }

    fun bind(channel: Location?) {
        with(binding) {
            viewModel = SearchResultItemViewModel(channel!!)
//            Glide.with(image)
//                .load(channel.image)
//                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//                .transition(CrossFade)
//                .into(image)
        }
    }

    companion object {
        fun create(parent: ViewGroup, onClick: (Location) -> Unit): SearchResultViewHolder {
            val binding = DataBindingUtil.inflate<SearchReultItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.search_reult_item,
                parent,
                false
            )
            return SearchResultViewHolder(binding, onClick)
        }
    }
}

private class ChannelDiffCallback :
    DiffUtil.ItemCallback<Location>() {
    override fun areItemsTheSame(oldItem: Location, newItem: Location): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Location, newItem: Location): Boolean {
        return oldItem == newItem
    }
}