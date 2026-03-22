package com.example.signspeak.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    suspend fun saveUserProfile(userProfile: UserProfile): Result<Unit> {
        val userId = getCurrentUserId()
            ?: return Result.failure(Exception("User not authenticated. Please log in again."))

        return try {
            suspendCancellableCoroutine { continuation ->
                firestore.collection("users")
                    .document(userId)
                    .set(userProfile.toMap())
                    .addOnSuccessListener {
                        if (continuation.isActive) {
                            continuation.resume(Result.success(Unit))
                        }
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) {
                            continuation.resumeWithException(exception)
                        }
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): Result<Map<String, Any>> {
        val userId = getCurrentUserId()
            ?: return Result.failure(Exception("User not authenticated."))

        return try {
            suspendCancellableCoroutine { continuation ->
                firestore.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (continuation.isActive) {
                            if (document.exists()) {
                                continuation.resume(Result.success(document.data ?: emptyMap()))
                            } else {
                                continuation.resume(Result.failure(Exception("Profile not found.")))
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) {
                            continuation.resumeWithException(exception)
                        }
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        val userId = getCurrentUserId()
            ?: return Result.failure(Exception("User not authenticated."))

        return try {
            suspendCancellableCoroutine { continuation ->
                firestore.collection("users")
                    .document(userId)
                    .update(updates)
                    .addOnSuccessListener {
                        if (continuation.isActive) {
                            continuation.resume(Result.success(Unit))
                        }
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) {
                            continuation.resumeWithException(exception)
                        }
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
