package com.splitup.android.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.splitup.android.databinding.ActivityCreateExpenseBinding
import com.splitup.android.model.GroupDto
import com.splitup.android.model.MemberDto
import com.splitup.android.network.RetrofitClient
import com.splitup.android.util.SessionManager
import kotlinx.coroutines.launch
import java.time.LocalDate

class CreateExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateExpenseBinding
    private lateinit var group: GroupDto
    private var members: List<MemberDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Nuevo gasto"

        @Suppress("DEPRECATION")
        group = intent.getSerializableExtra("group") as? GroupDto ?: run { finish(); return }

        loadMembers()

        binding.btnSave.setOnClickListener { onSave() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun loadMembers() {
        val userId = SessionManager.getUser(this)?.id ?: return
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.getMembers(group.id, userId)
                members = resp.body() ?: emptyList()
                val names = members.map { it.name }
                binding.spinnerPayer.adapter = ArrayAdapter(
                    this@CreateExpenseActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    names)
                // Preseleccionar el usuario actual
                val idx = members.indexOfFirst { it.userId == userId }
                if (idx >= 0) binding.spinnerPayer.setSelection(idx)
            } catch (e: Exception) {
                toast("Error cargando miembros")
            }
        }
    }

    private fun onSave() {
        val title = binding.etTitle.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()

        if (title.isBlank())    { toast("Introduce el título"); return }
        if (amountStr.isBlank()) { toast("Introduce el importe"); return }

        val amount = amountStr.replace(",", ".").toDoubleOrNull()
            ?: run { toast("Importe inválido"); return }

        val payerIdx = binding.spinnerPayer.selectedItemPosition
        if (payerIdx < 0 || payerIdx >= members.size) { toast("Selecciona el pagador"); return }
        val payerId = members[payerIdx].userId

        val userId = SessionManager.getUser(this)?.id ?: return

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val body: Map<String, Any> = mapOf(
                    "requesterId"   to userId,
                    "payerId"       to payerId,
                    "title"         to title,
                    "amount"        to amount,
                    "date"          to LocalDate.now().toString(),
                    "splitMode"     to "EQUAL"
                )
                val resp = RetrofitClient.api.createExpense(group.id, body)
                if (resp.isSuccessful) {
                    toast("Gasto guardado")
                    finish()
                } else {
                    toast("Error al guardar el gasto")
                }
            } catch (e: Exception) {
                toast("Error de conexión")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
