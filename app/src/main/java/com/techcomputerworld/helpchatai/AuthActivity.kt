package com.techcomputerworld.helpchatai

import com.techcomputerworld.helpchatai.databinding.ActivityAuthBinding

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.widget.doOnTextChanged
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.techcomputerworld.helpchatgpt.ProviderType
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.properties.Delegates


class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    companion object {
        lateinit var useremail: String
        lateinit var providerSession: String
        private const val TAG = "AuthActivity"
    }
    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    private var password2 by Delegates.notNull<String>()
    //loginButton only button to register and login user
    private lateinit var loginButton: Button
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPassword2: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var lyTerms: LinearLayout
    private var isVisible: Boolean = false;
    private var isVisible2: Boolean = false;
    private var customToken: String? = null
    private val TAG: String = "AuthActivity"
    //al inicio de la aplicación nos va a dar el email y el providerId
    public override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            goHome(currentUser.email.toString(), currentUser.providerId)
        }
        updateUI(currentUser)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_auth)
        //variableas que voy a usar
        val eyeOpen = R.drawable.ic_eye
        val eyeClosed = R.drawable.ic_eye_off
        lyTerms = binding.lyTerms
        //lyTerms = findViewById(R.id.lyTerms)
        lyTerms.visibility = View.INVISIBLE
        //Password2EditText sera invisible al cargar la app solo sera  visible cuando necesite registrar un usuario
        etPassword2 = binding.password2EditText
        etPassword2.visibility = View.INVISIBLE
        //binding.loginButton
        //loginButton = findViewById(R.id.loginButton)
        etEmail = binding.emailEditText
        etPassword = binding.passwordEditText
        // Initialize Firebase Auth the instance
        auth = FirebaseAuth.getInstance()
        //mostrar el Splashscreen durante 1 segundo 1000 miliseconds
        //show the splashscreen 1000 miliseconds 1 second ago
        Thread.sleep(1000)
        screenSplash.setKeepOnScreenCondition { false }
        /* Codigo para pasar al otro activity
        val intent = Intent(this, DetailActivity::class.java)
        startActivity(intent)
        finish();
        */
        // Analytics Events
        val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Complete Firebase integration")
        analytics.logEvent("Initscreen", bundle)

        manageButtonLogin()
        etEmail.doOnTextChanged{text, start, before, count -> manageButtonLogin() }
        etPassword.doOnTextChanged{text, start, before, count -> manageButtonLogin() }
        managePassword()
        etPassword2.doOnTextChanged{text, start, before, count -> managePassword() }
        //un metodo que llame al setOnClickListener() del boton del ojo
        clickEye()
        //etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, if (isVisible) eyeOpen else eyeClosed, 0)
    }

    // ir a la ventana de inicio o activity de inicio  de la aplicación
    /*override fun onBackPressed() {

         val startmain = Intent(Intent.ACTION_MAIN)
         startmain.addCategory(Intent.CATEGORY_HOME)
         startmain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
         startActivity(startmain)
     }*/
    fun login(view: View ) {
        loginUser()
    }
    private fun loginUser() {
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        //inicio de sesión
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful)
                    goHome(email, "email")
                else  {
                    if (lyTerms.visibility == View.INVISIBLE) {
                        lyTerms.visibility = View.VISIBLE
                        etPassword2.visibility = View.VISIBLE
                    }
                    else {
                        var cbAcept = findViewById<CheckBox>(R.id.cbAcept)
                        if (cbAcept.isChecked) {

                            register()
                        }
                    }


                }
            }
    }
    private fun goHome(email: String, provider: String) {
        useremail = email
        providerSession = provider
        val homeIntent = Intent(this, HomeActivity :: class.java)
        startActivity(homeIntent)
    }
    private fun register() {
        //etEmail = findViewById(R.id.emailEditText)
        //etPassword = findViewById(R.id.passwordEditText)

        email = etEmail.text.toString()
        password = etPassword.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    //si se ha creado bien el usuario lo vamos a guardar en la base de datos
                    //como guardar una tupla en la base de datos
                    var dateRegister = SimpleDateFormat("dd/mm/yyyy").format(Date())
                    var dbRegister = FirebaseFirestore.getInstance()
                    dbRegister.collection("users").document(email).set(hashMapOf(
                        "user" to email,
                        "dateRegister" to dateRegister
                    ))
                    goHome(email, "email")
                }
                else {
                    Toast.makeText(this, "error ", Toast.LENGTH_SHORT).show()
                }
            }

        //FirebaseAuth.getInstance().createUserWithEmailAndPassword(etEmail, etPassword)
    }
    private fun showAlert() {
        val builder = AlertDialog.Builder (this)
        builder.setTitle("Error")
        //Se ha ocurrido un error autenticando el usuario
        builder.setMessage("An error occurred authenticating the user")
        builder.setPositiveButton("Accept ", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()


    }
    private fun showHome(email: String, provider: ProviderType) {
        val homeIntent = Intent(this, HomeActivity :: class.java).apply {
            putExtra("email",email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }
    fun goTerms(view: View) {
        val intent = Intent(this, TermsActivity::class.java)
    }
    fun forgotPassword(view: View) {
        resetPassword()
        //esta linbea es la que se usaria habitualmente
        //startActivity(Intent(this.ForgotPasswordActivity::class.java))
    }
    private fun resetPassword() {
        var email = etEmail.text.toString()
        if (!TextUtils.isEmpty(email)) {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) Toast.makeText(this, "Email sent to $email", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this, "The user with this email was not found", Toast.LENGTH_SHORT).show()
                }
        }
        else Toast.makeText(this, "enter a valid email", Toast.LENGTH_SHORT).show()
    }
    //metodo para manejar el correo electronico
    private fun manageButtonLogin() {
        //posibilidad de activar o desactivar el boton login
        val btLogin = findViewById<Button>(R.id.loginButton)

        //obtener texto de los EditText
        email = etEmail.text.toString()
        password = etPassword.text.toString()
        Log.d("manageButtonLogin", "Email: $email")
        Log.d("manageButtonLogin", "Password: $password")
        //validar y actualizar el estado del boton
        if (TextUtils.isEmpty(password) || !ValidateEmail.isEmail(email)) {
            btLogin.setBackgroundColor(ContextCompat.getColor(this,  R.color.gray))
            btLogin.isEnabled = false
        } else {
            btLogin.setBackgroundColor(ContextCompat.getColor(this,  R.color.teal_700))
            btLogin.isEnabled = true
        }
    }
    //metodo para menajr la contraseña
    private fun managePassword() {
        var btlogin = findViewById<Button>(R.id.loginButton)
        password = etPassword.text.toString()
        password2 = etPassword2.text.toString()
        //comprobar que los 2 son iguales
        if (password == password2) {

            btlogin.isEnabled = true
        } else {
            btlogin.isEnabled = false
        }
    }
    private fun clickEye() {
        etPassword.setOnClickListener(View.OnClickListener {
            if (!isVisible) {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance())
                etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0)
                isVisible = true
                ///aqui puedes cambiar el texto del boton, o textview, o cambiar la imagen de un imageView.
            } else {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0)
                isVisible = false
                ///aqui puedes cambiar el texto del boton, o textview, o cambiar la imagen de un imageView.
            }
        })
    }
    //ojo del etPassword2
    private fun clickEye2() {
        etPassword.setOnClickListener(View.OnClickListener {
            if (!isVisible) {
                etPassword2.setTransformationMethod(PasswordTransformationMethod.getInstance())
                etPassword2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0)
                isVisible2 = true
                ///aqui puedes cambiar el texto del boton, o textview, o cambiar la imagen de un imageView.
            } else {
                etPassword2.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                etPassword2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0)
                isVisible2 = false
                ///aqui puedes cambiar el texto del boton, o textview, o cambiar la imagen de un imageView.
            }
        })
    }

    //metodo de prueba para ver si funciona la verificacion
    private fun startSignIn() {
        // Initiate sign in with custom token
        // [START sign_in_custom]

        customToken?.let {
            auth.signInWithCustomToken(it)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCustomToken:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCustomToken:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        updateUI(null)
                    }
                }
        }
        // [END sign_in_custom]
    }
    private fun updateUI(user: FirebaseUser?) {
    }



}