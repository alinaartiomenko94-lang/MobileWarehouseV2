package by.nik.warehouseapp.features.returns.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardActions


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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReturnCreateUiOnly(
    hostActivity: FragmentActivity,
    onBack: () -> Unit,
    onCancel: () -> Unit
) {
    // Цвета под эталон
    val screenBg = Color(0xFFEEF2F6)
    val primaryBlue = Color(0xFF2F73D9)
    val fieldBorder = Color(0xFFE5E7EB)
    val hint = Color(0xFF9CA3AF)
    val title = Color(0xFF0F172A)
    val green = Color(0xFF3E9E3E)

    val shape12 = RoundedCornerShape(12.dp)

    var docTypeText by remember { mutableStateOf("") }
    var docTypeExpanded by remember { mutableStateOf(false) }

    var contractor by remember { mutableStateOf("") }
    var invoice by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val df = remember { SimpleDateFormat("dd.MM.yyyy", Locale("ru")) }

    val contractorFR = remember { FocusRequester() }
    val invoiceFR = remember { FocusRequester() }

    // ✅ Лаунчер сканера — ОБЯЗАТЕЛЬНО внутри composable
    val scanInvoiceLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val raw = result.data?.getStringExtra(CameraScanActivity.EXTRA_RESULT).orEmpty()
                val normalized = normalizeInvoiceFromScan(raw)
                if (normalized.isNotBlank()) {
                    invoice = normalized
                }
            }
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
                    onClick = { /* позже добавим валидацию+создание */ },
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

            // ---- Тип документа (dropdown) ----
            Label("Тип документа", title)
            Spacer(Modifier.height(10.dp))

            ExposedDropdownMenuBox(
                expanded = docTypeExpanded,
                onExpandedChange = { docTypeExpanded = !docTypeExpanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
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
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = fieldBorder,
                        focusedBorderColor = fieldBorder,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
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
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Акт расхождения") },
                        onClick = {
                            docTypeText = "Акт расхождения"
                            docTypeExpanded = false
                        }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ---- Контрагент (пока обычное поле, позже сделаем подсказки) ----
            Label("Наименование контрагента", title)
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .focusRequester(contractorFR),
                value = contractor,
                onValueChange = { contractor = it },
                singleLine = true,
                placeholder = { Text("Начните вводить контрагента", color = hint) },
                shape = shape12,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = fieldBorder,
                    focusedBorderColor = fieldBorder,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    cursorColor = Color(0xFF0F172A)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { invoiceFR.requestFocus() }
                )
            )

            Spacer(Modifier.height(14.dp))

            // ---- №ТТН + Скан ----
            Label("№ТТН", title)
            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    .focusRequester(invoiceFR),
                    value = invoice,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }

                        if(digits.length <= 7) {
                            invoice = digits
                        }
                    },
                    singleLine = true,
                    placeholder = { Text("Введите или сканируйте №ТТН", color = hint) },
                    shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = fieldBorder,
                        focusedBorderColor = fieldBorder,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        cursorColor = Color(0xFF0F172A)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            // если хочешь, можно сразу открыть календарь:
                            // openDatePicker()
                        }
                    )

                )

                Box(
                    modifier = Modifier
                        .width(62.dp)
                        .height(56.dp)
                        .background(green, RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                        .clickable {
                            val intent = Intent(context, CameraScanActivity::class.java)
                            scanInvoiceLauncher.launch(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "SCAN",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ---- Дата (MaterialDatePicker) ----
            Label("Дата в ТТН / акте", title)
            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable {
                        val picker = MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Выберите дату")
                            .build()

                        picker.addOnPositiveButtonClickListener { millis ->
                            dateText = df.format(Date(millis))
                            focusManager.clearFocus(force = true)
                        }

                        picker.show(hostActivity.supportFragmentManager, "doc_date_picker")
                    }
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxSize(),
                    value = dateText,
                    onValueChange = {},
                    enabled = false,
                    readOnly = true,
                    singleLine = true,
                    placeholder = { Text("Дата в документе от контрагента", color = hint) },
                    trailingIcon = { Text("📅") },
                    shape = shape12,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = fieldBorder,
                        disabledContainerColor = Color.White,
                        disabledTextColor = Color(0xFF0F172A),
                        disabledPlaceholderColor = hint
                    )
                )
            }
        }
    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OutlinedField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    borderColor: Color,
    hintColor: Color,
    shape: RoundedCornerShape,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        readOnly = readOnly,
        placeholder = { Text(placeholder, color = hintColor) },
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = borderColor,
            focusedBorderColor = borderColor,
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
            cursorColor = Color(0xFF0F172A)
        )
    )
}
