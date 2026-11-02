package com.futuresflow.app

data class FlowTrade(val symbol: String, val price: Double, val quantity: Double, val quoteValue: Double, val aggressor: String, val tradeId: Long, val eventTime: Long)
data class TimeframeResult(val interval: String, val direction: String, val changePct: Double)
data class Signal(val symbol: String, val quoteValue: Double, val aggressor: String, val frames: List<TimeframeResult>, val oiChangePct: Double, val cvd: Double, val tp1: Double, val tp2: Double, val invalidation: Double, val entry: Double)
