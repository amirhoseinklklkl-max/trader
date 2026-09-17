package ir.amir.triedgame.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.amir.triedgame.model.LifeStats
import ir.amir.triedgame.ui.GameViewModel

private data class LifeItem(
    val title: String,
    val priceToman: Double,
    val effect: (LifeStats) -> LifeStats,
    val description: String
)

private val lifeItems = listOf(
    LifeItem("قهوه", 30_000.0, { it.copy(energy = it.energy + 15) }, "انرژی +۱۵"),
    LifeItem("ساندویچ فست‌فود", 80_000.0, { it.copy(hunger = it.hunger + 30) }, "گرسنگی +۳۰"),
    LifeItem("غذای رستوران", 250_000.0, { it.copy(hunger = it.hunger + 60, health = it.health + 5) }, "گرسنگی +۶۰، سلامتی +۵"),
    LifeItem("میان‌وعده سالم", 60_000.0, { it.copy(hunger = it.hunger + 20, health = it.health + 5) }, "گرسنگی +۲۰، سلامتی +۵"),
    LifeItem("استراحت کوتاه", 50_000.0, { it.copy(energy = it.energy + 20) }, "انرژی +۲۰"),
    LifeItem("ویزیت دکتر", 400_000.0, { it.copy(health = it.health + 40) }, "سلامتی +۴۰"),
    LifeItem("سفر داخلی", 3_000_000.0, { it }, "افزایش لول (تزئینی)"),
    LifeItem("ماشین اقتصادی", 200_000_000.0, { it }, "دارایی تزئینی، افزایش لول")
)

@Composable
fun LifeScreen(viewModel: GameViewModel) {
    val stats = viewModel.lifeStats
    var message by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("زندگی من", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        StatBar("سلامتی", stats.health)
        StatBar("گرسنگی", stats.hunger)
        StatBar("انرژی", stats.energy)

        message?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))
        Text("مغازه", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize()
        ) {
            items(lifeItems) { item ->
                ElevatedCard(Modifier.padding(6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item.title, style = MaterialTheme.typography.titleSmall)
                        Text(item.description, style = MaterialTheme.typography.bodySmall)
                        Text(String.format("%,.0f تومان", item.priceToman))
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            val ok = viewModel.spendTomanInLife(item.priceToman, item.effect)
                            message = if (ok) null else "موجودی تومانی کافی نیست"
                        }) { Text("خرید") }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBar(label: String, value: Int) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, modifier = Modifier.weight(1f))
            Text("$value%")
        }
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
