@file:OptIn(ExperimentalMaterial3Api::class)

package by.nik.warehouseapp.features.returns.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.fragment.app.FragmentActivity
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReturnCreateComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                ReturnCreateUiOnly(
                    hostActivity = this,
                    onBack = { finish() },
                    onCancel = { finish() }
                )
            }
        }
    }
}

private fun normalizeInvoiceFromScan(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    return if (digits.length >= 7) digits.takeLast(7) else digits
}

/** ✅ Безопасный clickable (без краша PlatformRipple) */
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

/** ✅ Временно: подсказки контрагентов (потом заменим на 1С) */
private object ContractorSuggestionsRepository {
    private val contractors = listOf(
        "ИП Самкова Е.В.",
        "ООО Самовар уюта",
        "ЧТУП УСамоката",
        "ООО Ромашка",
        "ИП Смирнов А.А."
    )

    fun search(query: String, limit: Int = 6): List<String> {
        val q = query.trim().lowercase()
        if (q.length < 3) return emptyList()

        return contractors
            .asSequence()
            .filter { it.lowercase().contains(q) }
            .take(limit)
            .toList()
    }
}

@Composable
private fun ReturnCreateUiOnly(
    hostActivity: FragmentActivity,
    onBack: () -> Unit,
    onCancel: () -> Unit
) {
    val screenBg = Color(0xFFEEF2F6)
    val primaryBlue = Color(0xFF2F73D9)
    val fieldBorder = Color(0xFFE5E7EB)
    val hint = Color(0xFF9CA3AF)
    val title = Color(0xFF0F172A)
    val green = Color(0xFF3E9E3E)
    val shape12 = RoundedCornerShape(12.dp)
    val textColor = Color(0xFF0F172A)

    // ---- Values
    var contractor by remember { mutableStateOf("") }
    var docTypeText by remember { mutableStateOf("") }
    var docTypeExpanded by remember { mutableStateOf(false) }
    var invoice by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }

    // ---- Errors (только текст; подсветка = красная обводка)
    var contractorError by remember { mutableStateOf<String?>(null) }
    var docTypeError by remember { mutableStateOf<String?>(null) }
    var invoiceError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val df = remember { SimpleDateFormat("dd.MM.yyyy", Locale("ru")) }

    // ---- Focus chain
    val contractorFR = remember { FocusRequester() }
    val docTypeFR = remember { FocusRequester() }
    val invoiceFR = remember { FocusRequester() }

    // ✅ Единые цвета полей: всегда белый фон, даже в ошибке
    val whiteFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = fieldBorder,
        unfocusedBorderColor = fieldBorder,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorContainerColor = Color.White, // 🔥 главное
        cursorColor = textColor
    )

    val scanInvoiceLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val raw = result.data?.getStringExtra(CameraScanActivity.EXTRA_RESULT).orEmpty()
                val normalized = normalizeInvoiceFromScan(raw)
                if (normalized.isNotBlank()) {
                    invoice = normalized
                    invoiceError = null
                    invoiceFR.requestFocus()
                }
            }
        }

    fun openDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Выберите дату")
            .build()

        picker.addOnPositiveButtonClickListener { millis ->
            dateText = df.format(Date(millis))
            dateError = null
            focusManager.clearFocus(force = true)
        }

        picker.show(hostActivity.supportFragmentManager, "doc_date_picker")
    }

    fun validateAndFocusFirstInvalid(): Boolean {
        // порядок проверки = порядок фокуса
        if (contractor.trim().isEmpty()) {
            contractorError = "Введите контрагента"
        } else contractorError = null

        if (docTypeText.trim().isEmpty()) {
            docTypeError = "Выберите тип документа"
        } else docTypeError = null

        val inv = invoice.trim()
        if (inv.isEmpty()) {
            invoiceError = "Введите №ТТН"
        } else if (inv.length != 7) {
            invoiceError = "№ТТН должен быть 7 цифр"
        } else invoiceError = null

        if (dateText.trim().isEmpty()) {
            dateError = "Выберите дату"
        } else dateError = null

        // фокус на первое невалидное
        when {
            contractorError != null -> contractorFR.requestFocus()
            docTypeError != null -> docTypeFR.requestFocus()
            invoiceError != null -> invoiceFR.requestFocus()
            dateError != null -> {
                // поле даты не фокусируем — сразу открываем календарь
                openDatePicker()
            }
        }

        return contractorError == null &&
                docTypeError == null &&
                invoiceError == null &&
                dateError == null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Добавить возврат", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", color = Color.White, fontSize = 22.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryBlue)
            )
        },
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        val ok = validateAndFocusFirstInvalid()
                        if (ok) {
                            // TODO: создать ReturnDocument + перейти назад в список
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = green)
                ) {
                    Text("Создать возврат", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(10.dp))

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Отменить", color = primaryBlue, fontSize = 16.sp)
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
                .padding(top = 18.dp)
        ) {

            // ---------------- Контрагент ----------------
            Label("Наименование контрагента", title)
            Spacer(Modifier.height(10.dp))

            Column(Modifier.fillMaxWidth()) {
                ContractorAutocompleteField(
                    modifier = Modifier.fillMaxWidth(),
                    value = contractor,
                    onValueChange = { newValue ->
                        contractor = newValue
                        contractorError = null
                    },
                    hintColor = hint,
                    borderColor = fieldBorder,
                    shape = shape12,
                    focusRequester = contractorFR,
                    isError = contractorError != null,
                    onNext = { docTypeFR.requestFocus() },
                    onPick = { picked ->
                        contractor = picked
                        contractorError = null
                        docTypeFR.requestFocus()
                    },
                    colors = whiteFieldColors
                )
                FieldErrorText(contractorError)
            }

            Spacer(Modifier.height(14.dp))

            // ---------------- Тип документа ----------------
            Label("Тип документа", title)
            Spacer(Modifier.height(10.dp))

            Column(Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = docTypeExpanded,
                    onExpandedChange = { docTypeExpanded = !docTypeExpanded }
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .focusRequester(docTypeFR)
                            .menuAnchor(),
                        value = docTypeText,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        placeholder = { Text("Возвратная накладная", color = hint) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = docTypeExpanded)
                        },
                        shape = shape12,
                        isError = docTypeError != null,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { invoiceFR.requestFocus() }
                        ),
                        colors = whiteFieldColors
                    )

                    ExposedDropdownMenu(
                        expanded = docTypeExpanded,
                        onDismissRequest = { docTypeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Возвратная накладная") },
                            onClick = {
                                docTypeText = "Возвратная накладная"
                                docTypeExpanded = false
                                docTypeError = null
                                invoiceFR.requestFocus()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Акт расхождения") },
                            onClick = {
                                docTypeText = "Акт расхождения"
                                docTypeExpanded = false
                                docTypeError = null
                                invoiceFR.requestFocus()
                            }
                        )
                    }
                }

                FieldErrorText(docTypeError)
            }

            Spacer(Modifier.height(14.dp))

            // ---------------- №ТТН ----------------
            Label("№ТТН", title)
            Spacer(Modifier.height(10.dp))

            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .focusRequester(invoiceFR),
                        value = invoice,
                        onValueChange = { raw ->
                            val digits = raw.filter { it.isDigit() }
                            if (digits.length <= 7) {
                                invoice = digits
                                invoiceError = null
                            }
                        },
                        singleLine = true,
                        isError = invoiceError != null,
                        placeholder = { Text("Введите или сканируйте №ТТН", color = hint) },
                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        colors = whiteFieldColors
                    )

                    Box(
                        modifier = Modifier
                            .width(62.dp)
                            .height(56.dp)
                            .background(green, RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                            .safeClickable {
                                val intent = Intent(context, CameraScanActivity::class.java)
                                scanInvoiceLauncher.launch(intent)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SCAN", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                FieldErrorText(invoiceError)
            }

            Spacer(Modifier.height(14.dp))

            // ---------------- Дата ----------------
            Label("Дата в ТТН / акте", title)
            Spacer(Modifier.height(10.dp))

            Column(Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .safeClickable { openDatePicker() }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxSize(),
                        value = dateText,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false, // ✅ не перехватывает клики, Box ловит
                        singleLine = true,
                        placeholder = { Text("Дата в документе от контрагента", color = hint) },
                        trailingIcon = { Text("📅") },
                        shape = shape12,
                        isError = dateError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = if (dateError != null) MaterialTheme.colorScheme.error else fieldBorder,
                            disabledContainerColor = Color.White,
                            disabledTextColor = textColor,
                            disabledPlaceholderColor = hint
                        )
                    )
                }

                FieldErrorText(dateError)
            }
        }
    }
}

