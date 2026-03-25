package com.example.signspeak

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.signspeak.data.CommentModel
import com.example.signspeak.databinding.BottomSheetCommentBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID

class CommentBottomSheet(private val postId: String) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCommentBinding? = null
    private val binding get() = _binding!!

    private lateinit var commentAdapter: CommentAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commentAdapter = CommentAdapter()
        binding.rvComments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComments.adapter = commentAdapter

        fetchComments()

        binding.btnSendComment.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            if (text.isNotEmpty()) {
                postComment(text)
            }
        }
    }

    private fun fetchComments() {
        firestore.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { it.toObject(CommentModel::class.java) }
                    commentAdapter.submitList(comments)
                    
                    if (comments.isEmpty()) {
                        binding.tvEmptyComments.visibility = View.VISIBLE
                    } else {
                        binding.tvEmptyComments.visibility = View.GONE
                        binding.rvComments.scrollToPosition(comments.size - 1)
                    }
                }
            }
    }

    private fun postComment(text: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please log in", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.btnSendComment.isEnabled = false

        firestore.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                val userName = document.getString("name") ?: "User"
                
                val comment = CommentModel(
                    userId = currentUser.uid,
                    userName = userName,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )
                
                firestore.collection("posts").document(postId).collection("comments")
                    .document(UUID.randomUUID().toString())
                    .set(comment)
                    .addOnSuccessListener {
                        binding.etComment.text?.clear()
                        binding.btnSendComment.isEnabled = true
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        binding.btnSendComment.isEnabled = true
                    }
            }
            .addOnFailureListener {
                binding.btnSendComment.isEnabled = true
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
