package com.omer.stegochat

import java.io.Serializable

data class Contact(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImage: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0
) : Serializable 