package ir.amir.triedgame.model

/** Fixed USD -> Toman conversion rate used throughout the game's economy. */
const val USD_TO_TOMAN_RATE = 220_000.0

data class Wallet(
    val usdBalance: Double = 0.0,
    val tomanBalance: Double = 0.0
) {
    fun convertUsdToToman(amountUsd: Double): Wallet {
        val usable = amountUsd.coerceAtMost(usdBalance)
        return copy(
            usdBalance = usdBalance - usable,
            tomanBalance = tomanBalance + usable * USD_TO_TOMAN_RATE
        )
    }

    fun convertTomanToUsd(amountToman: Double): Wallet {
        val usable = amountToman.coerceAtMost(tomanBalance)
        return copy(
            tomanBalance = tomanBalance - usable,
            usdBalance = usdBalance + usable / USD_TO_TOMAN_RATE
        )
    }

    fun addUsd(amount: Double): Wallet = copy(usdBalance = usdBalance + amount)
    fun addToman(amount: Double): Wallet = copy(tomanBalance = tomanBalance + amount)
    fun spendToman(amount: Double): Wallet? =
        if (tomanBalance >= amount) copy(tomanBalance = tomanBalance - amount) else null
}

enum class PositionSide { LONG, SHORT }

data class Position(
    val id: String,
    val assetSymbol: String,
    val side: PositionSide,
    val entryPrice: Double,
    val amountUsd: Double, // margin/size committed from the wallet
    val openedAtMillis: Long
) {
    fun currentPnlUsd(currentPrice: Double): Double {
        val change = (currentPrice - entryPrice) / entryPrice
        val signedChange = if (side == PositionSide.LONG) change else -change
        return amountUsd * signedChange
    }

    fun currentValueUsd(currentPrice: Double): Double = amountUsd + currentPnlUsd(currentPrice)
}

data class LifeStats(
    val health: Int = 100,
    val hunger: Int = 100,
    val energy: Int = 100
) {
    fun clamp() = copy(
        health = health.coerceIn(0, 100),
        hunger = hunger.coerceIn(0, 100),
        energy = energy.coerceIn(0, 100)
    )
}

data class UserProfile(
    val firstName: String,
    val lastName: String,
    val level: Int = 1,
    val xp: Int = 0,
    val randomSeed: Long,
    val accountCreatedAtMillis: Long
)
