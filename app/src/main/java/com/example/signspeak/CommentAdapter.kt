package com.example.signspeak

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.signspeak.data.CommentModel
import com.example.signspeak.databinding.ItemCommentBinding

class CommentAdapter : ListAdapter<CommentModel, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: CommentModel) {
            binding.tvCommentName.text = comment.userName
            binding.tvCommentText.text = comment.text
            
            // Assume we can get user profile from another source or update model later.
            // For now use default or if you add userProfile to CommentModel, use it.
            binding.ivCommentUser.setImageResource(R.mipmap.ic_launcher)
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<CommentModel>() {
        override fun areItemsTheSame(oldItem: CommentModel, newItem: CommentModel): Boolean {
            return oldItem.timestamp == newItem.timestamp && oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: CommentModel, newItem: CommentModel): Boolean {
            return oldItem == newItem
        }
    }
}
