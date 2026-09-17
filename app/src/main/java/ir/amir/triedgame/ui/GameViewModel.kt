package ir.amir.triedgame.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import ir.amir.triedgame.data.GameRepository
import ir.amir.triedgame.engine.PriceEngine
import ir.amir.triedgame.model.Asset
import ir.amir.triedgame.model.AssetCatalog
import ir.amir.triedgame.model.Candle
import ir.amir.triedgame.model.LifeStats
import ir.amir.triedgame.model.Position
import ir.amir.triedgame.model.PositionSide
import ir.amir.triedgame.model.UserProfile
import ir.amir.triedgame.model.Wallet
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import java.util.UUID

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    var profile by mutableStateOf<UserProfile?>(null)
        private set

    var wallet by mutableStateOf(Wallet())
        private set

    var lifeStats by mutableStateOf(LifeStats())
        private set

    var positions by mutableStateOf<List<Position>>(emptyList())
        private set

    var selectedAsset by mutableStateOf(AssetCatalog.all.first())
        private set

    var candles by mutableStateOf<List<Candle>>(emptyList())
        private set

    /** Live current price per asset symbol, updated every tick. */
    var currentPrices by mutableStateOf<Map<String, Double>>(emptyMap())
        private set

    private var tickerStarted = false

    fun loadOrCreateProfile(existing: UserProfile?) {
        if (existing != null) {
            profile = existing
            wallet = repository.loadWallet()
            lifeStats = repository.loadLifeStats()
            positions = repository.loadPositions()
            resolveOfflineGapAndStartTicking()
        }
    }

    fun registerNewUser(firstName: String, lastName: String) {
        val created = repository.createProfile(firstName, lastName)
        profile = created
        wallet = Wallet()
        lifeStats = LifeStats()
        positions = emptyList()
        resolveOfflineGapAndStartTicking()
    }

    fun selectAsset(asset: Asset) {
        selectedAsset = asset
        rebuildChartFor(asset)
    }

    private fun referencePriceFor(asset: Asset) = asset.startingPriceUsd

    private fun rebuildChartFor(asset: Asset) {
        val p = profile ?: return
        val now = System.currentTimeMillis()
        val from = now - PriceEngine.DAY_MS // last 24h at 1-min resolution for the visible chart window
        candles = PriceEngine.generateCandles(
            asset = asset,
            seed = p.randomSeed,
            fromMillis = from,
            toMillis = now,
            intervalMillis = PriceEngine.MINUTE_MS,
            referenceTimestampMillis = p.accountCreatedAtMillis,
            referencePrice = referencePriceFor(asset)
        )
    }

    /**
     * Called once when a profile becomes available (fresh registration or app
     * resume). Backfills every asset's price up to "now" from the last time we
     * ticked, so positions the user held while the app was closed reflect the
     * time that actually passed -- then starts the live per-second ticker.
     */
    private fun resolveOfflineGapAndStartTicking() {
        val p = profile ?: return
        val lastSeen = repository.loadLastSeen()
        val now = System.currentTimeMillis()

        val prices = mutableMapOf<String, Double>()
        AssetCatalog.all.forEach { asset ->
            prices[asset.symbol] = PriceEngine.priceAt(
                asset, p.randomSeed, now, p.accountCreatedAtMillis, referencePriceFor(asset)
            )
        }
        currentPrices = prices
        rebuildChartFor(selectedAsset)
        repository.saveLastSeen(now)

        if (!tickerStarted) {
            tickerStarted = true
            startTicker()
        }
    }

    private fun startTicker() {
        viewModelScope.launch {
            while (isActive) {
                delay(3000)
                tickOnce()
            }
        }
    }

    private fun tickOnce() {
        val p = profile ?: return
        val now = System.currentTimeMillis()
        val prices = mutableMapOf<String, Double>()
        AssetCatalog.all.forEach { asset ->
            prices[asset.symbol] = PriceEngine.priceAt(
                asset, p.randomSeed, now, p.accountCreatedAtMillis, referencePriceFor(asset)
            )
        }
        currentPrices = prices
        rebuildChartFor(selectedAsset)
        repository.saveLastSeen(now)
        applyLifeDrain()
    }

    private fun applyLifeDrain() {
        var stats = lifeStats
        // Small, steady drain while the trading screen is active; food/rest
        // in the "زندگی من" section restores these.
        stats = stats.copy(hunger = stats.hunger - 1, energy = stats.energy - 1)
        if (stats.hunger <= 0) {
            stats = stats.copy(health = stats.health - 2)
        }
        stats = stats.clamp()
        lifeStats = stats
        repository.saveLifeStats(stats)
    }

    // --- Trading ---

    fun openPosition(side: PositionSide, amountUsd: Double): Boolean {
        val price = currentPrices[selectedAsset.symbol] ?: return false
        val w = wallet.spendUsdForMargin(amountUsd) ?: return false
        wallet = w
        repository.saveWallet(wallet)
        val position = Position(
            id = UUID.randomUUID().toString(),
            assetSymbol = selectedAsset.symbol,
            side = side,
            entryPrice = price,
            amountUsd = amountUsd,
            openedAtMillis = System.currentTimeMillis()
        )
        positions = positions + position
        repository.savePositions(positions)
        return true
    }

    fun closePosition(position: Position) {
        val price = currentPrices[position.assetSymbol] ?: return
        val payout = position.currentValueUsd(price).coerceAtLeast(0.0)
        wallet = wallet.addUsd(payout)
        repository.saveWallet(wallet)
        positions = positions.filterNot { it.id == position.id }
        repository.savePositions(positions)
    }

    // --- Wallet ---

    fun creditUsd(amount: Double) {
        wallet = wallet.addUsd(amount)
        repository.saveWallet(wallet)
    }

    fun creditToman(amount: Double) {
        wallet = wallet.addToman(amount)
        repository.saveWallet(wallet)
    }

    fun convertUsdToToman(amount: Double) {
        wallet = wallet.convertUsdToToman(amount)
        repository.saveWallet(wallet)
    }

    fun convertTomanToUsd(amount: Double) {
        wallet = wallet.convertTomanToUsd(amount)
        repository.saveWallet(wallet)
    }

    fun spendTomanInLife(amount: Double, applyEffect: (LifeStats) -> LifeStats): Boolean {
        val newWallet = wallet.spendToman(amount) ?: return false
        wallet = newWallet
        repository.saveWallet(wallet)
        lifeStats = applyEffect(lifeStats).clamp()
        repository.saveLifeStats(lifeStats)
        return true
    }
}

/** Wallet needs a margin-hold helper distinct from a plain spend, since the
 * amount is returned (plus/minus P&L) when the position closes rather than
 * being permanently gone. Kept here to avoid widening Wallet's own API. */
private fun Wallet.spendUsdForMargin(amount: Double): Wallet? =
    if (usdBalance >= amount) copy(usdBalance = usdBalance - amount) else null
