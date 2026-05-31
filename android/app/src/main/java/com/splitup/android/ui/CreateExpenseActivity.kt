package com.splitup.android.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.splitup.android.R
import com.splitup.android.adapter.ParticipantAdapter
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
    private lateinit var participantAdapter: ParticipantAdapter
    private var members: List<MemberDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        group = intent.getSerializableExtra("group") as? GroupDto ?: run { finish(); return }

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.recyclerParticipants.layoutManager = LinearLayoutManager(this)

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
                    android.R.layout.simple_spinner_item,
                    names).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                val idx = members.indexOfFirst { it.userId == userId }
                if (idx >= 0) binding.spinnerPayer.setSelection(idx)

                participantAdapter = ParticipantAdapter(members)
                binding.recyclerParticipants.adapter = participantAdapter

                binding.rgSplitMode.setOnCheckedChangeListener { _, checkedId ->
                    val isCustom = checkedId == R.id.rbCustom
                    participantAdapter.isCustomMode = isCustom
                    binding.tvCustomHint.visibility = if (isCustom) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                toast("Error cargando miembros")
            }
        }
    }

    private fun onSave() {
        val title = binding.etTitle.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()

        if (title.isBlank())     { toast("Introduce el título"); return }
        if (amountStr.isBlank()) { toast("Introduce el importe"); return }

        val amount = amountStr.replace(",", ".").toDoubleOrNull()
            ?: run { toast("Importe inválido"); return }

        val payerIdx = binding.spinnerPayer.selectedItemPosition
        if (payerIdx < 0 || payerIdx >= members.size) { toast("Selecciona el pagador"); return }
        val payerId = members[payerIdx].userId

        val userId = SessionManager.getUser(this)?.id ?: return
        val isCustom = binding.rgSplitMode.checkedRadioButtonId == R.id.rbCustom

        val body = mutableMapOf<String, Any>(
            "requesterId" to userId,
            "payerId"     to payerId,
            "title"       to title,
            "amount"      to amount,
            "date"        to LocalDate.now().toString(),
            "splitMode"   to if (isCustom) "CUSTOM" else "EQUAL"
        )

        if (!::participantAdapter.isInitialized) { toast("Espera a que carguen los miembros"); return }

        if (isCustom) {
            val shares = participantAdapter.getCustomShares()
            if (shares.isEmpty()) { toast("Introduce el importe de al menos un participante"); return }
            body["participantIds"] = shares.map { it["userId"] as Long }
            body["shares"] = shares
        } else {
            val selectedIds = participantAdapter.getSelectedParticipantIds()
            if (selectedIds.isNotEmpty()) body["participantIds"] = selectedIds
        }

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
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
