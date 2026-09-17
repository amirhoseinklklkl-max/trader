package ir.amir.triedgame.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.amir.triedgame.ads.AdManager
import ir.amir.triedgame.billing.BillingManager
import ir.amir.triedgame.ui.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun ChargeAccountScreen(
    viewModel: GameViewModel,
    adManager: AdManager?,
    billingManager: BillingManager?
) {
    var remainingAds by remember { mutableIntStateOf(adManager?.remainingToday() ?: 0) }
    var cooldownSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(adManager) {
        while (true) {
            remainingAds = adManager?.remainingToday() ?: 0
            cooldownSeconds = ((adManager?.cooldownRemainingMillis() ?: 0L) / 1000L).toInt()
            delay(1000)
        }
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("شارژ حساب", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        // Rewarded ad section
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("دیدن تبلیغ برای دریافت پول", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("هر بار مشاهده: ${AdManager.REWARD_TOMAN.toInt()} تومان")
                Text("باقی‌مانده امروز: $remainingAds از ${AdManager.DAILY_LIMIT}")
                if (cooldownSeconds > 0) {
                    Text("زمان باقی‌مانده تا تبلیغ بعدی: ${cooldownSeconds}s")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        adManager?.showRewardedAd { rewarded ->
                            if (rewarded) {
                                viewModel.creditToman(AdManager.REWARD_TOMAN)
                            }
                        }
                    },
                    enabled = adManager?.canShowNow() == true
                ) { Text("دیدن تبلیغ") }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("خرید بسته‌ی شارژ (مایکت)", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        BillingManager.SKUS.forEach { sku ->
            val usd = BillingManager.USD_REWARD[sku] ?: 0.0
            val toman = BillingManager.TOMAN_PRICE[sku] ?: 0L
            ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("${usd.toInt()}$ به کیف پول")
                        Text("قیمت: ${toman} تومان", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { billingManager?.purchase(sku) }) {
                        Text("خرید")
                    }
                }
            }
        }
    }
}
