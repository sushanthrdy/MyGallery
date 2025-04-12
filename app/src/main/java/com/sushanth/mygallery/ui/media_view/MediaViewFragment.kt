package com.sushanth.mygallery.ui.media_view

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import com.sushanth.mygallery.databinding.FragmentMediaViewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaViewFragment : Fragment() {

    private var _binding: FragmentMediaViewBinding? = null
    private val binding get() = _binding!!

    private val args: MediaViewFragmentArgs by navArgs()
    private val viewModel: MediaViewViewModel by viewModels()

    private lateinit var mediaUri: Uri
    private var isVideo: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaUri = Uri.parse(args.mediaUri)
        isVideo = args.isVideo

        binding.progressBar.isVisible = true
        binding.closeButton.setOnClickListener {
            findNavController().popBackStack()
        }

        if (isVideo) {
            setupVideo()
        } else {
            setupImage()
        }
    }

    private fun setupVideo() {
        binding.playerView.isVisible = true
        binding.photoView.isVisible = false

        // Only initialize if not already initialized
        if (viewModel.player.value == null) {
            viewModel.initializePlayer(requireContext(), mediaUri)
        }

        viewModel.player.observe(viewLifecycleOwner) { player ->
            binding.playerView.player = player
            binding.progressBar.isVisible = false
        }
    }

    private fun setupImage() {
        binding.playerView.isVisible = false
        binding.photoView.isVisible = true
        binding.progressBar.isVisible = true

        Glide.with(requireContext())
            .load(mediaUri)
            .listener(object :
                com.bumptech.glide.request.RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.isVisible = false
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.isVisible = false
                    return false
                }
            })
            .into(binding.photoView)
    }

    override fun onStart() {
        super.onStart()
        if (isVideo) {
            viewModel.play()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isVideo) {
            viewModel.play()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isVideo) {
            viewModel.pause()
            viewModel.savePlayerState()
        }
    }

    override fun onDestroyView() {
        binding.playerView.player = null
        _binding = null
        super.onDestroyView()
    }
}
