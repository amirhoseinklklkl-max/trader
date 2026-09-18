package ir.amir.triedgame.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.amir.triedgame.model.AssetCatalog
import ir.amir.triedgame.model.Position
import ir.amir.triedgame.model.PositionSide
import ir.amir.triedgame.ui.GameViewModel
import ir.amir.triedgame.ui.components.CandlestickChart
import ir.amir.triedgame.ui.theme.TsGreen
import ir.amir.triedgame.ui.theme.TsRed
import java.util.Locale

@Composable
fun TradingScreen(viewModel: GameViewModel) {
    val price = viewModel.currentPrices[viewModel.selectedAsset.symbol] ?: viewModel.selectedAsset.startingPriceUsd
    var tradeAmount by remember { mutableStateOf(10.0) }

    Row(Modifier.fillMaxSize()) {
        // Asset list
        LazyColumn(
            modifier = Modifier.width(160.dp).fillMaxHeight().padding(4.dp)
        ) {
            items(AssetCatalog.all) { asset ->
                val isSelected = asset.symbol == viewModel.selectedAsset.symbol
                Surface(
                    tonalElevation = if (isSelected) 4.dp else 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickableSafe { viewModel.selectAsset(asset) }
                    ) {
                        Text(asset.symbol, fontWeight = FontWeight.Bold)
                        Text(
                            String.format(Locale.US, "%.4f", viewModel.currentPrices[asset.symbol] ?: asset.startingPriceUsd),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Chart + trade controls
        Column(Modifier.weight(1f).fillMaxHeight().padding(8.dp)) {
            Text(
                "${viewModel.selectedAsset.displayName} (${viewModel.selectedAsset.symbol})",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                String.format(Locale.US, "%.4f $", price),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(8.dp))
            CandlestickChart(
                candles = viewModel.candles,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("مقدار (دلار):")
                Spacer(Modifier.width(8.dp))
                listOf(10.0, 50.0, 100.0).forEach { amt ->
                    OutlinedButton(onClick = { tradeAmount = amt }, modifier = Modifier.padding(2.dp)) {
                        Text("$${amt.toInt()}")
                    }
                }
                Spacer(Modifier.weight(1f))
                Text("موجودی: ${String.format(Locale.US, "%.2f", viewModel.wallet.usdBalance)}$")
            }

            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Button(
                    onClick = { viewModel.openPosition(PositionSide.LONG, tradeAmount) },
                    colors = ButtonDefaults.buttonColors(containerColor = TsGreen),
                    modifier = Modifier.weight(1f)
                ) { Text("خرید / Long") }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.openPosition(PositionSide.SHORT, tradeAmount) },
                    colors = ButtonDefaults.buttonColors(containerColor = TsRed),
                    modifier = Modifier.weight(1f)
                ) { Text("فروش / Short") }
            }
        }

        // Open positions
        Column(
            modifier = Modifier.width(220.dp).fillMaxHeight().padding(4.dp)
        ) {
            Text("پوزیشن‌های باز", style = MaterialTheme.typography.titleSmall)
            LazyColumn {
                items(viewModel.positions) { position ->
                    PositionRow(position, viewModel)
                }
            }
        }
    }
}

@Composable
private fun PositionRow(position: Position, viewModel: GameViewModel) {
    val price = viewModel.currentPrices[position.assetSymbol] ?: position.entryPrice
    val pnl = position.currentPnlUsd(price)
    val color = if (pnl >= 0) TsGreen else TsRed

    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Column(Modifier.padding(8.dp)) {
            Text("${position.assetSymbol} • ${position.side}")
            Text(
                String.format(Locale.US, "%+.2f$", pnl),
                color = color,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { viewModel.closePosition(position) }) {
                Text("بستن پوزیشن")
            }
        }
    }
}

// Small helper so we don't have to import `clickable` with its full ripple config everywhere.
private fun Modifier.clickableSafe(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable { onClick() })
