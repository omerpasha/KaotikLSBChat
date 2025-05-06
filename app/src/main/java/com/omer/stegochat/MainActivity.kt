package com.omer.stegochat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omer.stegochat.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar'ı ayarla
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Kişiler"

        // Toolbar ve status bar rengini register_orange yap
        binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.register_orange))
        window.statusBarColor = ContextCompat.getColor(this, R.color.register_orange)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Eğer kullanıcı giriş yapmamışsa login sayfasına yönlendir
        if (auth.currentUser == null) {
            Log.d("MainActivity", "Kullanıcı giriş yapmamış, login sayfasına yönlendiriliyor")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        Log.d("MainActivity", "Giriş yapmış kullanıcı: ${auth.currentUser?.uid}")

        contactAdapter = ContactAdapter(emptyList()) { contact ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("contact", contact)
            startActivity(intent)
        }
        
        binding.recyclerViewContacts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewContacts.adapter = contactAdapter

        // Kişileri yükle
        loadContacts()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        auth.signOut()
        Toast.makeText(this, "Çıkış yapıldı", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun loadContacts() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.e("MainActivity", "Kullanıcı ID'si bulunamadı")
            return
        }

        Log.d("MainActivity", "Kullanıcılar yükleniyor... Mevcut kullanıcı ID: $currentUserId")
        
        database.reference.child("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val contacts = mutableListOf<Contact>()
                    Log.d("MainActivity", "Veri değişti, toplam kullanıcı sayısı: ${snapshot.childrenCount}")
                    
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        Log.d("MainActivity", "Kullanıcı verisi: $user")
                        if (user != null && user.id != currentUserId) {
                            Log.d("MainActivity", "Kullanıcı bulundu: ${user.name} (${user.id})")
                            contacts.add(Contact(
                                id = user.id,
                                name = user.name,
                                email = user.email,
                                profileImage = user.profileImage
                            ))
                        }
                    }
                    
                    Log.d("MainActivity", "Toplam ${contacts.size} kişi listelendi")
                    contactAdapter.updateContacts(contacts)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainActivity", "Veri yükleme hatası: ${error.message}")
                    Toast.makeText(this@MainActivity, "Kişiler yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

