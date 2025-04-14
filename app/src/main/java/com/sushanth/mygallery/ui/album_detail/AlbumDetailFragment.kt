package com.sushanth.mygallery.ui.album_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sushanth.mygallery.R
import com.sushanth.mygallery.databinding.FragmentAlbumDetailBinding
import com.sushanth.mygallery.utils.DialogUtils
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
        binding = FragmentAlbumDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.mediaHeader.text = args.bucketName
        setMediaCountSubHeader()
        setOnClickListeners()
        setupAdapter()
        setupRecyclerView()
        viewModel.loadMedia(args.bucketName)
        mediaAdapter.addLoadStateListener { loadState ->
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
                ?: loadState.refresh as? LoadState.Error
            errorState?.let {
                DialogUtils.showAlertDialog(
                    requireContext(),
                    title = getString(R.string.app_name),
                    message = "Error loading data",
                    positiveButtonText = "Ok",
                    isCancelable = false,
                    positiveButtonAction = { findNavController().popBackStack() })
            }
        }
        observeMedia()
    }

    private fun setMediaCountSubHeader(
        imageCount: Int = args.imageCount,
        videoCount: Int = args.videoCount
    ) {
        val context = requireContext()
        val imageText = if (imageCount > 0) {
            context.resources.getQuantityString(R.plurals.image_count, imageCount, imageCount)
        } else null

        val videoText = if (videoCount > 0) {
            context.resources.getQuantityString(R.plurals.video_count, videoCount, videoCount)
        } else null

        val mediaCount = listOfNotNull(imageText, videoText).joinToString(" ")

        binding.mediaCountTv.text = mediaCount
    }

    private fun setOnClickListeners() {
        binding.backArrow.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupAdapter() {
        mediaAdapter = AlbumDetailAdapter { media ->
            Glide.with(requireContext()).load(media.contentUri).preload()
            val action = AlbumDetailFragmentDirections.actionAlbumDetailFragmentToMediaViewFragment(
                media.contentUri.toString(),
                media.isVideo
            )
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {
        binding.mediaRv.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing_small)
            addItemDecoration(GridSpacingItemDecoration(3, spacingInPixels, true))
            adapter = mediaAdapter
            setHasFixedSize(true)
            setItemViewCacheSize(40)
            itemAnimator = null
        }

        binding.mediaRv.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 10 && !binding.fabScrollToTop.isVisible) {
                    binding.fabScrollToTop.show()
                }
                if (!recyclerView.canScrollVertically(-1)) {
                    binding.fabScrollToTop.hide()
                }
            }
        })
        binding.fabScrollToTop.setOnClickListener {
            binding.mediaRv.smoothScrollToPosition(0)
        }
    }

    private fun observeMedia() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mediaFlow?.collectLatest { pagingData ->
                    mediaAdapter.submitData(pagingData)
                }
            }
        }
    }
}