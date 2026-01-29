package by.nik.warehouseapp.features.returns.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.android.material.datepicker.MaterialDatePicker
import androidx.fragment.app.FragmentActivity
import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalFocusManager


class ReturnCreateComposeActivity : AppCompatActivity() {
    @OptIn(ExperimentalFoundationApi::class)
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReturnCreateUiOnly(
    hostActivity: FragmentActivity,
    onBack: () -> Unit,
    onCancel: () -> Unit
)
 {
    // цвета под эталон
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
    val df = remember { SimpleDateFormat("dd.MM.yyyy", Locale("ru")) }
     val focusManager = LocalFocusManager.current



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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryBlue
                )
            )
        },
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )
            {
                Button(
                    onClick = { /* пока пусто */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = green)
                ) {
                    Text("Создать возврат", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Отменить",
                    color = primaryBlue,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { onCancel() }
                        .padding(6.dp)
                )
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = docTypeExpanded) },
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

            Label("Наименование контрагента", title)
            Spacer(Modifier.height(10.dp))
            OutlinedField(
                value = contractor,
                onValueChange = { contractor = it },
                placeholder = "Начните вводить контрагента",
                borderColor = fieldBorder,
                hintColor = hint,
                shape = shape12
            )

            Spacer(Modifier.height(14.dp))

            Label("№ТТН", title)
            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedField(
                    modifier = Modifier.weight(1f),
                    value = invoice,
                    onValueChange = { invoice = it },
                    placeholder = "Введите или сканируйте №ТТН",
                    borderColor = fieldBorder,
                    hintColor = hint,
                    shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .width(62.dp)
                        .height(56.dp)
                        .background(green, RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                        .clickable { /* позже откроем скан */ },
                    contentAlignment = Alignment.Center
                ) {
                    Text("▦", color = Color.White, fontSize = 22.sp)
                }
            }

            Spacer(Modifier.height(14.dp))

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
                            dateText = df.format(java.util.Date(millis))
                            focusManager.clearFocus(force = true)
                        }

                        picker.show(hostActivity.supportFragmentManager, "doc_date_picker")
                    }

            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxSize(),
                    value = dateText,
                    onValueChange = {},
                    enabled = false,           // ✅ важно: само поле не ловит события
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

private fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}