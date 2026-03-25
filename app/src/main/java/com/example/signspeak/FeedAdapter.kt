package com.example.signspeak

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.signspeak.data.PostModel
import com.example.signspeak.databinding.ItemPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedAdapter(
    private val onCommentClick: (PostModel) -> Unit
) : ListAdapter<PostModel, FeedAdapter.PostViewHolder>(PostDiffCallback()) {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: PostModel) {
            binding.tvUserName.text = post.userName
            binding.tvCaption.text = post.caption
            binding.tvLikeCount.text = post.likeCount.toString()

            // Timestamp
            val timeAgo = getTimeAgo(post.timestamp)
            binding.tvPostTime.text = timeAgo

            if (post.caption.isNotEmpty()) {
                binding.tvCaption.visibility = View.VISIBLE
            } else {
                binding.tvCaption.visibility = View.GONE
            }

            if (post.imageUrl.isNotEmpty()) {
                binding.cardPostImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(post.imageUrl)
                    .into(binding.ivPostImage)
            } else {
                binding.cardPostImage.visibility = View.GONE
            }

            // Profile photo with circleCrop - fetch from user doc if post field is empty
            if (post.userProfile.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(post.userProfile)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .into(binding.ivUserProfile)
            } else {
                // Fallback: fetch profile photo from user's Firestore document
                FirebaseFirestore.getInstance().collection("users")
                    .document(post.userId).get()
                    .addOnSuccessListener { userDoc ->
                        val profileUrl = userDoc.getString("profileImageUrl") ?: ""
                        if (profileUrl.isNotEmpty()) {
                            Glide.with(itemView.context)
                                .load(profileUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_person_placeholder)
                                .error(R.drawable.ic_person_placeholder)
                                .into(binding.ivUserProfile)
                        } else {
                            binding.ivUserProfile.setImageResource(R.drawable.ic_person_placeholder)
                        }
                    }
            }

            // Check if current user liked the post
            currentUserId?.let { uid ->
                FirebaseFirestore.getInstance().collection("posts")
                    .document(post.postId).collection("likes").document(uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null && snapshot.exists()) {
                            binding.ivLikeIcon.setImageResource(R.drawable.ic_heart_filled)
                        } else {
                            binding.ivLikeIcon.setImageResource(R.drawable.ic_heart_outline)
                        }
                    }
            }

            binding.btnLike.setOnClickListener {
                currentUserId?.let { uid ->
                    val likeRef = FirebaseFirestore.getInstance().collection("posts")
                        .document(post.postId).collection("likes").document(uid)
                        
                    likeRef.get().addOnSuccessListener { document ->
                        val postRef = FirebaseFirestore.getInstance().collection("posts").document(post.postId)
                        if (document.exists()) {
                            // Unlike
                            likeRef.delete()
                            FirebaseFirestore.getInstance().runTransaction { transaction ->
                                val snapshot = transaction.get(postRef)
                                val currentLikes = snapshot.getLong("likeCount") ?: 0
                                if (currentLikes > 0) {
                                    transaction.update(postRef, "likeCount", currentLikes - 1)
                                }
                            }
                        } else {
                            // Like
                            likeRef.set(mapOf("liked" to true))
                            FirebaseFirestore.getInstance().runTransaction { transaction ->
                                val snapshot = transaction.get(postRef)
                                val currentLikes = snapshot.getLong("likeCount") ?: 0
                                transaction.update(postRef, "likeCount", currentLikes + 1)
                            }
                        }
                    }
                }
            }

            // Check follow status
            currentUserId?.let { uid ->
                if (uid == post.userId) {
                    binding.btnFollow.visibility = View.GONE
                } else {
                    binding.btnFollow.visibility = View.VISIBLE
                    FirebaseFirestore.getInstance().collection("users")
                        .document(uid).collection("following").document(post.userId)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) return@addSnapshotListener
                            if (snapshot != null && snapshot.exists()) {
                                binding.btnFollow.text = "Following"
                                binding.btnFollow.backgroundTintList = android.content.res.ColorStateList.valueOf(
                                    android.graphics.Color.parseColor("#1e1b36")
                                )
                            } else {
                                binding.btnFollow.text = "Follow"
                                binding.btnFollow.backgroundTintList = android.content.res.ColorStateList.valueOf(
                                    android.graphics.Color.parseColor("#2a244d")
                                )
                            }
                        }

                    binding.btnFollow.setOnClickListener {
                        val followingRef = FirebaseFirestore.getInstance().collection("users")
                            .document(uid).collection("following").document(post.userId)
                        val followerRef = FirebaseFirestore.getInstance().collection("users")
                            .document(post.userId).collection("followers").document(uid)
                            
                        followingRef.get().addOnSuccessListener { document ->
                            if (document.exists()) {
                                followingRef.delete()
                                followerRef.delete()
                            } else {
                                followingRef.set(mapOf("followed" to true))
                                followerRef.set(mapOf("followed" to true))
                            }
                        }
                    }
                }
            }
            
            binding.btnComment.setOnClickListener {
                onCommentClick(post)
            }

            // Share button
            binding.btnShare.setOnClickListener {
                val shareText = buildString {
                    append("Check out this post by ${post.userName}")
                    if (post.caption.isNotEmpty()) {
                        append(": ${post.caption}")
                    }
                    if (post.imageUrl.isNotEmpty()) {
                        append("\n${post.imageUrl}")
                    }
                }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                itemView.context.startActivity(Intent.createChooser(shareIntent, "Share post via"))
            }

            // Load comment count
            FirebaseFirestore.getInstance().collection("posts")
                .document(post.postId).collection("comments")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        binding.tvCommentCount.text = snapshot.size().toString()
                    }
                }
        }

        private fun getTimeAgo(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            return when {
                seconds < 60 -> "Just now"
                minutes < 60 -> "${minutes}m ago"
                hours < 24 -> "${hours}h ago"
                days < 7 -> "${days}d ago"
                else -> "${days / 7}w ago"
            }
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<PostModel>() {
        override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
            return oldItem == newItem
        }
    }
}
