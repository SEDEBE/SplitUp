package com.splitup.android.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.splitup.android.R
import com.splitup.android.adapter.BalanceAdapter
import com.splitup.android.adapter.ConfirmedSettlementAdapter
import com.splitup.android.adapter.ExpenseAdapter
import com.splitup.android.adapter.MemberAdapter
import com.splitup.android.adapter.PendingSettlementAdapter
import com.splitup.android.databinding.ActivityGroupDetailBinding
import com.splitup.android.model.ConfirmedSettlementDto
import com.splitup.android.model.ConfirmSettlementRequest
import com.splitup.android.model.GroupDto
import com.splitup.android.model.SettlementDto
import com.splitup.android.network.RetrofitClient
import com.splitup.android.util.SessionManager
import kotlinx.coroutines.launch

class GroupDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupDetailBinding
    private lateinit var group: GroupDto

    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var memberAdapter:  MemberAdapter
    private lateinit var balanceAdapter: BalanceAdapter

    // 0 = Pendientes, 1 = Historial
    private var settleSubTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        group = intent.getSerializableExtra("group") as? GroupDto ?: run { finish(); return }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = group.name
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupAdapters()
        setupTabs()
        setupSettleSubToggle()
        setupMembersActions()

        binding.fabNewExpense.setOnClickListener {
            startActivity(Intent(this, CreateExpenseActivity::class.java).apply {
                putExtra("group", group)
            })
        }

        loadTab(0)
    }

    private fun setupAdapters() {
        expenseAdapter = ExpenseAdapter { expense -> confirmDeleteExpense(expense) }
        memberAdapter  = MemberAdapter()
        balanceAdapter = BalanceAdapter()
        binding.recyclerContent.layoutManager = LinearLayoutManager(this)
    }

    private fun setupTabs() {
        listOf("Gastos", "Miembros", "Balances", "Liquidar").forEach { tab ->
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(tab))
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { loadTab(tab.position) }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) { loadTab(tab.position) }
        })
    }

    private fun setupMembersActions() {
        binding.btnCopyCode.setOnClickListener {
            val code = group.inviteCode ?: return@setOnClickListener
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("invite_code", code))
            Toast.makeText(this, "Código copiado", Toast.LENGTH_SHORT).show()
        }
        binding.btnAddMember.setOnClickListener { showAddMemberDialog() }
        binding.btnDeleteGroup.setOnClickListener { confirmDeleteGroup() }
    }

    private fun showAddMemberDialog() {
        val input = android.widget.EditText(this).apply {
            hint = "Email del miembro"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        AlertDialog.Builder(this)
            .setTitle("Añadir miembro")
            .setView(input)
            .setPositiveButton("Añadir") { _, _ ->
                val email = input.text.toString().trim()
                if (email.isNotBlank()) doAddMember(email)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun doAddMember(email: String) {
        val userId = SessionManager.getUser(this)?.id ?: return
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.addMember(
                    group.id,
                    mapOf("email" to email, "requesterId" to userId)
                )
                if (resp.isSuccessful) {
                    Toast.makeText(this@GroupDetailActivity, "Miembro añadido", Toast.LENGTH_SHORT).show()
                    loadTab(1)
                } else {
                    Toast.makeText(this@GroupDetailActivity, "No se pudo añadir al miembro", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@GroupDetailActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDeleteExpense(expense: com.splitup.android.model.ExpenseDto) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar gasto")
            .setMessage("¿Eliminar \"${expense.title}\"?\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> doDeleteExpense(expense) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun doDeleteExpense(expense: com.splitup.android.model.ExpenseDto) {
        val userId = SessionManager.getUser(this)?.id ?: return
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.deleteExpense(group.id, expense.id, userId)
                if (resp.isSuccessful) {
                    Toast.makeText(this@GroupDetailActivity, "Gasto eliminado", Toast.LENGTH_SHORT).show()
                    loadTab(0)
                } else {
                    Toast.makeText(this@GroupDetailActivity, "Sin permiso para eliminar este gasto", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@GroupDetailActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDeleteGroup() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar grupo")
            .setMessage("¿Eliminar \"${group.name}\" y todos sus datos?\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> doDeleteGroup() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun doDeleteGroup() {
        val userId = SessionManager.getUser(this)?.id ?: return
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.deleteGroup(group.id, userId)
                if (resp.isSuccessful) {
                    Toast.makeText(this@GroupDetailActivity, "Grupo eliminado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@GroupDetailActivity, "No tienes permiso para eliminar este grupo", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@GroupDetailActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSettleSubToggle() {
        // Seleccionar "Pendientes" como estado inicial
        binding.btnSubPending.isChecked = true

        binding.settleSubToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            settleSubTab = when (checkedId) {
                R.id.btnSubPending -> 0
                R.id.btnSubHistory -> 1
                else -> 0
            }
            loadTab(3)
        }
    }

    private fun loadTab(position: Int) {
        val userId = SessionManager.getUser(this)?.id ?: return

        // Controles específicos de cada pestaña
        binding.settleSubToggle.visibility = if (position == 3) View.VISIBLE else View.GONE
        val membersVisible = if (position == 1) View.VISIBLE else View.GONE
        binding.cardInvite.visibility     = membersVisible
        binding.btnAddMember.visibility   = membersVisible
        binding.btnDeleteGroup.visibility = membersVisible
        if (position == 1) {
            binding.tvInviteCode.text = group.inviteCode ?: "—"
        }

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                when (position) {
                    0 -> {
                        val resp = RetrofitClient.api.getExpenses(group.id, userId)
                        expenseAdapter.submitList(resp.body() ?: emptyList())
                        binding.recyclerContent.adapter = expenseAdapter
                        binding.tvEmpty.visibility = View.GONE
                    }
                    1 -> {
                        val resp = RetrofitClient.api.getMembers(group.id, userId)
                        memberAdapter.submitList(resp.body() ?: emptyList())
                        binding.recyclerContent.adapter = memberAdapter
                        binding.tvEmpty.visibility = View.GONE
                    }
                    2 -> {
                        val resp = RetrofitClient.api.getBalances(group.id, userId)
                        balanceAdapter.submitList(resp.body() ?: emptyList())
                        binding.recyclerContent.adapter = balanceAdapter
                        binding.tvEmpty.visibility = View.GONE
                    }
                    3 -> {
                        loadSettlementsSubTab(userId)
                        return@launch
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@GroupDetailActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private suspend fun loadSettlementsSubTab(userId: Long) {
        if (settleSubTab == 0) {
            val list = RetrofitClient.api.getSettlements(group.id, userId).body() ?: emptyList()
            val adapter = PendingSettlementAdapter { s -> confirmSettlementDialog(s) }
            adapter.submitList(list)
            binding.recyclerContent.adapter = adapter
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            if (list.isEmpty()) binding.tvEmpty.text = "¡Todo saldado! No hay deudas pendientes."
        } else {
            val list = RetrofitClient.api.getConfirmedSettlements(group.id, userId).body()
                ?: emptyList()
            val adapter = ConfirmedSettlementAdapter { c -> unconfirmSettlementDialog(c) }
            adapter.submitList(list)
            binding.recyclerContent.adapter = adapter
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            if (list.isEmpty()) binding.tvEmpty.text = "Aún no se ha confirmado ningún pago."
        }
    }

    // ── Diálogos de confirmación ──────────────────────────────────────────────

    private fun confirmSettlementDialog(s: SettlementDto) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar pago")
            .setMessage(
                "¿Confirmas que ${s.fromName} ha pagado " +
                "${"%.2f".format(s.amount)} € a ${s.toName}?"
            )
            .setPositiveButton("Confirmar") { _, _ -> doConfirm(s) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun unconfirmSettlementDialog(c: ConfirmedSettlementDto) {
        AlertDialog.Builder(this)
            .setTitle("Deshacer pago")
            .setMessage(
                "¿Deshacer el pago de ${c.fromName} → ${c.toName} " +
                "por ${"%.2f".format(c.amount)} €?"
            )
            .setPositiveButton("Confirmar") { _, _ -> doUnconfirm(c) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ── Llamadas al API ───────────────────────────────────────────────────────

    private fun doConfirm(s: SettlementDto) {
        val userId = SessionManager.getUser(this)?.id ?: return
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.confirmSettlement(
                    group.id,
                    ConfirmSettlementRequest(
                        fromUserId  = s.fromId,
                        toUserId    = s.toId,
                        amount      = s.amount,
                        requesterId = userId
                    )
                )
                if (resp.isSuccessful) {
                    Toast.makeText(
                        this@GroupDetailActivity, "Pago confirmado", Toast.LENGTH_SHORT
                    ).show()
                    loadTab(3)
                } else {
                    Toast.makeText(
                        this@GroupDetailActivity, "Error al confirmar pago", Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@GroupDetailActivity, "Error de conexión", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun doUnconfirm(c: ConfirmedSettlementDto) {
        val userId = SessionManager.getUser(this)?.id ?: return
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.unconfirmSettlement(group.id, c.id, userId)
                if (resp.isSuccessful) {
                    Toast.makeText(
                        this@GroupDetailActivity, "Pago deshecho", Toast.LENGTH_SHORT
                    ).show()
                    loadTab(3)
                } else {
                    Toast.makeText(
                        this@GroupDetailActivity, "Error al deshacer pago", Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@GroupDetailActivity, "Error de conexión", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTab(binding.tabLayout.selectedTabPosition)
    }
}
