package com.example.signspeak.Fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.signspeak.R

data class CommunityStory(val name: String, val imageResId: Int)
data class CommunitySuggested(val name: String, val subtitle: String, val imageResId: Int)
data class CommunityOnline(val imageResId: Int)
data class CommunityPost(
    val userName: String,
    val time: String,
    val text: String,
    val likes: String,
    val comments: String,
    val shares: String,
    val userImageResId: Int,
    val postImageResId: Int
)

class CommunityStoryAdapter(private val list: List<CommunityStory>) : RecyclerView.Adapter<CommunityStoryAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivStoryUser: ImageView = view.findViewById(R.id.ivStoryUser)
        val tvStoryName: TextView = view.findViewById(R.id.tvStoryName)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_community_story, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvStoryName.text = item.name
    }
    override fun getItemCount() = list.size
}

class CommunitySuggestedAdapter(private val list: List<CommunitySuggested>) : RecyclerView.Adapter<CommunitySuggestedAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSuggestedName: TextView = view.findViewById(R.id.tvSuggestedName)
        val tvSuggestedSub: TextView = view.findViewById(R.id.tvSuggestedSub)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_community_suggested, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvSuggestedName.text = item.name
        holder.tvSuggestedSub.text = item.subtitle
    }
    override fun getItemCount() = list.size
}

class CommunityOnlineAdapter(private val list: List<CommunityOnline>) : RecyclerView.Adapter<CommunityOnlineAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivOnlineUser: ImageView = view.findViewById(R.id.ivOnlineUser)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_community_online, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {}
    override fun getItemCount() = list.size
}

class CommunityPostAdapter(private val list: List<CommunityPost>) : RecyclerView.Adapter<CommunityPostAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPostName: TextView = view.findViewById(R.id.tvPostName)
        val tvPostTime: TextView = view.findViewById(R.id.tvPostTime)
        val tvPostText: TextView = view.findViewById(R.id.tvPostText)
        val tvLikes: TextView = view.findViewById(R.id.tvLikes)
        val tvComments: TextView = view.findViewById(R.id.tvComments)
        val tvShares: TextView = view.findViewById(R.id.tvShares)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_community_post, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvPostName.text = item.userName
        holder.tvPostTime.text = item.time
        holder.tvPostText.text = item.text
        holder.tvLikes.text = item.likes
        holder.tvComments.text = item.comments
        holder.tvShares.text = item.shares
    }
    override fun getItemCount() = list.size
}
