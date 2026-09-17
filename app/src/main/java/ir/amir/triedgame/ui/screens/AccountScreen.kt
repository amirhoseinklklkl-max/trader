package ir.amir.triedgame.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.amir.triedgame.model.USD_TO_TOMAN_RATE
import ir.amir.triedgame.ui.GameViewModel
import java.util.Locale

@Composable
fun AccountScreen(viewModel: GameViewModel) {
    val profile = viewModel.profile
    var convertAmount by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("حساب کاربری", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (profile != null) {
            InfoRow("نام", "${profile.firstName} ${profile.lastName}")
            InfoRow("لول", profile.level.toString())
            InfoRow("تجربه (XP)", profile.xp.toString())
        }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        Text("کیف پول", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        InfoRow("موجودی دلاری (سرمایه‌ی ترید)", String.format(Locale.US, "%.2f $", viewModel.wallet.usdBalance))
        InfoRow("موجودی تومانی", String.format(Locale.US, "%,.0f تومان", viewModel.wallet.tomanBalance))
        InfoRow("نرخ تبدیل ثابت", String.format(Locale.US, "1$ = %,.0f تومان", USD_TO_TOMAN_RATE))

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            OutlinedTextField(
                value = convertAmount,
                onValueChange = { convertAmount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("مبلغ") },
                modifier = Modifier.width(160.dp)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                convertAmount.toDoubleOrNull()?.let { viewModel.convertUsdToToman(it) }
                convertAmount = ""
            }) { Text("دلار → تومان") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                convertAmount.toDoubleOrNull()?.let { viewModel.convertTomanToUsd(it) }
                convertAmount = ""
            }) { Text("تومان → دلار") }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
