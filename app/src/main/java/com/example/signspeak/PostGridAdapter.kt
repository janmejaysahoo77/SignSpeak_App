package com.example.signspeak

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.signspeak.databinding.ItemPostGridBinding

class PostGridAdapter(
    private val posts: MutableList<String> = mutableListOf() // list of imageUrls
) : RecyclerView.Adapter<PostGridAdapter.GridViewHolder>() {

    fun submitList(list: List<String>) {
        posts.clear()
        posts.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val binding = ItemPostGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Make items square
        val size = parent.measuredWidth / 3
        binding.root.layoutParams = ViewGroup.LayoutParams(size, size)
        return GridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount() = posts.size

    inner class GridViewHolder(private val binding: ItemPostGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imageUrl: String) {
            Glide.with(itemView.context)
                .load(imageUrl)
                .centerCrop()
                .placeholder(android.R.color.darker_gray)
                .into(binding.ivGridPost)
        }
    }
}
