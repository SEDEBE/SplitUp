package com.splitup.android.model

import java.io.Serializable

data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val avatarUrl: String?
) : Serializable

data class GroupDto(
    val id: Long,
    val name: String,
    val description: String?,
    val createdById: Long,
    val inviteCode: String? = null
) : Serializable

data class MemberDto(
    val userId: Long,
    val name: String,
    val email: String,
    val role: String
) : Serializable

data class ExpenseDto(
    val id: Long,
    val groupId: Long,
    val payerId: Long,
    val payerName: String,
    val title: String,
    val totalAmount: Double,
    val currency: String,
    val expenseDate: String,
    val splitMode: String,
    val note: String?
) : Serializable

data class BalanceDto(
    val userId: Long,
    val userName: String,
    val balance: Double,
    val status: String          // "ACREEDOR", "DEUDOR" o "SALDADO"
) : Serializable

data class SettlementDto(
    val fromId: Long,
    val fromName: String,
    val toId: Long,
    val toName: String,
    val amount: Double
) : Serializable

data class ConfirmedSettlementDto(
    val id: Long,
    val fromUserId: Long,
    val fromName: String,
    val toUserId: Long,
    val toName: String,
    val amount: Double,
    val settledAt: String
) : Serializable

data class ConfirmSettlementRequest(
    val fromUserId: Long,
    val toUserId: Long,
    val amount: Double,
    val requesterId: Long
) : Serializable
