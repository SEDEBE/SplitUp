package com.splitup.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.splitup.android.adapter.GroupAdapter
import com.splitup.android.databinding.ActivityGroupListBinding
import com.splitup.android.model.GroupDto
import com.splitup.android.network.RetrofitClient
import com.splitup.android.util.SessionManager
import kotlinx.coroutines.launch

class GroupListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupListBinding
    private lateinit var adapter: GroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = SessionManager.getUser(this) ?: run {
            startActivity(Intent(this, LoginActivity::class.java)); finish(); return
        }

        supportActionBar?.title = "Grupos de ${user.name}"

        adapter = GroupAdapter { group -> openGroup(group) }
        binding.recyclerGroups.layoutManager = LinearLayoutManager(this)
        binding.recyclerGroups.adapter = adapter

        binding.fabNewGroup.setOnClickListener { showCreateGroupDialog() }

        binding.btnLogout.setOnClickListener {
            SessionManager.clear(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        loadGroups()
    }

    private fun loadGroups() {
        val userId = SessionManager.getUser(this)?.id ?: return
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.getGroups(userId)
                if (resp.isSuccessful) {
                    adapter.submitList(resp.body() ?: emptyList())
                    binding.tvEmpty.visibility =
                        if (resp.body().isNullOrEmpty()) View.VISIBLE else View.GONE
                } else {
                    toast("Error cargando grupos")
                }
            } catch (e: Exception) {
                toast("Error de conexión")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showCreateGroupDialog() {
        val input = android.widget.EditText(this).apply { hint = "Nombre del grupo" }
        AlertDialog.Builder(this)
            .setTitle("Nuevo grupo")
            .setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isBlank()) { toast("Introduce un nombre"); return@setPositiveButton }
                createGroup(name)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun createGroup(name: String) {
        val userId = SessionManager.getUser(this)?.id ?: return
        lifecycleScope.launch {
            try {
                val body = mapOf<String, Any>("name" to name, "description" to "", "creatorId" to userId)
                val resp = RetrofitClient.api.createGroup(body)
                if (resp.isSuccessful) loadGroups()
                else toast("No se pudo crear el grupo")
            } catch (e: Exception) { toast("Error de conexión") }
        }
    }

    private fun openGroup(group: GroupDto) {
        val intent = Intent(this, GroupDetailActivity::class.java)
        intent.putExtra("group", group)
        startActivity(intent)
    }

    override fun onResume() { super.onResume(); loadGroups() }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
