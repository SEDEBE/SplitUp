package com.splitup.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.splitup.android.adapter.ExpenseAdapter
import com.splitup.android.adapter.MemberAdapter
import com.splitup.android.adapter.BalanceAdapter
import com.splitup.android.databinding.ActivityGroupDetailBinding
import com.splitup.android.model.GroupDto
import com.splitup.android.network.RetrofitClient
import com.splitup.android.util.SessionManager
import kotlinx.coroutines.launch

class GroupDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupDetailBinding
    private lateinit var group: GroupDto

    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var memberAdapter:  MemberAdapter
    private lateinit var balanceAdapter: BalanceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        group = intent.getSerializableExtra("group") as GroupDto
        supportActionBar?.title = group.name

        setupAdapters()
        setupTabs()

        binding.fabNewExpense.setOnClickListener {
            startActivity(Intent(this, CreateExpenseActivity::class.java).apply {
                putExtra("group", group)
            })
        }

        loadTab(0)
    }

    private fun setupAdapters() {
        expenseAdapter = ExpenseAdapter()
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

    private fun loadTab(position: Int) {
        val userId = SessionManager.getUser(this)?.id ?: return
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                when (position) {
                    0 -> {
                        val resp = RetrofitClient.api.getExpenses(group.id, userId)
                        expenseAdapter.submitList(resp.body() ?: emptyList())
                        binding.recyclerContent.adapter = expenseAdapter
                    }
                    1 -> {
                        val resp = RetrofitClient.api.getMembers(group.id, userId)
                        memberAdapter.submitList(resp.body() ?: emptyList())
                        binding.recyclerContent.adapter = memberAdapter
                    }
                    2 -> {
                        val resp = RetrofitClient.api.getBalances(group.id, userId)
                        balanceAdapter.submitList(resp.body() ?: emptyList())
                        binding.recyclerContent.adapter = balanceAdapter
                    }
                    3 -> {
                        val resp = RetrofitClient.api.getSettlements(group.id, userId)
                        val items = (resp.body() ?: emptyList()).map { s ->
                            "${s.fromName}  →  ${s.toName}   € ${"%.2f".format(s.amount)}"
                        }
                        showSettlementsInRecycler(items)
                        return@launch
                    }
                }
                binding.tvEmpty.visibility = View.GONE
            } catch (e: Exception) {
                Toast.makeText(this@GroupDetailActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showSettlementsInRecycler(items: List<String>) {
        val adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
                val tv = android.widget.TextView(parent.context).apply {
                    setPadding(48, 32, 48, 32)
                    textSize = 16f
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
                }
                return object : androidx.recyclerview.widget.RecyclerView.ViewHolder(tv) {}
            }
            override fun getItemCount() = items.size
            override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
                (holder.itemView as android.widget.TextView).text = items[position]
            }
        }
        binding.recyclerContent.adapter = adapter
        binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        if (items.isEmpty()) binding.tvEmpty.text = "¡Todo saldado! No hay deudas pendientes."
    }

    override fun onResume() { super.onResume(); loadTab(binding.tabLayout.selectedTabPosition) }
}
