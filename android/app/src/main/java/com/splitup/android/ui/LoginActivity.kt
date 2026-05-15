package com.splitup.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.splitup.android.databinding.ActivityLoginBinding
import com.splitup.android.model.UserDto
import com.splitup.android.network.RetrofitClient
import com.splitup.android.util.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var registerMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAction.setOnClickListener { onAction() }
        binding.tvToggle.setOnClickListener { toggleMode() }
    }

    private fun onAction() {
        val email = binding.etEmail.text.toString().trim()
        if (email.isBlank()) { toast("Introduce tu email"); return }

        setLoading(true)
        lifecycleScope.launch {
            try {
                if (registerMode) {
                    val name = binding.etName.text.toString().trim()
                    if (name.isBlank()) { setLoading(false); toast("Introduce tu nombre"); return@launch }
                    val resp = RetrofitClient.api.register(mapOf("name" to name, "email" to email))
                    handleUserResponse(resp.body(), resp.isSuccessful)
                } else {
                    val resp = RetrofitClient.api.login(mapOf("email" to email))
                    handleUserResponse(resp.body(), resp.isSuccessful)
                }
            } catch (e: Exception) {
                toast("Error de conexión. ¿Está el servidor arrancado?")
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
            if (!registerMode) toast("Usuario no encontrado. ¿Quieres registrarte?")
            else toast("No se pudo registrar. El email puede ya existir.")
        }
    }

    private fun toggleMode() {
        registerMode = !registerMode
        if (registerMode) {
            binding.tvTitle.text = "Crear cuenta"
            binding.btnAction.text = "Registrar"
            binding.tvToggle.text = "¿Ya tienes cuenta? Inicia sesión"
            binding.tilName.visibility = View.VISIBLE
        } else {
            binding.tvTitle.text = "Iniciar sesión"
            binding.btnAction.text = "Entrar"
            binding.tvToggle.text = "¿No tienes cuenta? Regístrate"
            binding.tilName.visibility = View.GONE
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnAction.isEnabled = !loading
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
