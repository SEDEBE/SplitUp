package com.splitup.android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.splitup.android.databinding.ItemMemberBinding
import com.splitup.android.model.MemberDto

class MemberAdapter : ListAdapter<MemberDto, MemberAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = getItem(position)
        holder.binding.tvMemberInitial.text = m.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        holder.binding.tvMemberName.text  = m.name
        holder.binding.tvMemberEmail.text = m.email
        holder.binding.tvMemberRole.text  = when (m.role) {
            "OWNER" -> "Propietario"
            "ADMIN" -> "Admin"
            else    -> "Miembro"
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MemberDto>() {
            override fun areItemsTheSame(a: MemberDto, b: MemberDto) = a.userId == b.userId
            override fun areContentsTheSame(a: MemberDto, b: MemberDto) = a == b
        }
    }
}
