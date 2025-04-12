package com.sushanth.mygallery.ui.album_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sushanth.mygallery.data.model.Album
import com.sushanth.mygallery.databinding.AlbumItemBinding
import com.sushanth.mygallery.databinding.AlbumListItemBinding

class AlbumListAdapter(
    private var albums: List<Album>,
    private var isGrid: Boolean = true,
    private val onItemClick: (Album) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val GRID_VIEW_TYPE = 0
        private const val LIST_VIEW_TYPE = 1
    }

    fun updateLayout(isGrid: Boolean) {
        this.isGrid = isGrid
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            GRID_VIEW_TYPE -> {
                val binding = AlbumItemBinding.inflate(inflater, parent, false)
                GridViewHolder(binding)
            }

            LIST_VIEW_TYPE -> {
                val binding = AlbumListItemBinding.inflate(inflater, parent, false)
                ListViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val album = albums[position]
        when (holder) {
            is GridViewHolder -> {
                holder.bind(album)
            }

            is ListViewHolder -> {
                holder.bind(album)
            }
        }
    }

    override fun getItemCount(): Int = albums.size

    override fun getItemViewType(position: Int): Int {
        return if (isGrid) GRID_VIEW_TYPE else LIST_VIEW_TYPE
    }

    inner class GridViewHolder(private val binding: AlbumItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(album: Album) {
            binding.album = album
            binding.root.setOnClickListener { onItemClick(album) }
        }
    }

    inner class ListViewHolder(private val binding: AlbumListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(album: Album) {
            binding.album = album
            binding.root.setOnClickListener { onItemClick(album) }
        }
    }
}