package com.example.signspeak.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ImageUploader {

    companion object {
        private const val CLOUD_NAME = "dzubjgb9y"
        private const val UPLOAD_PRESET = "signspeak_user_details_upload"
        private const val UPLOAD_URL =
            "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun uploadImage(context: Context, imageUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val imageBytes = context.contentResolver.openInputStream(imageUri)?.use {
                    it.readBytes()
                } ?: return@withContext Result.failure(Exception("Unable to read image file"))

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        "profile_image.jpg",
                        imageBytes.toRequestBody("image/*".toMediaType())
                    )
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .addFormDataPart("folder", "signspeak_profiles")
                    .build()

                val request = Request.Builder()
                    .url(UPLOAD_URL)
                    .post(requestBody)
                    .build()

                val response = suspendCancellableCoroutine<Response> { continuation ->
                    val call = client.newCall(request)

                    continuation.invokeOnCancellation { call.cancel() }

                    call.enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            if (continuation.isActive) {
                                continuation.resumeWithException(e)
                            }
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (continuation.isActive) {
                                continuation.resume(response)
                            }
                        }
                    })
                }

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Upload failed with code: ${response.code}")
                    )
                }

                val responseBody = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty response from server"))

                val json = JSONObject(responseBody)
                val secureUrl = json.getString("secure_url")

                Result.success(secureUrl)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
