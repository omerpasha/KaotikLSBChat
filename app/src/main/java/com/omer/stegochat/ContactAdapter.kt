package com.omer.stegochat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ContactAdapter(
    private var contacts: List<Contact>,
    private val onContactClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    fun updateContacts(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact)
        holder.itemView.setOnClickListener { onContactClick(contact) }
    }

    override fun getItemCount(): Int = contacts.size

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewProfile: ImageView = itemView.findViewById(R.id.imageViewProfile)
        private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        private val textViewLastMessage: TextView = itemView.findViewById(R.id.textViewLastMessage)

        fun bind(contact: Contact) {
            textViewName.text = contact.name
            textViewLastMessage.text = contact.lastMessage

            if (contact.profileImage.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(contact.profileImage)
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .into(imageViewProfile)
            } else {
                imageViewProfile.setImageResource(android.R.drawable.ic_menu_myplaces)
            }
        }
    }
} 