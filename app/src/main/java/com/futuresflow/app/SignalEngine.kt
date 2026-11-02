package com.futuresflow.app

/** Signal gate. Strategy-specific confirmation and target rules must be explicitly configured. */
class SignalEngine {
    private val intervals = listOf("5m", "15m", "30m", "1h", "4h")

    fun evaluate(
        symbol: String,
        trade: FlowTrade,
        framePrices: Map<String, Pair<Double, Double>?>,
        oiChangePct: Double?,
        cvd: Double
    ): Signal? {
        if (trade.quoteValue < MIN_TRADE_USDT || oiChangePct == null) return null
        // Require complete data for all five timeframes; do not infer missing values.
        val frames = intervals.map { interval ->
            val prices = framePrices[interval] ?: return null
            if (prices.first <= 0.0) return null
            val change = (prices.second - prices.first) / prices.first * 100.0
            TimeframeResult(interval, if (change >= 0.0) "LONG" else "SHORT", change)
        }

        // Deliberately fail closed until the user-defined confirmation filters and TP/SL
        // methodology are supplied. No fabricated target or invalidation price is emitted.
        return null
    }

    companion object { const val MIN_TRADE_USDT = 3000.0 }
}
