package com.example.signspeak.api

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TokenResponse(
    val token: String,
    val error: String? = null
)

object ZegoTokenApi {

    // Provided public Ngrok URL
    private const val BASE_URL = "https://intercentral-gwendolyn-overtheatrically.ngrok-free.dev"

    suspend fun fetchToken(roomId: String, userId: String, userName: String): TokenResponse {
        return withContext(Dispatchers.IO) {
            try {
                // Ensure HTTP traffic is allowed in your app if you use plain http!
                val url = URL("$BASE_URL/generate-token")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("ngrok-skip-browser-warning", "69420") // Avoid ngrok block for automated requests
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val jsonBody = JSONObject().apply {
                    put("roomID", roomId)
                    put("userID", userId)
                    put("userName", userName)
                }

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonBody.toString())
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val jsonResponse = JSONObject(response)
                    val token = jsonResponse.optString("token", "")

                    if (token.isNotEmpty()) {
                        TokenResponse(token = token)
                    } else {
                        TokenResponse(token = "", error = "Empty token received from server")
                    }
                } else {
                    TokenResponse(token = "", error = "Server error: $responseCode")
                }
            } catch (e: Exception) {
                TokenResponse(token = "", error = "Network error: ${e.message}")
            }
        }
    }
}
