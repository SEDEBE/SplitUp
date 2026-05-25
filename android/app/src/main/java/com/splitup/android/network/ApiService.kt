package com.splitup.android.network

import com.splitup.android.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Usuarios ──────────────────────────────────────────────────────────────

    @POST("api/users/login")
    suspend fun login(@Body body: Map<String, String>): Response<UserDto>

    @POST("api/users/register")
    suspend fun register(@Body body: Map<String, String>): Response<UserDto>

    // ── Grupos ────────────────────────────────────────────────────────────────

    @GET("api/groups")
    suspend fun getGroups(@Query("userId") userId: Long): Response<List<GroupDto>>

    @POST("api/groups")
    suspend fun createGroup(@Body body: Map<String, Any>): Response<GroupDto>

    @DELETE("api/groups/{groupId}")
    suspend fun deleteGroup(
        @Path("groupId") groupId: Long,
        @Query("requesterId") requesterId: Long
    ): Response<Unit>

    @POST("api/groups/join")
    suspend fun joinByCode(@Body body: Map<String, Any>): Response<GroupDto>

    @GET("api/groups/{groupId}/members")
    suspend fun getMembers(
        @Path("groupId") groupId: Long,
        @Query("requesterId") requesterId: Long
    ): Response<List<MemberDto>>

    @POST("api/groups/{groupId}/members")
    suspend fun addMember(
        @Path("groupId") groupId: Long,
        @Body body: Map<String, Any>
    ): Response<Unit>

    // ── Gastos ────────────────────────────────────────────────────────────────

    @GET("api/groups/{groupId}/expenses")
    suspend fun getExpenses(
        @Path("groupId") groupId: Long,
        @Query("userId") userId: Long
    ): Response<List<ExpenseDto>>

    @POST("api/groups/{groupId}/expenses")
    suspend fun createExpense(
        @Path("groupId") groupId: Long,
        @Body body: Map<String, Any>
    ): Response<ExpenseDto>

    @DELETE("api/groups/{groupId}/expenses/{expenseId}")
    suspend fun deleteExpense(
        @Path("groupId") groupId: Long,
        @Path("expenseId") expenseId: Long,
        @Query("userId") userId: Long
    ): Response<Unit>

    // ── Balances y liquidaciones ──────────────────────────────────────────────

    @GET("api/groups/{groupId}/balances")
    suspend fun getBalances(
        @Path("groupId") groupId: Long,
        @Query("userId") userId: Long
    ): Response<List<BalanceDto>>

    @GET("api/groups/{groupId}/settlements")
    suspend fun getSettlements(
        @Path("groupId") groupId: Long,
        @Query("userId") userId: Long
    ): Response<List<SettlementDto>>

    @GET("api/groups/{groupId}/settlements/confirmed")
    suspend fun getConfirmedSettlements(
        @Path("groupId") groupId: Long,
        @Query("userId") userId: Long
    ): Response<List<ConfirmedSettlementDto>>

    @POST("api/groups/{groupId}/settlements")
    suspend fun confirmSettlement(
        @Path("groupId") groupId: Long,
        @Body body: ConfirmSettlementRequest
    ): Response<ConfirmedSettlementDto>

    @DELETE("api/groups/{groupId}/settlements/{settlementId}")
    suspend fun unconfirmSettlement(
        @Path("groupId") groupId: Long,
        @Path("settlementId") settlementId: Long,
        @Query("requesterId") requesterId: Long
    ): Response<Unit>
}
