package com.splitup.android.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.splitup.android.databinding.ItemBalanceBinding
import com.splitup.android.model.BalanceDto

class BalanceAdapter : ListAdapter<BalanceDto, BalanceAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemBalanceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemBalanceBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val b = getItem(position)
        holder.binding.tvBalanceUser.text = b.userName
        val sign = if (b.balance > 0.005) "+" else ""
        holder.binding.tvBalanceAmount.text = "$sign${"%.2f".format(b.balance)} €"
        holder.binding.tvBalanceAmount.setTextColor(when {
            b.balance > 0.005  -> Color.parseColor("#2E7D32")
            b.balance < -0.005 -> Color.parseColor("#C62828")
            else               -> Color.GRAY
        })
        holder.binding.tvBalanceStatus.text = when (b.status) {
            "ACREEDOR" -> "Acreedor"
            "DEUDOR"   -> "Deudor"
            else       -> "Saldado"
        }
        holder.binding.tvBalanceStatus.setTextColor(when (b.status) {
            "ACREEDOR" -> Color.parseColor("#2E7D32")
            "DEUDOR"   -> Color.parseColor("#C62828")
            else       -> Color.GRAY
        })
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<BalanceDto>() {
            override fun areItemsTheSame(a: BalanceDto, b: BalanceDto) = a.userId == b.userId
            override fun areContentsTheSame(a: BalanceDto, b: BalanceDto) = a == b
        }
    }
}
