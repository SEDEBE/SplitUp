package com.splitup.android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.splitup.android.databinding.ItemExpenseBinding
import com.splitup.android.model.ExpenseDto

class ExpenseAdapter(
    private val onDeleteClick: (ExpenseDto) -> Unit = {}
) : ListAdapter<ExpenseDto, ExpenseAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = getItem(position)
        holder.binding.tvExpenseTitle.text  = e.title
        holder.binding.tvExpensePayer.text  = "Pagado por: ${e.payerName}"
        holder.binding.tvExpenseAmount.text = "€ ${"%.2f".format(e.totalAmount)}"
        holder.binding.tvExpenseDate.text   = e.expenseDate
        holder.binding.btnDeleteExpense.setOnClickListener { onDeleteClick(e) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ExpenseDto>() {
            override fun areItemsTheSame(a: ExpenseDto, b: ExpenseDto) = a.id == b.id
            override fun areContentsTheSame(a: ExpenseDto, b: ExpenseDto) = a == b
        }
    }
}
