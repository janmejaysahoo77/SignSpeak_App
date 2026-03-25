package com.example.signspeak.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.signspeak.CommentBottomSheet
import com.example.signspeak.FeedAdapter
import com.example.signspeak.StoryAdapter
import com.example.signspeak.StoryItem
import com.example.signspeak.SuggestedUser
import com.example.signspeak.SuggestedUserAdapter
import com.example.signspeak.UploadPostActivity
import com.example.signspeak.ViewProfileActivity
import com.example.signspeak.data.PostModel
import com.example.signspeak.databinding.FragmentCommunityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    private lateinit var feedAdapter: FeedAdapter
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var suggestedUserAdapter: SuggestedUserAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupStories()
        setupSuggestedUsers()

        binding.fabAddPost.setOnClickListener {
            startActivity(Intent(requireContext(), UploadPostActivity::class.java))
        }

        fetchPosts()
    }

    private fun setupRecyclerView() {
        feedAdapter = FeedAdapter { post ->
            val bottomSheet = CommentBottomSheet(post.postId)
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = feedAdapter
        binding.rvFeed.itemAnimator = null
    }

    private fun setupStories() {
        storyAdapter = StoryAdapter { story ->
            if (story.isAddStory) {
                startActivity(Intent(requireContext(), UploadPostActivity::class.java))
            }
        }
        binding.rvStories.adapter = storyAdapter
        loadStories()
    }

    private fun setupSuggestedUsers() {
        suggestedUserAdapter = SuggestedUserAdapter(
            onFollowClick = { user, _ ->
                toggleFollow(user)
            },
            onProfileClick = { user ->
                val intent = Intent(requireContext(), ViewProfileActivity::class.java)
                intent.putExtra("userId", user.userId)
                startActivity(intent)
            }
        )
        binding.rvSuggestedUsers.adapter = suggestedUserAdapter
        loadSuggestedUsers()
    }

    private fun toggleFollow(user: SuggestedUser) {
        val currentUserId = auth.currentUser?.uid ?: return
        val followingRef = firestore.collection("users")
            .document(currentUserId).collection("following").document(user.userId)
        val followerRef = firestore.collection("users")
            .document(user.userId).collection("followers").document(currentUserId)

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

    private fun loadSuggestedUsers() {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .limit(20)
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.documents
                    .filter { it.id != currentUserId }
                    .mapNotNull { doc ->
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val profileUrl = doc.getString("profileImageUrl") ?: ""
                        val disability = doc.getString("disability") ?: ""
                        SuggestedUser(
                            userId = doc.id,
                            userName = name,
                            profileImageUrl = profileUrl,
                            disability = disability
                        )
                    }

                if (users.isNotEmpty()) {
                    suggestedUserAdapter.submitList(users)
                    binding.layoutSuggestedUsers.visibility = View.VISIBLE
                } else {
                    binding.layoutSuggestedUsers.visibility = View.GONE
                }
            }
    }

    private fun loadStories() {
        val stories = mutableListOf<StoryItem>()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { doc ->
                    val myName = doc.getString("name") ?: "You"
                    val myPic = doc.getString("profileImageUrl") ?: ""
                    stories.add(0, StoryItem(
                        userId = currentUser.uid,
                        userName = myName,
                        profileUrl = myPic,
                        isAddStory = true
                    ))

                    firestore.collection("posts")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(20)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val seenUsers = mutableSetOf(currentUser.uid)
                            for (postDoc in snapshot.documents) {
                                val post = postDoc.toObject(PostModel::class.java) ?: continue
                                if (post.userId !in seenUsers) {
                                    seenUsers.add(post.userId)
                                    stories.add(StoryItem(
                                        userId = post.userId,
                                        userName = post.userName,
                                        profileUrl = post.userProfile
                                    ))
                                }
                            }
                            storyAdapter.submitList(stories)
                        }
                }
        }
    }

    private fun fetchPosts() {
        binding.progressBar.visibility = View.VISIBLE

        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                binding.progressBar.visibility = View.GONE
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val posts = snapshot.documents.mapNotNull { it.toObject(PostModel::class.java) }
                    feedAdapter.submitList(posts)

                    if (posts.isEmpty()) {
                        binding.tvEmptyFeed.visibility = View.VISIBLE
                    } else {
                        binding.tvEmptyFeed.visibility = View.GONE
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
