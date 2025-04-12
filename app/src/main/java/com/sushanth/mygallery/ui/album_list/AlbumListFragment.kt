package com.sushanth.mygallery.ui.album_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.sushanth.mygallery.R
import com.sushanth.mygallery.databinding.FragmentAlbumListBinding
import com.sushanth.mygallery.ui.UIState
import com.sushanth.mygallery.utils.GridSpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumListFragment : Fragment() {

    private val viewModel: AlbumListViewModel by viewModels()
    private lateinit var binding: FragmentAlbumListBinding
    private lateinit var albumAdapter: AlbumListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlbumListBinding.inflate(inflater, container, false)
        setupRecyclerView(viewModel.isGrid)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the toggle button click listener
        binding.toggleLayoutButton.setOnClickListener {
            if (
                binding.albumsRv.adapter!=null
            ) {
                viewModel.isGrid = !viewModel.isGrid // Toggle the layout state
                setupRecyclerView(viewModel.isGrid) // Update the layout
                albumAdapter.updateLayout(viewModel.isGrid)
                updateToggleButtonIcon(viewModel.isGrid) // Update the icon
            }
        }

        viewModel.albumUIState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UIState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.noMediaTv.isVisible = false
                    binding.albumsRv.isVisible = false
                    binding.albumsRv.adapter = null
                }

                is UIState.Success -> {
                    binding.progressBar.isVisible = false
                    binding.albumsRv.isVisible = state.data.isNotEmpty()
                    binding.noMediaTv.isVisible = state.data.isEmpty()
                    if (state.data.isEmpty()) {
                        binding.albumsRv.adapter = null
                    } else {

                        albumAdapter =
                            AlbumListAdapter(state.data, viewModel.isGrid) { clickedAlbum ->
                                val action =
                                    AlbumListFragmentDirections.actionAlbumListFragmentToAlbumDetailFragment(
                                        clickedAlbum.name,
                                        clickedAlbum.videoCount,
                                        clickedAlbum.imageCount
                                    )
                                findNavController().navigate(action)
                            }
                        binding.albumsRv.adapter = albumAdapter
                    }
                }
            }
        }
    }

    private fun setupRecyclerView(isGrid: Boolean) {
        binding.albumsRv.apply {
            layoutManager = if (isGrid) {
                GridLayoutManager(requireContext(), 3)
            } else {
                LinearLayoutManager(requireContext())
            }
            if (isGrid) {
                val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
                addItemDecoration(GridSpacingItemDecoration(3, spacingInPixels, true))
            } else {
                // Remove any existing item decorations if switching to linear layout
                while (itemDecorationCount > 0) {
                    removeItemDecorationAt(0)
                }
            }
            setHasFixedSize(true)
        }
    }

    private fun updateToggleButtonIcon(isGrid: Boolean) {
        binding.toggleLayoutButton.setImageResource(if (isGrid) R.drawable.baseline_view_list_24 else R.drawable.baseline_grid_view_24)
    }
}