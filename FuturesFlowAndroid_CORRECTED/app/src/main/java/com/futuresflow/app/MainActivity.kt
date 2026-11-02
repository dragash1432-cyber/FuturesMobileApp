package com.futuresflow.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(this)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 40, 32, 24); setBackgroundColor(0xFF101010.toInt()) }
        fun label(text: String) = TextView(this).apply { this.text = text; textSize = 15f; setTextColor(0xFFFFFFFF.toInt()); setPadding(0, 12, 0, 8) }
        val token = EditText(this).apply { hint = "Telegram Bot Token"; setSingleLine(); setText(prefs.telegramToken) }
        val chat = EditText(this).apply { hint = "Telegram Chat ID"; setSingleLine(); setText(prefs.telegramChatId) }
        val save = Button(this).apply { text = "Telegram ayarlarını kaydet"; setOnClickListener { prefs.telegramToken = token.text.toString(); prefs.telegramChatId = chat.text.toString(); Toast.makeText(this@MainActivity, "Kaydedildi", Toast.LENGTH_SHORT).show() } }
        val start = Button(this).apply { text = "İzlemeyi başlat"; setOnClickListener { prefs.telegramToken = token.text.toString(); prefs.telegramChatId = chat.text.toString(); ContextCompat.startForegroundService(this@MainActivity, Intent(this@MainActivity, FlowService::class.java)); Toast.makeText(this@MainActivity, "Servis başlatıldı", Toast.LENGTH_SHORT).show() } }
        val stop = Button(this).apply { text = "İzlemeyi durdur"; setOnClickListener { stopService(Intent(this@MainActivity, FlowService::class.java)) } }
        root.addView(label("FUTURES FLOW — Binance USDT-M"))
        root.addView(label("İlk sürüm izleme sembolü: BTCUSDT | Minimum tek işlem: 3.000 USDT"))
        root.addView(token); root.addView(chat); root.addView(save); root.addView(start); root.addView(stop)
        root.addView(label("Analiz: 5m / 15m / 30m / 1h / 4h\nBildirim: yalnızca filtreleri geçen sinyal"))
        setContentView(ScrollView(this).apply { addView(root) })
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
    }
}
