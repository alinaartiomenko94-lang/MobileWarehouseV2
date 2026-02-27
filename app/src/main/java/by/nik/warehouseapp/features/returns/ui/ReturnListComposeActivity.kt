package by.nik.warehouseapp.features.returns.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import by.nik.warehouseapp.core.data.InMemoryRepository
import by.nik.warehouseapp.features.returns.model.ReturnDocument
import by.nik.warehouseapp.features.returns.model.ReturnDocType
import by.nik.warehouseapp.features.returns.model.ReturnStatus
import by.nik.warehouseapp.core.AppColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.automirrored.filled.ArrowBack

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
private fun ReturnListScreen(onAddReturn: () -> Unit) {
    val primaryBlue = AppColors.primaryBlue
    val navyBlue = Color(0xFF1A3E6E)
    val screenBg = Color(0xFFEEF2F6)

    var docs by remember { mutableStateOf(InMemoryRepository.returns.toList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Все документы") }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.currentStateFlow.collect { state ->
            if (state == Lifecycle.State.RESUMED) {
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(48.dp)
                    .background(AppColors.primaryBlue)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }
                Text(
                    "Приёмка возврата",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onAddReturn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E9E3E))
                ) {
                    // Иконка + в кружке
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Добавить возврат",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp)
                    )
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "Введите № ТТН, контрагента",
                                        fontSize = 14.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Фильтры
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) AppColors.primaryBlue else Color.White
                            )
                            .border(
                                width = 1.dp,
                                color = AppColors.primaryBlue,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .safeClickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else AppColors.primaryBlue,
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
                        Text(
                            "Нажмите + чтобы добавить возврат",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredDocs) { doc ->
                        ReturnDocCard(doc = doc)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReturnDocCard(doc: ReturnDocument) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(Modifier.padding(14.dp)) {

            // Контрагент + меню
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = doc.contractorName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.weight(1f)
                )
                Text("⋮", fontSize = 20.sp, color = Color(0xFF6B7280))
            }

            Spacer(Modifier.height(6.dp))

            // Бейдж ТТН
            Box(
                modifier = Modifier
                    .background(AppColors.primaryBlue, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ТТН № ${doc.invoiceNumber}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(6.dp))

            // Дата и тип
            Text(
                text = "${doc.documentDate} • ${docTypeText(doc)}",
                fontSize = 15.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Spacer(Modifier.height(8.dp))

            // Статус и приёмка
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(doc.status)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Приёмка: ${doc.acceptanceDate}",
                    fontSize = 15.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Кнопки
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Кнопка Детали — серая заливка
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, AppColors.primaryBlue, RoundedCornerShape(20.dp))
                        .safeClickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Детали",
                            fontSize = 13.sp,
                            color = AppColors.primaryBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Кнопка Список товара — синяя
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppColors.primaryBlue)
                        .safeClickable {
                            val intent = Intent(context, ReturnItemsActivity::class.java)
                            intent.putExtra(ReturnItemsActivity.EXTRA_RETURN_ID, doc.id)
                            context.startActivity(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Список товара",
                            fontSize = 13.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ReturnStatus) {
    val (icon, text, bg) = when (status) {
        ReturnStatus.CREATED -> Triple(null, "СОЗДАН", Color(0xFFE5E7EB))
        ReturnStatus.IN_WORK -> Triple(Icons.Default.Schedule, "В РАБОТЕ", Color(0xFFE87B1E))
        ReturnStatus.ACCEPTED -> Triple(Icons.Default.Check, "ПРИНЯТ", Color(0xFF3E9E3E))
        ReturnStatus.UPLOADED -> Triple(Icons.Default.Upload, "ВЫГРУЖЕН", Color(0xFF1A3E6E))
    }

    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (bg == Color(0xFFE5E7EB)) Color(0xFF6B7280) else Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
        Text(
            text = text,
            color = if (bg == Color(0xFFE5E7EB)) Color(0xFF6B7280) else Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun docTypeText(doc: ReturnDocument): String {
    return when (doc.docType) {
        ReturnDocType.RETURN_INVOICE -> "Возвратная накладная"
        ReturnDocType.DISCREPANCY_ACT -> "Акт расхождения"
    }
}