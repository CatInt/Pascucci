package io.pascucci.ui.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import dagger.hilt.android.AndroidEntryPoint
import io.pascucci.R
import io.pascucci.databinding.FragmentSearchBinding
import io.pascucci.ui.dashboard.SlidingPanelStateHelper
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment(), View.OnFocusChangeListener {

    @Inject
    lateinit var slidingPanelStateHelper: SlidingPanelStateHelper

    private lateinit var binding: FragmentSearchBinding
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return DataBindingUtil.inflate<FragmentSearchBinding>(
            inflater, R.layout.fragment_search, container, false
        ).let {
            binding = it
            binding.viewModel = viewModel
            binding.lifecycleOwner = viewLifecycleOwner
            binding.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchView.setOnQueryTextListener(createOnQueryListener(viewModel, binding))
        binding.searchView.setOnQueryTextFocusChangeListener(this)

        binding.searchResultView.run {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = viewModel.listAdapter
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { locations ->
            viewModel.listAdapter.submitList(locations)
            binding.searchResultView.visibility = View.VISIBLE
            binding.routeTypeSelection.visibility = View.INVISIBLE
        }

        viewModel.vehicleType.observe(viewLifecycleOwner) { _ ->
            binding.searchResultView.visibility = View.INVISIBLE
            binding.routeTypeSelection.visibility = View.VISIBLE
        }
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        val state = when {
            hasFocus -> SlidingUpPanelLayout.PanelState.ANCHORED
            else -> SlidingUpPanelLayout.PanelState.COLLAPSED
        }
        slidingPanelStateHelper.slidingUpPanelLayout?.panelState = state
    }

    companion object {
        private fun createOnQueryListener(
            viewModel: SearchViewModel,
            binding: FragmentSearchBinding
        ) = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Timber.d("Submit $query")
                query?.let { viewModel.search(it) }
                with(binding.searchView) {
                    clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Do nothing
                return true
            }
        }
    }
}