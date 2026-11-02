package com.futuresflow.app

import android.content.Context

class Prefs(context: Context) {
    private val p = context.getSharedPreferences("futures_flow", Context.MODE_PRIVATE)
    var telegramToken: String
        get() = p.getString("tg_token", "") ?: ""
        set(v) { p.edit().putString("tg_token", v.trim()).apply() }
    var telegramChatId: String
        get() = p.getString("tg_chat", "") ?: ""
        set(v) { p.edit().putString("tg_chat", v.trim()).apply() }
    var enabled: Boolean
        get() = p.getBoolean("enabled", false)
        set(v) { p.edit().putBoolean("enabled", v).apply() }
}
