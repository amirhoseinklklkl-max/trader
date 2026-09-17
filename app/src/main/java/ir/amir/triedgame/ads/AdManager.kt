package ir.amir.triedgame.ads

import android.content.Context
import com.adivery.sdk.Adivery
import com.adivery.sdk.AdiveryListener
import ir.amir.triedgame.BuildConfig
import ir.amir.triedgame.data.GameRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Wraps the Adivery rewarded ad flow and adds the game's own daily cap
 * (6 views/day, 2 minutes between views) on top of the 2-per-hour limit
 * already configured server-side in the Adivery dashboard. Both limits
 * apply; whichever is stricter at a given moment wins.
 */
class AdManager(private val context: Context, private val repository: GameRepository) {

    private val placementId = BuildConfig.ADIVERY_REWARDED_PLACEMENT_ID
    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private fun todayKey(): String = dayFormat.format(Date())

    fun prepare() {
        Adivery.prepareRewardedAd(context, placementId)
    }

    fun remainingToday(): Int {
        val used = repository.loadDailyAdCount(todayKey())
        return (DAILY_LIMIT - used).coerceAtLeast(0)
    }

    fun cooldownRemainingMillis(): Long {
        val lastAd = repository.loadLastAdTimestamp()
        val elapsed = System.currentTimeMillis() - lastAd
        return (COOLDOWN_MILLIS - elapsed).coerceAtLeast(0)
    }

    fun canShowNow(): Boolean = remainingToday() > 0 && cooldownRemainingMillis() == 0L && Adivery.isLoaded(placementId)

    /**
     * Shows the rewarded ad if allowed. [onResult] receives true only if the
     * ad actually played to completion and the reward should be granted.
     */
    fun showRewardedAd(onResult: (Boolean) -> Unit) {
        if (!canShowNow()) {
            onResult(false)
            return
        }

        Adivery.addPlacementListener(placementId, object : AdiveryListener() {
            override fun onRewardedAdClosed(placementId: String, isRewarded: Boolean) {
                Adivery.removePlacementListener(placementId)
                if (isRewarded) {
                    val day = todayKey()
                    repository.saveDailyAdCount(repository.loadDailyAdCount(day) + 1, day)
                    repository.saveLastAdTimestamp(System.currentTimeMillis())
                }
                onResult(isRewarded)
            }

            override fun onError(placementId: String, reason: String) {
                Adivery.removePlacementListener(placementId)
                onResult(false)
            }
        })

        Adivery.showAd(placementId)
    }

    companion object {
        const val DAILY_LIMIT = 6
        const val COOLDOWN_MILLIS = 2 * 60 * 1000L

        /** Fixed reward amount granted per completed rewarded ad view, in Toman. */
        const val REWARD_TOMAN = 75_000.0
    }
}
