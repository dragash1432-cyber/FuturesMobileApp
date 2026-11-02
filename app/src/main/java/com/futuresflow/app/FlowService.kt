package com.futuresflow.app

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class FlowService : Service() {
    private val binance = BinanceClient()
    private val engine = SignalEngine()
    private val telegram = TelegramClient()
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var socket: WebSocket? = null
    private var oiBaseline: Double? = null
    private var cvd = 0.0
    private var lastTradeId = AtomicLong(-1)
    private var lastAlertAt = 0L
    private val symbol = "BTCUSDT" // Initial watch symbol; multi-symbol scanner is a separate expansion.

    override fun onCreate() {
        super.onCreate()
        startForeground(41, notification("Binance Futures akışı izleniyor"))
        executor.scheduleAtFixedRate({ refreshOiBaseline() }, 0, 60, TimeUnit.SECONDS)
        connect()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null

    private fun connect() {
        socket?.cancel()
        socket = binance.tradesSocket(symbol, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) { processTrade(text) }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                executor.schedule({ connect() }, 5, TimeUnit.SECONDS)
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                executor.schedule({ connect() }, 3, TimeUnit.SECONDS)
            }
        })
    }

    private fun refreshOiBaseline() {
        val current = binance.openInterest(symbol) ?: return
        val previous = oiBaseline
        oiBaseline = current
        if (previous == null || previous == 0.0) return
        val change = (current - previous) / previous * 100.0
        updateStatus("OI son değişim: ${"%.3f".format(change)}%")
    }

    private fun processTrade(raw: String) {
        try {
            val j = JSONObject(raw)
            val id = j.getLong("a")
            if (id <= lastTradeId.get()) return
            lastTradeId.set(id)
            val price = j.getString("p").toDouble()
            val qty = j.getString("q").toDouble()
            val quote = price * qty
            val buyerIsMaker = j.getBoolean("m")
            val aggressor = if (buyerIsMaker) "SHORT" else "LONG"
            cvd += if (aggressor == "LONG") quote else -quote
            if (quote < SignalEngine.MIN_TRADE_USDT) return
            executor.execute { evaluateAndNotify(FlowTrade(symbol, price, qty, quote, aggressor, id, j.optLong("T"))) }
        } catch (_: Exception) { }
    }

    private fun evaluateAndNotify(trade: FlowTrade) {
        val currentOi = binance.openInterest(symbol) ?: return
        val baseOi = oiBaseline ?: currentOi.also { oiBaseline = it }
        if (baseOi == 0.0) return
        val oiChange = (currentOi - baseOi) / baseOi * 100.0
        val frames = listOf("5m", "15m", "30m", "1h", "4h").associateWith { binance.klines(symbol, it) }
        val signal = engine.evaluate(symbol, trade, frames, oiChange, cvd) ?: return
        val now = System.currentTimeMillis()
        if (now - lastAlertAt < 60_000) return
        val prefs = Prefs(this)
        val msg = buildString {
            appendLine("FUTURES FLOW | ${signal.symbol}")
            appendLine("Gerçekleşen işlem: ${"%.2f".format(signal.quoteValue)} USDT")
            appendLine("Agresör: ${signal.aggressor}")
            appendLine("Zaman dilimleri:")
            signal.frames.forEach { appendLine("${it.interval}: ${it.direction} (${"%.3f".format(it.changePct)}%)") }
            appendLine("OI değişimi: ${"%.4f".format(signal.oiChangePct)}%")
            appendLine("CVD: ${"%.2f".format(signal.cvd)} USDT (oturum birikimi)")
            appendLine("Giriş referansı: ${signal.entry}")
            appendLine("TP1: ${signal.tp1}")
            appendLine("TP2: ${signal.tp2}")
            appendLine("Geçersizleşme: ${signal.invalidation}")
            append("Not: hedefler ilk sürümde %0,3 fiyat mesafesi proxy'sidir.")
        }
        if (telegram.send(prefs.telegramToken, prefs.telegramChatId, msg)) lastAlertAt = now
    }

    private fun updateStatus(text: String) { /* reserved for in-app status broadcast */ }
    private fun notification(text: String): Notification {
        val channelId = "flow_monitor"
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel(channelId, "Futures Flow", NotificationManager.IMPORTANCE_LOW))
        }
        return NotificationCompat.Builder(this, channelId).setContentTitle("Futures Flow").setContentText(text).setSmallIcon(android.R.drawable.stat_notify_sync).setOngoing(true).build()
    }
    override fun onDestroy() { socket?.close(1000, "service stopped"); executor.shutdownNow(); super.onDestroy() }
}
