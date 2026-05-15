package com.splitup.android.util

import android.content.Context
import com.splitup.android.model.UserDto

object SessionManager {

    private const val PREF = "splitup_session"

    fun saveUser(ctx: Context, user: UserDto) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putLong("userId", user.id)
            .putString("userName", user.name)
            .putString("userEmail", user.email)
            .apply()
    }

    fun getUser(ctx: Context): UserDto? {
        val prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val id = prefs.getLong("userId", -1L)
        if (id == -1L) return null
        return UserDto(id, prefs.getString("userName", "")!!, prefs.getString("userEmail", "")!!, null)
    }

    fun clear(ctx: Context) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
