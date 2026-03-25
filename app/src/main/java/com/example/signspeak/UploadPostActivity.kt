package com.example.signspeak

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.signspeak.data.PostModel
import com.example.signspeak.databinding.ActivityUploadPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class UploadPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadPostBinding
    private var selectedImageUri: Uri? = null
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Cloudinary setup
    private val cloudinaryConfig = mapOf(
        "cloud_name" to "dzubjgb9y",
        "api_key" to "736625827197944",
        "api_secret" to "9WL3awjjHgyM0uPzPHsV8gzVSgE"
    )
    private val cloudinary = Cloudinary(cloudinaryConfig)

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivPostImage.setImageURI(uri)
            binding.ivPostImage.visibility = View.VISIBLE
            binding.btnRemoveImage.visibility = View.VISIBLE
            binding.cardImagePreview.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserInfo()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnSelectImage.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        binding.btnRemoveImage.setOnClickListener {
            selectedImageUri = null
            binding.ivPostImage.setImageURI(null)
            binding.ivPostImage.visibility = View.GONE
            binding.btnRemoveImage.visibility = View.GONE
            binding.cardImagePreview.visibility = View.GONE
        }

        binding.btnPost.setOnClickListener {
            uploadPost()
        }
    }

    private fun loadUserInfo() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "User"
                        val profilePic = document.getString("profileImageUrl") ?: ""
                        binding.tvUserName.text = name
                        if (profilePic.isNotEmpty()) {
                            com.bumptech.glide.Glide.with(this)
                                .load(profilePic)
                                .placeholder(R.mipmap.ic_launcher)
                                .into(binding.ivUserProfile)
                        }
                    }
                }
        }
    }

    private fun uploadPost() {
        val caption = binding.etCaption.text.toString().trim()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            return
        }

        if (caption.isEmpty() && selectedImageUri == null) {
            Toast.makeText(this, "Please add an image or write a caption", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressOverlay.visibility = View.VISIBLE
        binding.btnPost.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                var imageUrl = ""
                selectedImageUri?.let { uri ->
                    val inputStream = contentResolver.openInputStream(uri)
                    val result = cloudinary.uploader().upload(
                        inputStream, 
                        ObjectUtils.asMap("upload_preset", "signspeak_user_details_upload")
                    )
                    imageUrl = result["secure_url"].toString()
                }

                val postId = UUID.randomUUID().toString()
                
                // Fetch user data again using await
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                val userName = userDoc.getString("name") ?: "Unknown User"
                val profilePic = userDoc.getString("profileImageUrl") ?: ""

                val newPost = PostModel(
                    postId = postId,
                    userId = currentUser.uid,
                    userName = userName,
                    userProfile = profilePic,
                    caption = caption,
                    imageUrl = imageUrl,
                    timestamp = System.currentTimeMillis(),
                    likeCount = 0
                )

                firestore.collection("posts").document(postId).set(newPost).await()

                withContext(Dispatchers.Main) {
                    binding.progressOverlay.visibility = View.GONE
                    Toast.makeText(this@UploadPostActivity, "Posted successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressOverlay.visibility = View.GONE
                    binding.btnPost.isEnabled = true
                    Toast.makeText(this@UploadPostActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
