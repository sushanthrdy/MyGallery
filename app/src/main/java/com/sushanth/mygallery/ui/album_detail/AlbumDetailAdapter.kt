package com.sushanth.mygallery.ui.album_detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sushanth.mygallery.data.model.Media
import com.sushanth.mygallery.databinding.MediaItemBinding
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.sushanth.mygallery.utils.Utils.formatDuration

class AlbumDetailAdapter(private val onItemClick: (Media) -> Unit) :
    PagingDataAdapter<Media, AlbumDetailAdapter.MediaViewHolder>(MediaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = MediaItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val media = getItem(position)
        if (media != null) {
            holder.bind(media, onItemClick)
        }
    }

    inner class MediaViewHolder(private val binding: MediaItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(media: Media, onItemClick: (Media) -> Unit) {
            binding.media = media

            if (media.isVideo) {
                binding.videoDurationTv.text = formatDuration(media.videoDuration)
            }

            itemView.setOnClickListener {
                onItemClick(media)
            }
        }
    }

    class MediaDiffCallback : DiffUtil.ItemCallback<Media>() {
        override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean =
            oldItem == newItem
    }
}
