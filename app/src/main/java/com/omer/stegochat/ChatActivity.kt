package com.omer.stegochat

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.omer.stegochat.databinding.ActivityChatBinding
import com.omer.stegochat.EncodeAndDecodeHelper

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class ChatActivity : AppCompatActivity() {
    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
 
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var messageHelper: FirebaseMessageHelper
    private var contact: Contact? = null
    private var selectedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            Toast.makeText(this, "Oturum açık değil", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        messageHelper = FirebaseMessageHelper()

        // Kişi bilgilerini al
        contact = intent.getSerializableExtra("contact") as? Contact
        if (contact == null) {
            Toast.makeText(this, "Kişi bilgileri alınamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Kişi bilgilerini göster
        binding.textViewContactName.text = contact?.name
        if (contact?.profileImage?.isNotEmpty() == true) {
            Glide.with(this)
                .load(contact?.profileImage)
                .into(binding.imageViewProfile)
        }

        // Mesaj listesini ayarla
        messageAdapter = MessageAdapter(emptyList(), auth.currentUser?.uid ?: "")
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.recyclerViewMessages.adapter = messageAdapter

        // Mesaj gönderme butonu
        binding.buttonSend.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                Log.d("ChatActivity", "Mesaj gönderme butonu tıklandı: $messageText")
                sendTextMessage(messageText)
            } else {
                Toast.makeText(this, "Mesaj boş olamaz", Toast.LENGTH_SHORT).show()
            }
        }

        // Resim seçme butonu
        binding.buttonAttach.setOnClickListener {
            showImageOptionsDialog()
        }

        // Mesajları yükle
        loadMessages()
    }

    private fun showImageOptionsDialog() {
        val options = arrayOf("Normal Resim Gönder", "Gizli Mesajlı Resim Gönder")
        AlertDialog.Builder(this)
            .setTitle("Resim Gönderme Seçenekleri")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> selectImage(false)
                    1 -> selectImage(true)
                }
            }
            .show()
    }

    private fun selectImage(withHiddenMessage: Boolean) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun showHiddenMessageDialog(imageUri: Uri) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_hidden_message, null)
        val editTextHiddenMessage = dialogView.findViewById<android.widget.EditText>(R.id.editTextHiddenMessage)
        val textViewMaxChars = dialogView.findViewById<android.widget.TextView>(R.id.textViewMaxChars)
        
        // Maksimum karakter sayısını göster
        val maxCharacters = calculateMaxHiddenCharacters(selectedBitmap!!)
        textViewMaxChars.text = "Resim Boyutu: ${selectedBitmap!!.width}x${selectedBitmap!!.height}\nMaksimum karakter: $maxCharacters"

        AlertDialog.Builder(this)
            .setTitle("Gizli Mesaj")
            .setView(dialogView)
            .setPositiveButton("Gönder") { _, _ ->
                val hiddenMessage = editTextHiddenMessage.text.toString()
                if (hiddenMessage.isNotEmpty()) {
                    if (hiddenMessage.length <= maxCharacters) {
                        sendImageMessageWithSteganography(imageUri, hiddenMessage)
                    } else {
                        Toast.makeText(this, 
                            "Bu resme (${selectedBitmap!!.width}x${selectedBitmap!!.height}) en fazla $maxCharacters karakter gizlenebilir. " +
                            "Lütfen daha büyük bir resim seçin veya mesajı kısaltın.", 
                            Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Gizli mesaj boş olamaz", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun loadMessages() {
        val currentUserId = auth.currentUser?.uid ?: return
        val contactId = contact?.id ?: return

        val chatId = if (currentUserId < contactId) {
            "$currentUserId-$contactId"
        } else {
            "$contactId-$currentUserId"
        }

        Log.d("ChatActivity", "Mesajlar yükleniyor - Chat ID: $chatId")
        messageHelper.getMessages(chatId) { messages ->
            Log.d("ChatActivity", "Yüklenen mesaj sayısı: ${messages.size}")
            runOnUiThread {
                messageAdapter.updateMessages(messages)
                if (messages.isNotEmpty()) {
                    binding.recyclerViewMessages.scrollToPosition(messages.size - 1)
                }
            }
        }
    }

    private fun sendTextMessage(text: String) {
        val currentUserId = auth.currentUser?.uid
        val contactId = contact?.id

        if (currentUserId == null) {
            Toast.makeText(this, "Oturum açık değil", Toast.LENGTH_SHORT).show()
            return
        }

        if (contactId == null) {
            Toast.makeText(this, "Kişi bilgisi bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }

        val message = Message(
            id = UUID.randomUUID().toString(),
            senderId = currentUserId,
            receiverId = contactId,
            text = text,
            imageUrl = "",
            base64Image = "",
            timestamp = System.currentTimeMillis()
        )

        Log.d("ChatActivity", "Metin mesajı gönderiliyor: $message")
        messageHelper.sendMessage(currentUserId, contactId, message)
        binding.editTextMessage.text.clear()
    }

    private fun sendImageMessage(imageUri: Uri) {
        try {
            Log.d("ChatActivity", "Resim gönderme işlemi başladı: $imageUri")
            val currentUserId = auth.currentUser?.uid
            val contactId = contact?.id

            if (currentUserId == null) {
                Log.e("ChatActivity", "Oturum açık değil")
                Toast.makeText(this, "Oturum açık değil", Toast.LENGTH_SHORT).show()
                return
            }

            if (contactId == null) {
                Log.e("ChatActivity", "Kişi bilgisi bulunamadı")
                Toast.makeText(this, "Kişi bilgisi bulunamadı", Toast.LENGTH_SHORT).show()
                return
            }

            Log.d("ChatActivity", "Kullanıcı ID: $currentUserId, Alıcı ID: $contactId")

            // Uri'den InputStream al
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e("ChatActivity", "Resim dosyası açılamadı")
                Toast.makeText(this, "Resim dosyası açılamadı", Toast.LENGTH_SHORT).show()
                return
            }

            // Bitmap'e dönüştür
            val bitmap = BitmapFactory.decodeStream(inputStream)
            if (bitmap == null) {
                Log.e("ChatActivity", "Resim yüklenemedi")
                Toast.makeText(this, "Resim yüklenemedi", Toast.LENGTH_SHORT).show()
                inputStream.close()
                return
            }

            Log.d("ChatActivity", "Orijinal resim boyutları: ${bitmap.width}x${bitmap.height}")
            Log.d("ChatActivity", "Maksimum gizlenebilecek karakter sayısı: ${(bitmap.width * bitmap.height) / 8}")

            // Resmi sıkıştır
            val byteArrayOutputStream = ByteArrayOutputStream()
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)) {
                Log.e("ChatActivity", "Resim sıkıştırılamadı")
                Toast.makeText(this, "Resim sıkıştırılamadı", Toast.LENGTH_SHORT).show()
                inputStream.close()
                bitmap.recycle()
                return
            }

            val imageBytes = byteArrayOutputStream.toByteArray()
            val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            
            Log.d("ChatActivity", "Base64 veri boyutu: ${base64Image.length} karakter")
            
            // Boyut kontrolü
            if (base64Image.length > 1000000) { // ~750KB
                Log.e("ChatActivity", "Resim çok büyük: ${base64Image.length} karakter (${base64Image.length / 1024}KB)")
                Toast.makeText(this, "Resim çok büyük, lütfen daha küçük bir resim seçin", Toast.LENGTH_SHORT).show()
                inputStream.close()
                bitmap.recycle()
                return
            }

            // Mesajı oluştur ve gönder
            val message = Message(
                id = UUID.randomUUID().toString(),
                senderId = currentUserId,
                receiverId = contactId,
                text = "",
                imageUrl = "",
                base64Image = base64Image,
                timestamp = System.currentTimeMillis()
            )

            Log.d("ChatActivity", "Mesaj gönderiliyor: ID=${message.id}, Boyut=${base64Image.length}")
            messageHelper.sendMessage(currentUserId, contactId, message)

            // Kaynakları temizle
            inputStream.close()
            byteArrayOutputStream.close()
            bitmap.recycle()
            Log.d("ChatActivity", "Resim gönderme işlemi başarıyla tamamlandı")

        } catch (e: Exception) {
            Log.e("ChatActivity", "Resim gönderilirken hata: ${e.message}", e)
            Toast.makeText(this, "Resim gönderilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendImageMessageWithSteganography(imageUri: Uri, hiddenMessage: String) {
        try {
            Log.d("ChatActivity", "Steganografi işlemi başladı. Gizli mesaj: $hiddenMessage")
            val currentUserId = auth.currentUser?.uid
            val contactId = contact?.id

            if (currentUserId == null || contactId == null) {
                Log.e("ChatActivity", "Kullanıcı bilgileri eksik")
                Toast.makeText(this, "Kullanıcı bilgileri eksik", Toast.LENGTH_SHORT).show()
                return
            }

            Log.d("ChatActivity", "Kullanıcı ID: $currentUserId, Alıcı ID: $contactId")

            // Yükleme dialogunu göster
            val progressDialog = AlertDialog.Builder(this)
                .setTitle("İşlem Devam Ediyor")
                .setMessage("Gizli mesaj resme ekleniyor...")
                .setCancelable(false)
                .create()
            progressDialog.show()

            // Arka thread'de işlemi başlat
            Thread {
                try {
                    Log.d("ChatActivity", "1. Adım: Resim yükleme başladı")
                    // Uri'den Bitmap oluştur
                    val inputStream = contentResolver.openInputStream(imageUri)
                    if (inputStream == null) {
                        runOnUiThread {
                            progressDialog.dismiss()
                            Log.e("ChatActivity", "Resim dosyası açılamadı")
                            Toast.makeText(this, "Resim dosyası açılamadı", Toast.LENGTH_SHORT).show()
                        }
                        return@Thread
                    }

                    var bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap == null) {
                        runOnUiThread {
                            progressDialog.dismiss()
                            Log.e("ChatActivity", "Resim yüklenemedi")
                            Toast.makeText(this, "Resim yüklenemedi", Toast.LENGTH_SHORT).show()
                        }
                        inputStream.close()
                        return@Thread
                    }

                    Log.d("ChatActivity", "2. Adım: Resim yüklendi. Boyutlar: ${bitmap.width}x${bitmap.height}")

                    // Eğer resim 512x512'den büyükse küçült
                    if (bitmap.width > 512 || bitmap.height > 512) {
                        Log.d("ChatActivity", "3. Adım: Resim küçültme başladı")
                        val scale = 512f / maxOf(bitmap.width, bitmap.height)
                        val newWidth = (bitmap.width * scale).toInt()
                        val newHeight = (bitmap.height * scale).toInt()
                        
                        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
                        bitmap.recycle()
                        bitmap = scaledBitmap
                        
                        Log.d("ChatActivity", "4. Adım: Resim küçültüldü. Yeni boyutlar: ${bitmap.width}x${bitmap.height}")
                    }

                    // Her karakter 9 bit, her piksel 3 bit saklayabilir (RGB kanallarında 1'er bit)
                    val maxCharacters = (bitmap.width * bitmap.height) / 3
                    Log.d("ChatActivity", "5. Adım: Maksimum karakter hesaplandı: $maxCharacters")

                    // Karakter sayısı kontrolü
                    if (hiddenMessage.length > maxCharacters) {
                        runOnUiThread {
                            progressDialog.dismiss()
                            Log.e("ChatActivity", "Mesaj çok uzun: ${hiddenMessage.length} karakter, maksimum: $maxCharacters")
                            Toast.makeText(this, 
                                "Bu resme (${bitmap.width}x${bitmap.height}) en fazla $maxCharacters karakter gizlenebilir. " +
                                "Lütfen daha büyük bir resim seçin veya mesajı kısaltın.", 
                                Toast.LENGTH_LONG).show()
                        }
                        inputStream.close()
                        bitmap.recycle()
                        return@Thread
                    }

                    // Steganografi uygula
                    Log.d("ChatActivity", "6. Adım: Steganografi uygulanıyor...")
                    val startTime = System.currentTimeMillis()
                    val encodeResult = EncodeAndDecodeHelper.encode(bitmap, hiddenMessage)
                    val endTime = System.currentTimeMillis()
                    Log.d("ChatActivity", "7. Adım: Steganografi tamamlandı. Süre: ${endTime - startTime}ms")

                    // Resmi sıkıştır
                    Log.d("ChatActivity", "8. Adım: Resim sıkıştırma başladı")
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    if (!encodeResult.bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)) {
                        runOnUiThread {
                            progressDialog.dismiss()
                            Log.e("ChatActivity", "Resim sıkıştırılamadı")
                            Toast.makeText(this, "Resim sıkıştırılamadı", Toast.LENGTH_SHORT).show()
                        }
                        inputStream.close()
                        bitmap.recycle()
                        encodeResult.bitmap.recycle()
                        return@Thread
                    }

                    val imageBytes = byteArrayOutputStream.toByteArray()
                    val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)
                    
                    Log.d("ChatActivity", "9. Adım: Base64 dönüşümü tamamlandı. Boyut: ${base64Image.length} karakter")
                    
                    // Boyut kontrolü
                    if (base64Image.length > 1000000) { // ~750KB
                        runOnUiThread {
                            progressDialog.dismiss()
                            Log.e("ChatActivity", "Resim çok büyük: ${base64Image.length} karakter (${base64Image.length / 1024}KB)")
                            Toast.makeText(this, "Resim çok büyük, lütfen daha küçük bir resim seçin", Toast.LENGTH_SHORT).show()
                        }
                        inputStream.close()
                        bitmap.recycle()
                        encodeResult.bitmap.recycle()
                        return@Thread
                    }

                    // Mesajı oluştur ve gönder
                    Log.d("ChatActivity", "10. Adım: Mesaj gönderme başladı")
                    val message = Message(
                        id = UUID.randomUUID().toString(),
                        senderId = currentUserId,
                        receiverId = contactId,
                        text = "",
                        imageUrl = "",
                        base64Image = base64Image,
                        chaosIndices = encodeResult.chaosIndices,
                        hiddenMessage = hiddenMessage,
                        timestamp = System.currentTimeMillis()
                    )

                    Log.d("ChatActivity", "11. Adım: Mesaj gönderiliyor: ID=${message.id}, Boyut=${base64Image.length}")
                    
                    runOnUiThread {
                        progressDialog.dismiss()
                        messageHelper.sendMessage(currentUserId, contactId, message)
                        Toast.makeText(this, "Gizli mesaj başarıyla gönderildi", Toast.LENGTH_SHORT).show()
                    }

                    // Kaynakları temizle
                    Log.d("ChatActivity", "12. Adım: Kaynaklar temizleniyor")
                    inputStream.close()
                    byteArrayOutputStream.close()
                    bitmap.recycle()
                    encodeResult.bitmap.recycle()
                    Log.d("ChatActivity", "13. Adım: Steganografi işlemi başarıyla tamamlandı")

                } catch (e: Exception) {
                    runOnUiThread {
                        progressDialog.dismiss()
                        Log.e("ChatActivity", "Steganografi uygulanırken hata: ${e.message}", e)
                        Toast.makeText(this, "Resim gönderilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()

        } catch (e: Exception) {
            Log.e("ChatActivity", "Steganografi başlatılırken hata: ${e.message}", e)
            Toast.makeText(this, "İşlem başlatılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateMaxHiddenCharacters(bitmap: Bitmap): Int {
        // Her karakter 9 bit, her piksel 3 bit saklayabilir (RGB kanallarında 1'er bit)
        return (bitmap.width * bitmap.height) / 3
    }

    private fun showImageSelectionDialog() {
        val maxCharacters = calculateMaxHiddenCharacters(selectedBitmap!!)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Resim Seçimi")
            .setMessage("Bu resme en fazla $maxCharacters karakter gizlenebilir.")
            .setPositiveButton("Gizli Mesaj Ekle") { _, _ ->
                // Bitmap'i geçici bir dosyaya kaydet ve Uri'ye dönüştür
                val tempFile = File.createTempFile("temp_image", ".png", cacheDir)
                selectedBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(tempFile))
                val imageUri = Uri.fromFile(tempFile)
                showHiddenMessageDialog(imageUri)
            }
            .setNegativeButton("Sadece Resim Gönder") { _, _ ->
                // Bitmap'i geçici bir dosyaya kaydet ve Uri'ye dönüştür
                val tempFile = File.createTempFile("temp_image", ".png", cacheDir)
                selectedBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(tempFile))
                val imageUri = Uri.fromFile(tempFile)
                sendImageMessage(imageUri)
            }
            .setNeutralButton("İptal", null)
            .create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    data?.data?.let { uri ->
                        try {
                            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                            selectedBitmap = bitmap
                            showImageSelectionDialog()
                        } catch (e: Exception) {
                            Log.e("ChatActivity", "Resim yüklenirken hata: ${e.message}")
                            Toast.makeText(this, "Resim yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
} 