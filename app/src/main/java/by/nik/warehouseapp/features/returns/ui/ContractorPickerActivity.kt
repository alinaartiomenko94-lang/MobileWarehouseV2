package by.nik.warehouseapp.features.returns.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import by.nik.warehouseapp.core.data.ContractorRepository
import by.nik.warehouseapp.features.returns.model.Contractor
import by.nik.warehouseapp.features.returns.model.DeliveryPoint

class ContractorPickerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CONTRACTOR_NAME = "contractor_name"
        const val EXTRA_CONTRACTOR_UNP = "contractor_unp"
        const val EXTRA_TT_CODE = "tt_code"
        const val EXTRA_TT_ADDRESS = "tt_address"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ContractorPickerScreen(
                    onBack = { finish() },
                    onPick = { contractor, point ->
                        val intent = Intent().apply {
                            putExtra(EXTRA_CONTRACTOR_NAME, contractor.name)
                            putExtra(EXTRA_CONTRACTOR_UNP, contractor.unp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContractorPickerScreen(
    onBack: () -> Unit,
    onPick: (Contractor, DeliveryPoint) -> Unit
) {
    val primaryBlue = Color(0xFF2F73D9)
    val screenBg = Color(0xFFEEF2F6)

    var selectedContractor by remember { mutableStateOf<Contractor?>(null) }
    var query by remember { mutableStateOf("") }

    val filtered = remember(query) {
        ContractorRepository.search(query)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        text = if (selectedContractor == null) "Выбор контрагента"
                        else "Торговая точка",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedContractor != null) {
                            selectedContractor = null
                            query = ""
                        } else {
                            onBack()
                        }
                    }) {
                        Text("←", color = Color.White, fontSize = 22.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryBlue)
            )
        }
    ) { padding ->

        Column(
            Modifier
                .fillMaxSize()
                .background(screenBg)
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            AnimatedContent(
                targetState = selectedContractor,
                transitionSpec = {
                    if (targetState != null) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "contractor_picker_anim"
            ) { contractor ->

                if (contractor == null) {
                    ContractorListStep(
                        query = query,
                        onQueryChange = { query = it },
                        contractors = filtered,
                        onSelect = { selected ->
                            if (selected.deliveryPoints.size == 1) {
                                onPick(selected, selected.deliveryPoints.first())
                            } else {
                                selectedContractor = selected
                            }
                        }
                    )
                } else {
                    DeliveryPointStep(
                        contractor = contractor,
                        onPick = { point -> onPick(contractor, point) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContractorListStep(
    query: String,
    onQueryChange: (String) -> Unit,
    contractors: List<Contractor>,
    onSelect: (Contractor) -> Unit
) {
    Column(Modifier.fillMaxSize()) {

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Поиск по названию или УНП") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(Modifier.height(12.dp))

        if (contractors.isEmpty()) {
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
                    itemsIndexed(contractors) { index, contractor ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .safeClickable { onSelect(contractor) }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Column {
                                Text(
                                    text = contractor.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "УНП: ${contractor.unp}",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        if (index < contractors.lastIndex) {
                            HorizontalDivider(color = Color(0xFFE5E7EB))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryPointStep(
    contractor: Contractor,
    onPick: (DeliveryPoint) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filtered = remember(query) {
        if (query.isBlank()) contractor.deliveryPoints
        else contractor.deliveryPoints.filter {
            it.address.lowercase().contains(query.trim().lowercase()) ||
                    it.code.contains(query.trim())
        }
    }

    Column(Modifier.fillMaxSize()) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCEAFB))
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = contractor.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F73D9)
                )
                Text(
                    text = "УНП: ${contractor.unp}",
                    fontSize = 13.sp,
                    color = Color(0xFF2F73D9)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Поиск по адресу или коду ТТ") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(Modifier.height(8.dp))

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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = point.address,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Код ТТ: ${point.code}",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }
                                Text("→", fontSize = 20.sp, color = Color(0xFF2F73D9))
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