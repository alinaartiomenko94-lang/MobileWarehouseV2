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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import by.nik.warehouseapp.core.AppColors
import by.nik.warehouseapp.core.data.InMemoryRepository
import by.nik.warehouseapp.features.returns.model.ReturnDocument
import by.nik.warehouseapp.features.returns.model.ReturnDocType
import by.nik.warehouseapp.features.returns.model.ReturnStatus

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

@Composable
private fun ReturnListScreen(onAddReturn: () -> Unit) {
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

        // Основной контент
        Column(
            Modifier
                .fillMaxSize()
                .background(screenBg)
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
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
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
                            .background(if (isSelected) AppColors.primaryBlue else Color.White)
                            .border(1.dp, AppColors.primaryBlue, RoundedCornerShape(20.dp))
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
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredDocs) { doc ->
                        ReturnDocCard(
                            doc = doc,
                            onRefresh = { docs = InMemoryRepository.returns.toList() }
                        )
                    }
                }
            }
        }
    }

    // Кнопка внизу
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
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
}

@Composable
private fun ReturnDocCard(doc: ReturnDocument, onRefresh: () -> Unit) {
    val context = LocalContext.current
    var detailsExpanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val lines = InMemoryRepository.lines.filter { it.returnId == doc.id }
    val totalQty = lines.sumOf { it.quantity }
    val totalDefect = lines.sumOf { it.defect }

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

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Меню",
                            tint = Color(0xFF6B7280)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        when (doc.status) {
                            ReturnStatus.CREATED -> {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = Color(0xFF6B7280),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text("Редактировать")
                                        }
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        val intent = Intent(context, ReturnCreateComposeActivity::class.java)
                                        intent.putExtra(ReturnCreateComposeActivity.EXTRA_RETURN_ID, doc.id)
                                        context.startActivity(intent)
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = Color.Red,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text("Удалить", color = Color.Red)
                                        }
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                            ReturnStatus.IN_WORK -> {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF3E9E3E),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text("Подтвердить приёмку")
                                        }
                                    },
                                    onClick = {
                                        val index = InMemoryRepository.returns.indexOfFirst { it.id == doc.id }
                                        if (index != -1) {
                                            InMemoryRepository.returns[index] = doc.copy(
                                                status = ReturnStatus.UPLOADED
                                            )
                                        }
                                        menuExpanded = false
                                        onRefresh()
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = Color(0xFF6B7280),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text("Редактировать")
                                        }
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        val intent = Intent(context, ReturnCreateComposeActivity::class.java)
                                        intent.putExtra(ReturnCreateComposeActivity.EXTRA_RETURN_ID, doc.id)
                                        context.startActivity(intent)
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = Color.Red,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text("Удалить", color = Color.Red)
                                        }
                                    },
                                    onClick = {
                                        InMemoryRepository.returns.remove(doc)
                                        menuExpanded = false
                                        onRefresh()
                                    }
                                )
                            }
                            ReturnStatus.ACCEPTED -> {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = Color.Red,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text("Удалить", color = Color.Red)
                                        }
                                    },
                                    onClick = {
                                        InMemoryRepository.returns.remove(doc)
                                        menuExpanded = false
                                        onRefresh()
                                    }
                                )
                            }
                            ReturnStatus.UPLOADED -> {
                                DropdownMenuItem(
                                    text = { Text("Документ выгружен", color = Color.Gray) },
                                    onClick = {
                                        menuExpanded = false
                                        val intent = Intent(context, ReturnCreateComposeActivity::class.java)
                                        intent.putExtra(ReturnCreateComposeActivity.EXTRA_RETURN_ID, doc.id)
                                        context.startActivity(intent)
                                    },
                                    enabled = false
                                )
                            }
                        }
                    }
                }
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

            Text(
                text = "${doc.documentDate} • ${docTypeText(doc)}",
                fontSize = 15.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Spacer(Modifier.height(8.dp))

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
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Spacer(Modifier.height(10.dp))

            // Кнопки
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, AppColors.primaryBlue, RoundedCornerShape(20.dp))
                        .safeClickable { detailsExpanded = !detailsExpanded },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (detailsExpanded)
                                Icons.Default.KeyboardArrowUp
                            else
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = AppColors.primaryBlue,
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

            // Раскрывающиеся детали
            if (detailsExpanded) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFE5E7EB))
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = AppColors.primaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Всего товара:", fontSize = 13.sp, color = Color(0xFF6B7280))
                    }
                    Text(
                        "$totalQty шт.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE87B1E),
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Из них брак:", fontSize = 13.sp, color = Color(0xFF6B7280))
                    }
                    Text(
                        "$totalDefect шт.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE87B1E)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    "Удалить возврат?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    "Возврат ТТН № ${doc.invoiceNumber} (${doc.contractorName}) будет удалён. Это действие нельзя отменить.",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            },
            confirmButton = {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteDialog = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppColors.primaryBlue
                        )
                    ) {
                        Text("Отмена")
                    }
                    Button(
                        onClick = {
                            InMemoryRepository.returns.remove(doc)
                            showDeleteDialog = false
                            onRefresh()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Удалить", color = Color.White)
                    }
                }
            },
            dismissButton = {}
        )
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