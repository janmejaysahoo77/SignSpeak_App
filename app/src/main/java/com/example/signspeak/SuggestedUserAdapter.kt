package com.example.signspeak

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.signspeak.databinding.ItemSuggestedUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class SuggestedUser(
    val userId: String = "",
    val userName: String = "",
    val profileImageUrl: String = "",
    val disability: String = ""
)

class SuggestedUserAdapter(
    private val users: MutableList<SuggestedUser> = mutableListOf(),
    private val onFollowClick: (SuggestedUser, Int) -> Unit,
    private val onProfileClick: (SuggestedUser) -> Unit
) : RecyclerView.Adapter<SuggestedUserAdapter.ViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val followStates = mutableMapOf<String, Boolean>()

    fun submitList(list: List<SuggestedUser>) {
        users.clear()
        users.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSuggestedUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    inner class ViewHolder(private val binding: ItemSuggestedUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: SuggestedUser) {
            binding.tvSuggestedName.text = user.userName

            if (user.profileImageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(user.profileImageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .into(binding.ivSuggestedProfile)
            } else {
                binding.ivSuggestedProfile.setImageResource(R.drawable.ic_person_placeholder)
            }

            // Check follow state
            currentUserId?.let { uid ->
                FirebaseFirestore.getInstance().collection("users")
                    .document(uid).collection("following").document(user.userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        val isFollowing = snapshot != null && snapshot.exists()
                        followStates[user.userId] = isFollowing
                        updateFollowButton(isFollowing)
                    }
            }

            binding.btnSuggestedFollow.setOnClickListener {
                onFollowClick(user, adapterPosition)
            }

            // Profile click - image or name
            binding.ivSuggestedProfile.setOnClickListener {
                onProfileClick(user)
            }
            binding.tvSuggestedName.setOnClickListener {
                onProfileClick(user)
            }
        }

        private fun updateFollowButton(isFollowing: Boolean) {
            if (isFollowing) {
                binding.btnSuggestedFollow.text = "Following"
                binding.btnSuggestedFollow.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1e1b36"))
            } else {
                binding.btnSuggestedFollow.text = "Follow"
                binding.btnSuggestedFollow.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2a244d"))
            }
        }
    }
}
