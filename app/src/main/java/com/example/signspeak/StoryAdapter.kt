package com.example.signspeak

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.signspeak.databinding.ItemStoryBinding

data class StoryItem(
    val userId: String = "",
    val userName: String = "",
    val profileUrl: String = "",
    val isAddStory: Boolean = false
)

class StoryAdapter(
    private val stories: MutableList<StoryItem> = mutableListOf(),
    private val onStoryClick: (StoryItem) -> Unit
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    fun submitList(list: List<StoryItem>) {
        stories.clear()
        stories.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(stories[position])
    }

    override fun getItemCount() = stories.size

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(story: StoryItem) {
            binding.tvStoryName.text = if (story.isAddStory) "Your Story" else story.userName

            if (story.isAddStory) {
                binding.ivAddBadge.visibility = View.VISIBLE
            } else {
                binding.ivAddBadge.visibility = View.GONE
            }

            if (story.profileUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(story.profileUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person_placeholder)
                    .into(binding.ivStoryProfile)
            } else {
                binding.ivStoryProfile.setImageResource(R.drawable.ic_person_placeholder)
            }

            itemView.setOnClickListener { onStoryClick(story) }
        }
    }
}
