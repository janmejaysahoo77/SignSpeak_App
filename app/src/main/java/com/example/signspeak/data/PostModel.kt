package com.example.signspeak.data

data class PostModel(
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfile: String = "",
    val caption: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0L,
    val likeCount: Int = 0
)
