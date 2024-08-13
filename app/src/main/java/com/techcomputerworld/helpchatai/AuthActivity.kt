package com.techcomputerworld.helpchatai

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.techcomputerworld.helpchatai.databinding.ActivityAuthBinding
import java.text.SimpleDateFormat
import java.util.Date

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth

    companion object {
        lateinit var useremail: String
        lateinit var providerSession: String
        private const val TAG = "AuthActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        checkCurrentUser()
        setupUI()
        logAnalyticsEvent()
    }

    private fun checkCurrentUser() {
        auth.currentUser?.let { user ->
            goHome(user.email.orEmpty(), user.providerId)
        }
    }
    private fun setupUI() {
        binding.apply {
            // Añadir valores predeterminados para pruebas rápidas // tienes que dar en siguiente o intro el el campo texto email y contraseña para que se active el login
            //emailEditText.setText("david1@test.com")  // Bórralo después de las pruebas
            //passwordEditText.setText("123456")        // Bórralo después de las pruebas

            // Configurar TextWatchers para habilitar/deshabilitar el botón de login según el estado de los campos de texto
            emailEditText.addTextChangedListener(textWatcher)
            passwordEditText.addTextChangedListener(textWatcher)

            loginButton.setOnClickListener { login() }
            signUpButton.setOnClickListener { register() }
            togglePasswordVisibility(passwordEditText)
            togglePasswordVisibility(password2EditText)

            lyTerms.visibility = View.INVISIBLE
            password2EditText.visibility = View.INVISIBLE

            // Inicialmente deshabilitar el botón de login
            loginButton.isEnabled = false
        }
    }

    // TextWatcher que observa los cambios en los campos de texto
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // Verificar si ambos campos están completos
            binding.apply {
                val isEmailFilled = emailEditText.text.toString().isNotBlank()
                val isPasswordFilled = passwordEditText.text.toString().isNotBlank()

                // Habilitar o deshabilitar el botón según el estado de los campos
                loginButton.isEnabled = isEmailFilled && isPasswordFilled

                // Cambiar el color del botón dependiendo de si está habilitado o no
                loginButton.setBackgroundColor(
                    ContextCompat.getColor(
                        this@AuthActivity,
                        if (loginButton.isEnabled) R.color.teal_700 else R.color.gray
                    )
                )
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun logAnalyticsEvent() {
        val analytics = FirebaseAnalytics.getInstance(this)
        analytics.logEvent("Initscreen", Bundle().apply {
            putString("message", "Complete Firebase integration")
        })
    }

    private fun login() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please enter email and password")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    goHome(email, "email")
                } else {
                    showToast("Authentication failed")

                }
            }.addOnFailureListener {
                showToast("Login failed: ${it.message}")
                Log.d("auth", "${it.message}")
            }
    }

    private fun register() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Email and password must not be empty")
            return
        }
        //Here Validate Email
        //aqui probamos a validad el email y si no valida le damos un mensaje al usuario de que tiene que poner un email valido
        checkEmail(email)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            Log.d(TAG, "Generated Token: ${tokenTask.result?.token}")
                            saveUserToDatabase(email)
                            goHome(email, "email")
                        } else {
                            showToast("Failed to generate token: ${tokenTask.exception?.message}")
                        }
                    }
                } else {
                    showToast("Registration failed: ${task.exception?.message}")
                }
            }
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveUserToDatabase(email: String) {
        val dateRegister = SimpleDateFormat("dd/MM/yyyy").format(Date())
        FirebaseFirestore.getInstance().collection("users")
            .document(email)
            .set(hashMapOf("user" to email, "dateRegister" to dateRegister))
    }

    private fun goHome(email: String, provider: String) {
        useremail = email
        providerSession = provider
        Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider)
        }.also {
            startActivity(it)
            finish()
        }
    }

    private fun togglePasswordVisibility(editText: EditText) {
        editText.setOnClickListener {
            val isVisible = editText.transformationMethod !is PasswordTransformationMethod
            editText.transformationMethod = if (isVisible) {
                PasswordTransformationMethod.getInstance()
            } else {
                HideReturnsTransformationMethod.getInstance()
            }
            editText.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                if (isVisible) R.drawable.ic_eye else R.drawable.ic_eye_off,
                0
            )
        }
    }

    fun forgotPassword(view: View) {
        val email = binding.emailEditText.text.toString()
        if (email.isNotEmpty()) {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showToast("Email sent to $email")
                    } else {
                        showToast("User with this email not found")
                    }
                }
        } else {
            showToast("Enter a valid email")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun goTerms(view: View) {
        startActivity(Intent(this, TermsActivity::class.java))
    }

    private fun showAlert() {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage("An error occurred while authenticating the user")
            .setPositiveButton("Accept", null)
            .create()
            .show()
    }
    //este metodo quiero implementarlo de alguna manera para que chequear si el email es valido que no sé como hacerlo.
    fun checkEmail(email: String) {
        val isValid = ValidateEmail.isEmail(email)
        if (isValid) {
            showToast("The email is valid")

            //println("El correo electrónico es válido.")
        } else {
            showToast("The email is not valid")
            return
            //println("El correo electrónico no es válido.")
        }
    }
}
