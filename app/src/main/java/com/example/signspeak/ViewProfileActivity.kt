package com.example.signspeak

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.signspeak.data.PostModel
import com.example.signspeak.databinding.ActivityViewProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewProfileBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var postGridAdapter: PostGridAdapter
    private var targetUserId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        targetUserId = intent.getStringExtra("userId") ?: ""
        if (targetUserId.isEmpty()) {
            finish()
            return
        }

        setupPostsGrid()
        loadUserProfile()
        loadFollowerCounts()
        loadUserPosts()
        setupFollowButton()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupPostsGrid() {
        postGridAdapter = PostGridAdapter()
        binding.rvPostsGrid.layoutManager = GridLayoutManager(this, 3)
        binding.rvPostsGrid.adapter = postGridAdapter
    }

    private fun loadUserProfile() {
        firestore.collection("users").document(targetUserId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: "User"
                    val disability = doc.getString("disability") ?: ""
                    val profileUrl = doc.getString("profileImageUrl") ?: ""

                    binding.tvUsername.text = name
                    binding.tvToolbarTitle.text = name
                    binding.tvDisability.text = if (disability.isNotEmpty()) disability else "Not specified"

                    if (profileUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(profileUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_person_placeholder)
                            .into(binding.ivProfileImage)
                    }
                }
            }
    }

    private fun loadFollowerCounts() {
        // Followers count
        firestore.collection("users").document(targetUserId)
            .collection("followers")
            .addSnapshotListener { snapshot, _ ->
                binding.tvFollowersCount.text = (snapshot?.size() ?: 0).toString()
            }

        // Following count
        firestore.collection("users").document(targetUserId)
            .collection("following")
            .addSnapshotListener { snapshot, _ ->
                binding.tvFollowingCount.text = (snapshot?.size() ?: 0).toString()
            }
    }

    private fun loadUserPosts() {
        firestore.collection("posts")
            .whereEqualTo("userId", targetUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val posts = snapshot.documents.mapNotNull { it.toObject(PostModel::class.java) }
                    binding.tvPostCount.text = posts.size.toString()

                    val imageUrls = posts.filter { it.imageUrl.isNotEmpty() }.map { it.imageUrl }
                    if (imageUrls.isNotEmpty()) {
                        postGridAdapter.submitList(imageUrls)
                        binding.tvNoPosts.visibility = View.GONE
                    } else {
                        postGridAdapter.submitList(emptyList())
                        binding.tvNoPosts.visibility = View.VISIBLE
                    }
                }
            }
    }

    private fun setupFollowButton() {
        val currentUserId = auth.currentUser?.uid ?: return

        // Hide follow button on own profile
        if (currentUserId == targetUserId) {
            binding.btnFollowProfile.visibility = View.GONE
            return
        }

        val followingRef = firestore.collection("users")
            .document(currentUserId).collection("following").document(targetUserId)
        val followerRef = firestore.collection("users")
            .document(targetUserId).collection("followers").document(currentUserId)

        // Listen for follow state
        followingRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                binding.btnFollowProfile.text = "Following"
                binding.btnFollowProfile.setTextColor(android.graphics.Color.parseColor("#d3bcfc"))
                binding.btnFollowProfile.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1e1b36"))
            } else {
                binding.btnFollowProfile.text = "Follow"
                binding.btnFollowProfile.setTextColor(android.graphics.Color.parseColor("#131125"))
                binding.btnFollowProfile.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#d3bcfc"))
            }
        }

        binding.btnFollowProfile.setOnClickListener {
            followingRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
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
