package com.sushanth.mygallery.ui.album_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.sushanth.mygallery.R
import com.sushanth.mygallery.databinding.FragmentAlbumListBinding
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
        binding =  FragmentAlbumListBinding.inflate(inflater, container, false)
        binding.albumsRv.layoutManager = GridLayoutManager(requireContext(), 3)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        binding.albumsRv.addItemDecoration(GridSpacingItemDecoration(3, spacingInPixels, true))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel

        viewModel.albums.observe(viewLifecycleOwner) { albumList ->
            albumAdapter = AlbumListAdapter(albumList) { clickedAlbum ->

            }
            binding.albumsRv.adapter = albumAdapter
        }
    }
}