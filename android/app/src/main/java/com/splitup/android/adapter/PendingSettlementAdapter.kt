package com.splitup.android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.splitup.android.databinding.ItemPendingSettlementBinding
import com.splitup.android.model.SettlementDto

class PendingSettlementAdapter(
    private val onMarkPaid: (SettlementDto) -> Unit
) : ListAdapter<SettlementDto, PendingSettlementAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemPendingSettlementBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemPendingSettlementBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = getItem(position)
        holder.binding.tvPendParties.text = "${s.fromName}  →  ${s.toName}"
        holder.binding.tvPendAmount.text  = "€ ${"%.2f".format(s.amount)}"
        holder.binding.btnMarkPaid.setOnClickListener { onMarkPaid(s) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SettlementDto>() {
            // Las sugerencias no tienen id; dos registros son iguales si tienen los mismos usuarios
            override fun areItemsTheSame(a: SettlementDto, b: SettlementDto) =
                a.fromId == b.fromId && a.toId == b.toId
            override fun areContentsTheSame(a: SettlementDto, b: SettlementDto) = a == b
        }
    }
}
