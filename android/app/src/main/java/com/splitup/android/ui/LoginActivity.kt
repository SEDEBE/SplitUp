package com.splitup.android.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.splitup.android.databinding.ActivityLoginBinding
import com.splitup.android.model.UserDto
import com.splitup.android.network.RetrofitClient
import com.splitup.android.util.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private var registerMode = false

    // Reemplaza esto con tu Web Client ID de Google Cloud Console
    private val WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    doGoogleLogin(idToken, account.email ?: "", account.displayName ?: "")
                } else {
                    toast("No se pudo obtener el token de Google")
                }
            } catch (e: ApiException) {
                toast("Error de Google Sign-In: ${e.statusCode}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnAction.setOnClickListener { onAction() }
        binding.tvToggle.setOnClickListener { toggleMode() }
        binding.btnGoogle.setOnClickListener { launchGoogleSignIn() }
    }

    private fun onAction() {
        val email    = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (email.isBlank())    { toast("Introduce tu email"); return }
        if (password.isBlank()) { toast("Introduce tu contraseña"); return }

        setLoading(true)
        lifecycleScope.launch {
            try {
                if (registerMode) {
                    val name = binding.etName.text.toString().trim()
                    if (name.isBlank()) { setLoading(false); toast("Introduce tu nombre"); return@launch }
                    val resp = RetrofitClient.api.register(
                        mapOf("name" to name, "email" to email, "password" to password)
                    )
                    handleUserResponse(resp.body(), resp.isSuccessful)
                } else {
                    val resp = RetrofitClient.api.login(
                        mapOf("email" to email, "password" to password)
                    )
                    handleUserResponse(resp.body(), resp.isSuccessful)
                }
            } catch (e: Exception) {
                toast("Error de conexión. ¿Está el servidor arrancado?")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun launchGoogleSignIn() {
        if (WEB_CLIENT_ID == "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com") {
            toast("Google Sign-In necesita configuración (ver README)")
            return
        }
        val signInIntent = googleSignInClient.signInIntent
        googleLauncher.launch(signInIntent)
    }

    private fun doGoogleLogin(idToken: String, email: String, name: String) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.loginWithGoogle(
                    mapOf("idToken" to idToken, "email" to email, "name" to name)
                )
                handleUserResponse(resp.body(), resp.isSuccessful)
            } catch (e: Exception) {
                toast("Error de conexión con Google")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun handleUserResponse(user: UserDto?, success: Boolean) {
        if (success && user != null) {
            SessionManager.saveUser(this, user)
            startActivity(Intent(this, GroupListActivity::class.java))
            finish()
        } else {
            if (!registerMode) toast("Email o contraseña incorrectos")
            else toast("No se pudo registrar. El email puede ya existir.")
        }
    }

    private fun toggleMode() {
        registerMode = !registerMode
        if (registerMode) {
            binding.tvTitle.text    = "Crear cuenta"
            binding.btnAction.text  = "Registrar"
            binding.tvToggle.text   = "¿Ya tienes cuenta? Inicia sesión"
            binding.tilName.visibility = View.VISIBLE
        } else {
            binding.tvTitle.text    = "Iniciar sesión"
            binding.btnAction.text  = "Entrar"
            binding.tvToggle.text   = "¿No tienes cuenta? Regístrate"
            binding.tilName.visibility = View.GONE
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnAction.isEnabled    = !loading
        binding.btnGoogle.isEnabled    = !loading
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
