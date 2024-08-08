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
    //private lateinit var email: String
    //private lateinit var provider: String
    private lateinit var emailTextView: TextView
    private lateinit var providerTextView: TextView
    private lateinit var logOutButton : Button
    private lateinit var chatgptButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_home)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //emailTextView = findViewById(R.id.ChatGPT)
        //providerTextView = findViewById(R.id.providerTextView)
        logOutButton = binding.logOutButton
        chatgptButton = binding.btChatgpt
        //recuperar los 2 parametros del intent
        val bundle: Bundle? = intent.extras
        val email:String? = bundle?.getString("email")
        val provider:String? = bundle?.getString("provider")
        //setup
        //setup(email ?: "", provider ?: "")
        //logout
        signOff()
        gochatgpt()
    }
    /*
    private fun setup(email: String, provider: String) {
        title = "Home"
        emailTextView.text = email
        providerTextView.text = provider

        logOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            onBackPressedDispatcher.onBackPressed()
        }
    }
    */

    fun callSignOut(view: View) {
        signOff()
    }
    private fun signOff() {
        FirebaseAuth.getInstance().signOut()
        //hay que mandarlo a la ventana de inicio
        startActivity(Intent(this,AuthActivity::class.java))
    }
    private fun gochatgpt() {
        val chatGptIntent = Intent(this, ChatGptActivity::class.java)
        startActivity(chatGptIntent)
    }
}