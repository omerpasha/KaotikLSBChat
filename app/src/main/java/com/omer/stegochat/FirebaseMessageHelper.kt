package com.omer.stegochat

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class FirebaseMessageHelper {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    fun getContacts(currentUserId: String, callback: (List<Contact>) -> Unit) {
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contacts = mutableListOf<Contact>()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue<User>()
                    if (user != null && user.id != currentUserId) {
                        contacts.add(Contact(
                            id = user.id,
                            name = user.name,
                            email = user.email,
                            profileImage = user.profileImage
                        ))
                    }
                }
                callback(contacts)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    fun sendMessage(senderId: String, receiverId: String, message: Message) {
        val chatId = if (senderId < receiverId) {
            "$senderId-$receiverId"
        } else {
            "$receiverId-$senderId"
        }

        val messageRef = database.getReference("chats/$chatId/messages")
        val newMessageRef = messageRef.push()
        
        // Mesajı kaydet
        newMessageRef.setValue(message.copy(id = newMessageRef.key ?: ""))
        
        // Son mesajı güncelle
        val chatRef = database.getReference("chats/$chatId")
        chatRef.child("lastMessage").setValue(message.copy(id = newMessageRef.key ?: ""))
        chatRef.child("timestamp").setValue(System.currentTimeMillis())
    }

    fun getMessages(chatId: String, callback: (List<Message>) -> Unit) {
        val messageRef = database.getReference("chats/$chatId/messages")
        messageRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue<Message>()
                    if (message != null) {
                        messages.add(message)
                    }
                }
                callback(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }
} 