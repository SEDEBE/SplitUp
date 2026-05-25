package com.splitup.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.splitup.android.databinding.ItemParticipantBinding
import com.splitup.android.model.MemberDto

class ParticipantAdapter(private val members: List<MemberDto>) :
    RecyclerView.Adapter<ParticipantAdapter.VH>() {

    private val checked = BooleanArray(members.size) { true }
    private val customAmounts = Array(members.size) { "" }

    var isCustomMode = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class VH(val binding: ItemParticipantBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemParticipantBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val member = members[position]
        holder.binding.cbParticipant.text = member.name
        holder.binding.cbParticipant.isChecked = checked[position]
        holder.binding.cbParticipant.setOnCheckedChangeListener { _, isChecked ->
            checked[holder.adapterPosition] = isChecked
        }
        holder.binding.tilCustomAmount.visibility = if (isCustomMode) View.VISIBLE else View.GONE
        if (isCustomMode) {
            holder.binding.etCustomAmount.setText(customAmounts[position])
            holder.binding.etCustomAmount.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val pos = holder.adapterPosition
                    if (pos != RecyclerView.NO_ID.toInt()) {
                        customAmounts[pos] = holder.binding.etCustomAmount.text?.toString() ?: ""
                    }
                }
            }
        }
    }

    override fun getItemCount() = members.size

    fun getSelectedParticipantIds(): List<Long> =
        members.filterIndexed { i, _ -> checked[i] }.map { it.userId }

    fun getCustomShares(): List<Map<String, Any>> =
        members.mapIndexed { i, m ->
            val amount = customAmounts[i].replace(",", ".").toDoubleOrNull() ?: 0.0
            Triple(checked[i], m.userId, amount)
        }
        .filter { (isChecked, _, amount) -> isChecked && amount > 0.0 }
        .map { (_, userId, amount) -> mapOf<String, Any>("userId" to userId, "amount" to amount) }
}
