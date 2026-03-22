package com.example.signspeak.data

import com.google.firebase.Timestamp

data class UserProfile(
    val name: String,
    val age: Int,
    val gender: String,
    val disability: String,
    val severity: String,
    val language: String,
    val communicationMode: String,
    val bloodGroup: String,
    val medicalCondition: String,
    val emergency1: String,
    val emergency2: String,
    val guardian: String,
    val signUsage: String,
    val experience: String,
    val sosEnabled: Boolean,
    val profileImageUrl: String,
    val createdAt: Timestamp = Timestamp.now()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "age" to age,
            "gender" to gender,
            "disability" to disability,
            "severity" to severity,
            "language" to language,
            "communicationMode" to communicationMode,
            "bloodGroup" to bloodGroup,
            "medicalCondition" to medicalCondition,
            "emergency1" to emergency1,
            "emergency2" to emergency2,
            "guardian" to guardian,
            "signUsage" to signUsage,
            "experience" to experience,
            "sosEnabled" to sosEnabled,
            "profileImageUrl" to profileImageUrl,
            "createdAt" to createdAt
        )
    }
}
