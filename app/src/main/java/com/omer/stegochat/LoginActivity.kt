package com.omer.stegochat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.omer.stegochat.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Eğer kullanıcı zaten giriş yapmışsa ana sayfaya yönlendir
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        Log.d("LoginActivity", "Giriş denemesi başlatılıyor: $email")
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginActivity", "Giriş başarılı: ${auth.currentUser?.uid}")
                    Toast.makeText(this, "Giriş başarılı", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    val errorMessage = task.exception?.message ?: "Bilinmeyen hata"
                    Log.e("LoginActivity", "Giriş başarısız: $errorMessage")
                    Toast.makeText(this, "Giriş başarısız: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("LoginActivity", "Giriş işlemi sırasında hata: ${e.message}")
                Toast.makeText(this, "Giriş işlemi sırasında hata: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
} 