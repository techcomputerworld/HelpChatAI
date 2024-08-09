package com.techcomputerworld.helpchatai

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.techcomputerworld.helpchatai.databinding.ActivityAuthBinding
import com.techcomputerworld.helpchatai.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = intent.getStringExtra("email").orEmpty()
        val provider = intent.getStringExtra("provider").orEmpty()

        setupUI(email, provider)
        setupListeners()
    }

    private fun setupUI(email: String, provider: String) {
        binding.apply {
          //  Usa esto cuando necesites
            //emailTextView.text = email
            //oviderTextView.text = provider
            title = "Home"
        }
    }

    private fun setupListeners() {
        binding.apply {
            logOutButton.setOnClickListener { signOff() }
            btChatgpt.setOnClickListener { goChatGpt() }
        }
    }

    private fun signOff() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, AuthActivity::class.java))
        finish() // Cerrar la actividad actual para que no pueda volver a ella con el botón de retroceso
    }

    private fun goChatGpt() {
        // Implementa la navegación a ChatGptActivity cuando esté disponible
        // val chatGptIntent = Intent(this, ChatGptActivity::class.java)
        // startActivity(chatGptIntent)
    }
}