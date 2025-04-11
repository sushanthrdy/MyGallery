package com.sushanth.mygallery.ui.album_detail

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.sushanth.mygallery.R
import com.sushanth.mygallery.databinding.FragmentAlbumDetailBinding
import com.sushanth.mygallery.utils.GridSpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlbumDetailFragment : Fragment() {

    private val viewModel: AlbumDetailViewModel by viewModels()
    private lateinit var binding: FragmentAlbumDetailBinding
    private lateinit var mediaAdapter: AlbumDetailAdapter
    private val args: AlbumDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentAlbumDetailBinding.inflate(inflater, container, false)
        binding.mediaHeader.text = args.bucketName
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setOnClickListeners()
        setupAdapter()
        setupRecyclerView()
        observeMedia()
    }

    private fun setOnClickListeners() {
        binding.backArrow.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupAdapter() {
        mediaAdapter = AlbumDetailAdapter { media ->
        }
    }

    private fun setupRecyclerView() {
        binding.mediaRv.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
            addItemDecoration(GridSpacingItemDecoration(3, spacingInPixels, true))
            adapter = mediaAdapter
            setHasFixedSize(true)
            setItemViewCacheSize(40)
            itemAnimator = null
        }
    }

    private fun observeMedia() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getPagedMedia(bucketName = args.bucketName).collectLatest { pagingData ->
                mediaAdapter.submitData(pagingData)
            }
        }
    }
}