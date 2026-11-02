package com.futuresflow.app

import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class BinanceClient {
    private val http = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build()

    fun tradesSocket(symbol: String, listener: WebSocketListener): WebSocket {
        val request = Request.Builder().url("wss://fstream.binance.com/ws/${symbol.lowercase()}@aggTrade").build()
        return http.newWebSocket(request, listener)
    }

    fun openInterest(symbol: String): Double? = try {
        val body = get("https://fapi.binance.com/fapi/v1/openInterest?symbol=$symbol")
        JSONObject(body).optString("openInterest").toDoubleOrNull()
    } catch (_: Exception) { null }

    fun klines(symbol: String, interval: String, limit: Int = 2): Pair<Double, Double>? = try {
        val body = get("https://fapi.binance.com/fapi/v1/klines?symbol=$symbol&interval=$interval&limit=$limit")
        val a = JSONArray(body)
        if (a.length() < 2) null else {
            val previous = a.getJSONArray(a.length() - 2).getString(4).toDouble()
            val latest = a.getJSONArray(a.length() - 1).getString(4).toDouble()
            previous to latest
        }
    } catch (_: Exception) { null }

    private fun get(url: String): String {
        val request = Request.Builder().url(url).get().build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Binance HTTP ${response.code}")
            return response.body?.string() ?: error("Empty response")
        }
    }
}
