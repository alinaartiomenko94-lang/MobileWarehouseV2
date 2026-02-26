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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Icon
import by.nik.warehouseapp.R
import by.nik.warehouseapp.core.data.InMemoryRepository
import by.nik.warehouseapp.features.returns.model.ReturnDocument
import by.nik.warehouseapp.features.returns.model.ReturnDocType
import by.nik.warehouseapp.features.returns.model.ReturnStatus

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
    var contractorName by remember { mutableStateOf("") }
    var contractorUnp by remember { mutableStateOf("") }
    var ttCode by remember { mutableStateOf("") }
    var ttAddress by remember { mutableStateOf("") }
    var docTypeText by remember { mutableStateOf("") }
    var docTypeExpanded by remember { mutableStateOf(false) }
    var invoice by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }

// ---- Errors
    var contractorError by remember { mutableStateOf<String?>(null) }
    var ttError by remember { mutableStateOf<String?>(null) }
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

    val contractorPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                contractorName = data?.getStringExtra(ContractorPickerActivity.EXTRA_CONTRACTOR_NAME).orEmpty()
                contractorUnp = data?.getStringExtra(ContractorPickerActivity.EXTRA_CONTRACTOR_UNP).orEmpty()
                ttCode = data?.getStringExtra(ContractorPickerActivity.EXTRA_TT_CODE).orEmpty()
                ttAddress = data?.getStringExtra(ContractorPickerActivity.EXTRA_TT_ADDRESS).orEmpty()
                contractorError = null
                ttError = null
            }
        }

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
        if (contractorName.trim().isEmpty()) {
            contractorError = "Введите контрагента"
        } else contractorError = null

        if (ttCode.trim().isEmpty()) {
            ttError = "Выберите торговую точку"
        } else ttError = null

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
            dateError != null -> openDatePicker()
        }

        return contractorError == null &&
                ttError == null &&
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
                            val newDoc = ReturnDocument(
                                id = System.currentTimeMillis(),
                                docType = if (docTypeText == "Возвратная накладная")
                                    ReturnDocType.RETURN_INVOICE
                                else
                                    ReturnDocType.DISCREPANCY_ACT,
                                invoiceNumber = invoice,
                                documentDate = dateText,
                                acceptanceDate = SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(Date()),
                                contractorName = contractorName,
                                status = ReturnStatus.CREATED
                            )
                            InMemoryRepository.returns.add(newDoc)
                            (context as Activity).finish()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = green)
                ) {
                    Text("Создать возврат", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(7.dp))

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
            Spacer(Modifier.height(7.dp))

            Column(Modifier.fillMaxWidth()) {
                // Поле контрагента — нажимаешь, открывается экран выбора
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .safeClickable {
                            val intent = Intent(context, ContractorPickerActivity::class.java)
                            contractorPickerLauncher.launch(intent)
                        }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxSize(),
                        value = if (contractorName.isBlank()) "" else "${contractorName}",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        singleLine = true,
                        placeholder = { Text("Выберите контрагента", color = hint) },
                        trailingIcon = { Text("›", fontSize = 22.sp, color = hint) },
                        shape = shape12,
                        isError = contractorError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = if (contractorError != null) MaterialTheme.colorScheme.error else fieldBorder,
                            disabledContainerColor = Color.White,
                            disabledTextColor = textColor,
                            disabledPlaceholderColor = hint
                        )
                    )
                }
                FieldErrorText(contractorError)

                // Поле торговой точки — появляется после выбора контрагента
                if (contractorName.isNotBlank()) {
                    Spacer(Modifier.height(7.dp))
                    Label("Торговая точка", title)

                    Spacer(Modifier.height(7.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        value = if (ttAddress.isBlank()) "" else "${ttAddress}",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        singleLine = true,
                        placeholder = { Text("Торговая точка", color = hint) },
                        shape = shape12,
                        isError = ttError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = if (ttError != null) MaterialTheme.colorScheme.error else fieldBorder,
                            disabledContainerColor = Color.White,
                            disabledTextColor = textColor,
                            disabledPlaceholderColor = hint
                        )
                    )
                    FieldErrorText(ttError)
                }
            }

            Spacer(Modifier.height(14.dp))

            // ---------------- Тип документа ----------------
            Label("Тип документа", title)
            Spacer(Modifier.height(7.dp))

            Column(Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .safeClickable { docTypeExpanded = !docTypeExpanded }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxSize(),
                        value = docTypeText,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        singleLine = true,
                        placeholder = { Text("Выберите тип документа", color = hint) },
                        trailingIcon = {
                            Text(
                                text = if (docTypeExpanded) "▲" else "▼",
                                color = hint,
                                fontSize = 12.sp
                            )
                        },
                        shape = shape12,
                        isError = docTypeError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = if (docTypeError != null) MaterialTheme.colorScheme.error else fieldBorder,
                            disabledContainerColor = Color.White,
                            disabledTextColor = textColor,
                            disabledPlaceholderColor = hint
                        )
                    )
                }

                // Выпадающие пункты
                if (docTypeExpanded) {
                    Spacer(Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column {
                            listOf("Возвратная накладная", "Акт расхождения").forEach { option ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .safeClickable {
                                            docTypeText = option
                                            docTypeExpanded = false
                                            docTypeError = null
                                            invoiceFR.requestFocus()
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 16.sp,
                                        color = if (docTypeText == option) Color(0xFF2F73D9) else textColor,
                                        fontWeight = if (docTypeText == option) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                if (option == "Возвратная накладная") {
                                    HorizontalDivider(color = Color(0xFFE5E7EB))
                                }
                            }
                        }
                    }
                }

                FieldErrorText(docTypeError)
            }

            Spacer(Modifier.height(14.dp))

            // ---------------- №ТТН ----------------
            Label("№ТТН", title)
            Spacer(Modifier.height(7.dp))

            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        colors = whiteFieldColors
                    )

                    Box(
                        modifier = Modifier
                            .width(62.dp)
                            .height(54.dp)
                            .background(green, RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                            .safeClickable {
                                val intent = Intent(context, CameraScanActivity::class.java)
                                scanInvoiceLauncher.launch(intent)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_scan_24),
                            contentDescription = "Сканировать",
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }

                FieldErrorText(invoiceError)
            }

            Spacer(Modifier.height(14.dp))

            // ---------------- Дата ----------------
            Label("Дата в ТТН / акте", title)
            Spacer(Modifier.height(7.dp))

            Column(Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
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