package ir.amir.triedgame.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import ir.amir.triedgame.model.Candle
import ir.amir.triedgame.ui.theme.TsBackground
import ir.amir.triedgame.ui.theme.TsGreen
import ir.amir.triedgame.ui.theme.TsRed

/**
 * Lightweight candlestick chart drawn directly on a Compose Canvas.
 * Deliberately avoids a third-party charting dependency to keep the build
 * simple; if a richer chart (crosshair, pinch-zoom, indicators) is wanted
 * later, this can be swapped for a library like MPAndroidChart wrapped in
 * an AndroidView.
 */
@Composable
fun CandlestickChart(
    candles: List<Candle>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(TsBackground)
    ) {
        if (candles.isEmpty()) return@Canvas

        val maxPrice = candles.maxOf { it.high }
        val minPrice = candles.minOf { it.low }
        val priceRange = (maxPrice - minPrice).let { if (it <= 0.0) 1.0 else it }

        val candleSlotWidth = size.width / candles.size
        val bodyWidth = candleSlotWidth * 0.6f
        val wickWidth = (candleSlotWidth * 0.08f).coerceAtLeast(2f)

        fun yFor(price: Double): Float {
            val fraction = (price - minPrice) / priceRange
            return (size.height * (1.0 - fraction)).toFloat()
        }

        candles.forEachIndexed { index, candle ->
            val centerX = candleSlotWidth * index + candleSlotWidth / 2f
            val isBullish = candle.close >= candle.open
            val color = if (isBullish) TsGreen else TsRed

            val highY = yFor(candle.high)
            val lowY = yFor(candle.low)
            val openY = yFor(candle.open)
            val closeY = yFor(candle.close)

            // Wick
            drawLine(
                color = color,
                start = Offset(centerX, highY),
                end = Offset(centerX, lowY),
                strokeWidth = wickWidth
            )

            // Body
            val bodyTop = minOf(openY, closeY)
            val bodyBottom = maxOf(openY, closeY).coerceAtLeast(bodyTop + 2f)
            drawRect(
                color = color,
                topLeft = Offset(centerX - bodyWidth / 2f, bodyTop),
                size = androidx.compose.ui.geometry.Size(bodyWidth, bodyBottom - bodyTop)
            )
        }
    }
}
