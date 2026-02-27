package by.nik.warehouseapp.features.returns.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import by.nik.warehouseapp.core.AppColors
import by.nik.warehouseapp.core.data.ContractorRepository
import by.nik.warehouseapp.features.returns.model.DeliveryPoint

class TtPickerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CONTRACTOR_UNP = "contractor_unp"
        const val EXTRA_TT_CODE = "tt_code"
        const val EXTRA_TT_ADDRESS = "tt_address"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val unp = intent.getStringExtra(EXTRA_CONTRACTOR_UNP).orEmpty()
        val contractor = ContractorRepository.all.find { it.unp == unp }

        setContent {
            MaterialTheme {
                TtPickerScreen(
                    contractorName = contractor?.name.orEmpty(),
                    points = contractor?.deliveryPoints ?: emptyList(),
                    onBack = { finish() },
                    onPick = { point ->
                        val intent = Intent().apply {
                            putExtra(EXTRA_TT_CODE, point.code)
                            putExtra(EXTRA_TT_ADDRESS, point.address)
                        }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun Modifier.safeClickable(onClick: () -> Unit): Modifier {
    val indication = LocalIndication.current
    val interaction = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interaction,
        indication = indication,
        onClick = onClick
    )
}

@Composable
private fun TtPickerScreen(
    contractorName: String,
    points: List<DeliveryPoint>,
    onBack: () -> Unit,
    onPick: (DeliveryPoint) -> Unit
) {
    val screenBg = Color(0xFFEEF2F6)
    var query by remember { mutableStateOf("") }

    val filtered = remember(query) {
        if (query.isBlank()) points
        else points.filter {
            it.address.lowercase().contains(query.trim().lowercase()) ||
                    it.code.contains(query.trim())
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(AppColors.primaryBlue)
    ) {
        // Шапка
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(48.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color.White
                )
            }
            Text(
                "Торговая точка",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .background(screenBg)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            // Плашка с контрагентом
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCEAFB))
            ) {
                Text(
                    text = contractorName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.primaryBlue,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Поиск
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (query.isEmpty()) {
                                Text(
                                    "Поиск по адресу или коду ТТ...",
                                    fontSize = 14.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ничего не найдено", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    LazyColumn {
                        itemsIndexed(filtered) { index, point ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .safeClickable { onPick(point) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = point.address,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "Код ТТ: ${point.code}",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            if (index < filtered.lastIndex) {
                                HorizontalDivider(color = Color(0xFFE5E7EB))
                            }
                        }
                    }
                }
            }
        }
    }
}