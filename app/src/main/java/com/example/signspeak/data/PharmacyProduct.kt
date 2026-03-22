package com.example.signspeak.data

import com.google.firebase.Timestamp

data class PharmacyProduct(
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val price: Int = 0,
    val stock: Int = 0,
    val usage: String = "",
    val sideEffects: String = "",
    val pharmacistId: String = "",
    val createdAt: Timestamp? = null
)
