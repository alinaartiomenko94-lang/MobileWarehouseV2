package by.nik.warehouseapp.features.returns.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import by.nik.warehouseapp.core.data.InMemoryRepository
import by.nik.warehouseapp.features.returns.model.ReturnDocument
import by.nik.warehouseapp.features.returns.model.ReturnStatus
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.res.painterResource

class ReturnListComposeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ReturnListScreen(
                    onAddReturn = {
                        startActivity(Intent(this, ReturnCreateComposeActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReturnListScreen(onAddReturn: () -> Unit) {
    val primaryBlue = Color(0xFF2F73D9)
    val screenBg = Color(0xFFEEF2F6)

    var docs by remember { mutableStateOf(InMemoryRepository.returns.toList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Все документы") }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.currentStateFlow.collect { state ->
            if (state == androidx.lifecycle.Lifecycle.State.RESUMED) {
                docs = InMemoryRepository.returns.toList()
            }
        }
    }

    val filters = listOf("Все документы", "В работе", "Принят", "Выгружен")

    val filteredDocs = remember(docs, searchQuery, selectedFilter) {
        docs.filter { doc ->
            val matchesSearch = searchQuery.isBlank() ||
                    doc.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                    doc.contractorName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "В работе" -> doc.status == ReturnStatus.IN_WORK
                "Принят" -> doc.status == ReturnStatus.ACCEPTED
                "Выгружен" -> doc.status == ReturnStatus.UPLOADED
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Приёмка возврата", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Text("←", color = Color.White, fontSize = 22.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryBlue)
            )
        },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onAddReturn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
                ) {
                    Text("+ Добавить возврат", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->

        Column(
            Modifier
                .fillMaxSize()
                .background(screenBg)
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp)
        ) {
            // Поиск
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Введите № ТТН, контрагента") },
                leadingIcon = { Text("🔍", fontSize = 16.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFFE5E7EB),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )

            Spacer(Modifier.height(12.dp))

            // Фильтры
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) primaryBlue else Color.White,
                                RoundedCornerShape(20.dp)
                            )
                            .safeClickable { selectedFilter = filter }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else Color(0xFF6B7280),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (filteredDocs.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Документов пока нет", fontSize = 16.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Text("Нажмите + чтобы добавить возврат", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredDocs) { doc ->
                        ReturnDocCard(doc = doc)
                    }
                }
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
private fun ReturnDocCard(doc: ReturnDocument) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = doc.contractorName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.weight(1f)
                )
                Text("⋮", fontSize = 20.sp, color = Color(0xFF6B7280))
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFF2F73D9), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ТТН № ${doc.invoiceNumber}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "${doc.documentDate} • ${docTypeText(doc)}",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(doc.status)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Приёмка: ${doc.acceptanceDate}",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(Modifier.height(12.dp))

            HorizontalDivider(color = Color(0xFFE5E7EB))

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6B7280)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("∨ Детали", fontSize = 13.sp, maxLines = 1)
                }

                Button(
                    onClick = {
                        val intent = Intent(context, ReturnItemsActivity::class.java)
                        intent.putExtra(ReturnItemsActivity.EXTRA_RETURN_ID, doc.id)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2F73D9)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("📋 Список товара", fontSize = 13.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ReturnStatus) {
    val (icon, text, bg) = when (status) {
        ReturnStatus.CREATED -> Triple("", "СОЗДАН", Color(0xFF6B7280))
        ReturnStatus.IN_WORK -> Triple("⏱", "В РАБОТЕ", Color(0xFFE87B1E))
        ReturnStatus.ACCEPTED -> Triple("✓", "ПРИНЯТ", Color(0xFF3E9E3E))
        ReturnStatus.UPLOADED -> Triple("📤", "ВЫГРУЖЕН", Color(0xFF2F73D9))
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (icon.isBlank()) text else "$icon $text",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun docTypeText(doc: ReturnDocument): String {
    return when (doc.docType) {
        by.nik.warehouseapp.features.returns.model.ReturnDocType.RETURN_INVOICE -> "Возвратная накладная"
        by.nik.warehouseapp.features.returns.model.ReturnDocType.DISCREPANCY_ACT -> "Акт расхождения"
    }
}
