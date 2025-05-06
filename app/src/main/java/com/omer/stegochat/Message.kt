package com.omer.stegochat

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val base64Image: String = "",
    val chaosIndices: List<Int>? = null,
    val hiddenMessage: String = "",
    val timestamp: Long = 0
) 