package com.omer.stegochat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.omer.stegochat.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Şifreler eşleşmiyor", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(email, password, name)
            }
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser(email: String, password: String, name: String) {
        Log.d("RegisterActivity", "Kullanıcı kaydı başlatılıyor: $email")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        Log.d("RegisterActivity", "Firebase Auth kaydı başarılı, kullanıcı ID: ${it.uid}")
                        // Kullanıcı bilgilerini Firebase Realtime Database'e kaydet
                        val userData = User(
                            id = it.uid,
                            name = name,
                            email = email,
                            profileImage = ""
                        )
                        
                        Log.d("RegisterActivity", "Realtime Database'e kayıt yapılıyor: $userData")
                        database.reference.child("users").child(it.uid).setValue(userData)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Log.d("RegisterActivity", "Realtime Database kaydı başarılı")
                                    Toast.makeText(this, "Kayıt başarılı", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                } else {
                                    Log.e("RegisterActivity", "Realtime Database kaydı başarısız: ${dbTask.exception?.message}")
                                    Toast.makeText(this, "Kullanıcı bilgileri kaydedilemedi", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    Log.e("RegisterActivity", "Firebase Auth kaydı başarısız: ${task.exception?.message}")
                    Toast.makeText(this, "Kayıt başarısız: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
} 