package com.splitup.android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.splitup.android.databinding.ItemGroupBinding
import com.splitup.android.model.GroupDto

class GroupAdapter(private val onClick: (GroupDto) -> Unit) :
    ListAdapter<GroupDto, GroupAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemGroupBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val group = getItem(position)
        holder.binding.tvGroupName.text = group.name
        holder.binding.tvGroupDesc.text = group.description ?: ""
        holder.itemView.setOnClickListener { onClick(group) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<GroupDto>() {
            override fun areItemsTheSame(a: GroupDto, b: GroupDto) = a.id == b.id
            override fun areContentsTheSame(a: GroupDto, b: GroupDto) = a == b
        }
    }
}
