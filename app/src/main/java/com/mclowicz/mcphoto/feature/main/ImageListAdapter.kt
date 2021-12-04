package com.mclowicz.mcphoto.feature.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.mclowicz.mcphoto.Image
import com.mclowicz.mcphoto.databinding.ItemImageBinding

class ImageListAdapter(
    val glide: RequestManager
) :
    ListAdapter<Image, ImageListAdapter.ImageViewHolder>(ImageComparator()) {

    lateinit var onItemClickListener: (Image) -> Unit

    class ImageComparator : DiffUtil.ItemCallback<Image>() {
        override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean =
            oldItem.id == newItem.id
    }

    class ImageViewHolder(
        private val binding: ItemImageBinding,
        val listener: (Image) -> Unit,
        val glide: RequestManager
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(image: Image) {
            binding.apply {
                glide.load(image.uri).into(itemImage)
                root.setOnClickListener {
                    listener.invoke(image)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding, onItemClickListener, glide)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }
}