package com.omer.stegochat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.omer.stegochat.R

class MessageAdapter(
    private var messages: List<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewMessage: TextView = view.findViewById(R.id.textViewMessage)
        val imageViewMessage: ImageView = view.findViewById(R.id.imageViewMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == 0) {
            R.layout.item_message_sent
        } else {
            R.layout.item_message_received
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        
        if (message.text.isNotEmpty()) {
            holder.textViewMessage.text = message.text
            holder.textViewMessage.visibility = View.VISIBLE
            holder.imageViewMessage.visibility = View.GONE
        } else if (message.base64Image.isNotEmpty()) {
            holder.textViewMessage.visibility = View.GONE
            holder.imageViewMessage.visibility = View.VISIBLE

            try {
                val imageBytes = Base64.decode(message.base64Image, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.imageViewMessage.setImageBitmap(bitmap)

                if (message.chaosIndices != null) {
                    holder.imageViewMessage.setOnLongClickListener {
                        try {
                            val hiddenMessage = decodeMessage(bitmap, message.chaosIndices)
                            showHiddenMessage(holder.itemView.context, hiddenMessage)
                        } catch (e: Exception) {
                            // Hata durumunda sessizce devam et
                        }
                        true
                    }
                }
            } catch (e: Exception) {
                // Hata durumunda sessizce devam et
            }
        }
    }

    private fun decodeMessage(bitmap: Bitmap, chaosIndices: List<Int>): String {
        return try {
            EncodeAndDecodeHelper.decode(bitmap, chaosIndices)
        } catch (e: Exception) {
            "Mesaj çözülemedi"
        }
    }

    private fun showHiddenMessage(context: android.content.Context, hiddenMessage: String) {
        AlertDialog.Builder(context)
            .setTitle("Gizli Mesaj")
            .setMessage(hiddenMessage)
            .setPositiveButton("Tamam", null)
            .show()
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) 0 else 1
    }

    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    fun getMessages(): List<Message> = messages
} 