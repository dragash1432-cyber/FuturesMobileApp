package com.futuresflow.app

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class TelegramClient {
    private val client = OkHttpClient.Builder().callTimeout(15, TimeUnit.SECONDS).build()
    fun send(token: String, chatId: String, message: String): Boolean {
        if (token.isBlank() || chatId.isBlank()) return false
        val body = FormBody.Builder().add("chat_id", chatId).add("text", message).build()
        val request = Request.Builder().url("https://api.telegram.org/bot$token/sendMessage").post(body).build()
        return try { client.newCall(request).execute().use { it.isSuccessful } } catch (_: Exception) { false }
    }
}
