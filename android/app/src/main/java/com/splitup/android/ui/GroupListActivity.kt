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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.tvUserName.text = "Hola, ${user.name}"

        adapter = GroupAdapter { group -> openGroup(group) }
        binding.recyclerGroups.layoutManager = LinearLayoutManager(this)
        binding.recyclerGroups.adapter = adapter

        binding.fabNewGroup.setOnClickListener { showCreateGroupDialog() }
        binding.fabJoinGroup.setOnClickListener { showJoinGroupDialog() }

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
                    toast("Error ${resp.code()}: ${resp.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                toast("Error: ${e.message}")
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
                else toast("Error ${resp.code()}: ${resp.errorBody()?.string()}")
            } catch (e: Exception) { toast("Error: ${e.message}") }
        }
    }

    private fun showJoinGroupDialog() {
        val input = android.widget.EditText(this).apply {
            hint = "Código de invitación"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }
        AlertDialog.Builder(this)
            .setTitle("Unirse a un grupo")
            .setMessage("Introduce el código de invitación que te compartieron:")
            .setView(input)
            .setPositiveButton("Unirse") { _, _ ->
                val code = input.text.toString().trim()
                if (code.isBlank()) { toast("Introduce un código"); return@setPositiveButton }
                joinGroup(code)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun joinGroup(inviteCode: String) {
        val userId = SessionManager.getUser(this)?.id ?: return
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.joinByCode(
                    mapOf("inviteCode" to inviteCode, "userId" to userId)
                )
                if (resp.isSuccessful) {
                    toast("Te has unido al grupo")
                    loadGroups()
                } else {
                    val errBody = resp.errorBody()?.string() ?: "(vacío)"
                    showJoinError("Error HTTP ${resp.code()}", errBody)
                }
            } catch (e: Exception) {
                showJoinError("Error de red / parseo", "${e.javaClass.simpleName}: ${e.message}")
            }
        }
    }

    private fun showJoinError(title: String, message: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun openGroup(group: GroupDto) {
        val intent = Intent(this, GroupDetailActivity::class.java)
        intent.putExtra("group", group)
        startActivity(intent)
    }

    override fun onResume() { super.onResume(); loadGroups() }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