@Composable
private fun ContractorAutocompleteField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    hintColor: Color,
    borderColor: Color,
    shape: RoundedCornerShape,
    focusRequester: FocusRequester,
    isError: Boolean,
    onNext: () -> Unit,
    onPick: (String) -> Unit,
    colors: TextFieldColors
) {
    var expanded by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var hasFocus by remember { mutableStateOf(false) }

    // ✅ чтобы после выбора не открывалось снова
    var suppressSearchOnce by remember { mutableStateOf(false) }

    // ✅ debounce + поиск в фоне
    LaunchedEffect(value, hasFocus) {
        val q = value.trim()

        if (!hasFocus) {
            expanded = false
            suggestions = emptyList()
            return@LaunchedEffect
        }

        if (suppressSearchOnce) {
            suppressSearchOnce = false
            expanded = false
            suggestions = emptyList()
            return@LaunchedEffect
        }

        if (q.length < 3) {
            expanded = false
            suggestions = emptyList()
            return@LaunchedEffect
        }

        delay(250)

        val res = withContext(Dispatchers.Default) {
            ContractorSuggestionsRepository.search(q, limit = 6)
        }

        val shouldShow = res.isNotEmpty() &&
                !(res.size == 1 && res[0].equals(q, ignoreCase = true))

        suggestions = res
        expanded = shouldShow
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { want -> expanded = want && suggestions.isNotEmpty() },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .focusRequester(focusRequester)
                .menuAnchor()
                .onFocusChanged { state -> hasFocus = state.isFocused },
            singleLine = true,
            placeholder = { Text("Начните вводить контрагента", color = hintColor) },
            shape = shape,
            isError = isError,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { onNext() }
            ),
            colors = colors
        )

        // ✅ обычный DropdownMenu: фокус НЕ теряется, Backspace работает
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            properties = PopupProperties(focusable = false),
            modifier = Modifier
                .heightIn(max = 260.dp)
                .exposedDropdownSize(matchTextFieldWidth = true)
        ) {
            suggestions.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        suppressSearchOnce = true
                        expanded = false
                        suggestions = emptyList()

                        onValueChange(item)
                        onPick(item)
                    }
                )
            }
        }
    }
}

@Composable
private fun FieldErrorText(text: String?) {
    if (text.isNullOrBlank()) return
    Spacer(Modifier.height(4.dp))
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        fontSize = 12.sp
    )
}

@Composable
private fun Label(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
}