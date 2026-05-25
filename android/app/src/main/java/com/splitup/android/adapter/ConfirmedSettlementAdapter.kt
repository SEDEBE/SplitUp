package com.splitup.android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.splitup.android.databinding.ItemConfirmedSettlementBinding
import com.splitup.android.model.ConfirmedSettlementDto

class ConfirmedSettlementAdapter(
    private val onUndo: (ConfirmedSettlementDto) -> Unit
) : ListAdapter<ConfirmedSettlementDto, ConfirmedSettlementAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemConfirmedSettlementBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemConfirmedSettlementBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = getItem(position)
        holder.binding.tvConfParties.text = "${c.fromName}  →  ${c.toName}"
        holder.binding.tvConfAmount.text  = "€ ${"%.2f".format(c.amount)}"
        holder.binding.tvConfDate.text    = c.settledAt
        holder.binding.btnUndo.setOnClickListener { onUndo(c) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ConfirmedSettlementDto>() {
            override fun areItemsTheSame(a: ConfirmedSettlementDto, b: ConfirmedSettlementDto) =
                a.id == b.id
            override fun areContentsTheSame(a: ConfirmedSettlementDto, b: ConfirmedSettlementDto) =
                a == b
        }
    }
}
