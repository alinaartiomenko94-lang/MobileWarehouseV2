package by.nik.warehouseapp.features.returns.ui

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import by.nik.warehouseapp.R
import by.nik.warehouseapp.features.returns.model.ReturnDocType
import by.nik.warehouseapp.features.returns.model.ReturnDocument
import by.nik.warehouseapp.features.returns.model.ReturnStatus
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReturnCreateActivity : AppCompatActivity() {

    private lateinit var etDocType: MaterialAutoCompleteTextView
    private lateinit var tilInvoice: TextInputLayout
    private lateinit var etInvoice: TextInputEditText

    private lateinit var tilDate: TextInputLayout
    private lateinit var etDate: TextInputEditText

    private lateinit var tilContractor: TextInputLayout
    private lateinit var etContractor: AutoCompleteTextView

    private var isDatePickerOpen = false

    private val df = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))

    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ СТАБИЛЬНО: без edge-to-edge, без плясок inset’ов
        WindowCompat.setDecorFitsSystemWindows(window, true)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_return_create)

        // Views
        etDocType = findViewById(R.id.etDocType)
        tilInvoice = findViewById(R.id.tilInvoice)
        etInvoice = findViewById(R.id.etInvoice)

        tilDate = findViewById(R.id.tilDate)
        etDate = findViewById(R.id.etDate)
        etDate.showSoftInputOnFocus = false

        tilContractor = findViewById(R.id.tilContractor)
        etContractor = findViewById(R.id.etContractor)

        // Toolbar back (если нужно)
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        setupDocTypeDropdown()
        setupInvoiceScanButton()
        setupDatePicker()
        setupContractorAutocomplete()
        setupImeActions()
        setupCreateButton()
    }

    // ---------------------------
    // DocType dropdown
    // ---------------------------
    private fun setupDocTypeDropdown() {
        val types = listOf("Возвратная накладная", "Акт расхождения")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, types)
        etDocType.setAdapter(adapter)

        // Никаких автопрыжков — максимум мягко переведём фокус на контрагента (он сверху)
        etDocType.setOnItemClickListener { _, _, _, _ ->
            etContractor.requestFocus()
            showIme(etContractor)

        }
    }

    // ---------------------------
    // Invoice scan
    // ---------------------------
    private fun setupInvoiceScanButton() {
        tilInvoice.isEndIconVisible = true
        tilInvoice.setEndIconOnClickListener {
            // Пока заглушка (потом камера/ТСД)
            val rawScan = "96105103169525"
            onScan(rawScan)
        }

        // Нормальная навигация по IME: Next не должен прыгать сам куда-то странно
        etInvoice.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                // Логично: Next → дата (но календарь не открываем сам)
                etDate.requestFocus()
                true
            } else false
        }

        // Нормализация, если “впечатался” длинный код от сканера как клавиатура
        etInvoice.addTextChangedListenerSimple { text ->
            val digits = text.filter { it.isDigit() }
            if (digits.length > 7) {
                val normalized = digits.takeLast(7)
                if (normalized != etInvoice.text?.toString().orEmpty()) {
                    etInvoice.setText(normalized)
                    etInvoice.setSelection(normalized.length)
                }
            }
        }
    }

    private fun extractInvoiceNumber(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        return if (digits.length >= 7) digits.takeLast(7) else digits
    }

    private fun onScan(raw: String) {
        val invoice = extractInvoiceNumber(raw)
        if (invoice.isBlank()) return
        etInvoice.setText(invoice)
        etInvoice.setSelection(invoice.length)
        // ✅ не открываем календарь, не прыгаем по полям
    }

    // ---------------------------
    // Date picker
    // ---------------------------
    private fun setupDatePicker() {
        // чтобы клавиатура не вылезала на поле даты
        etDate.showSoftInputOnFocus = false

        // ✅ 1) календарь с первого клика
        etDate.setOnClickListener { showDatePicker() }
        tilDate.setEndIconOnClickListener { showDatePicker() }

        // ✅ 2) если фокус пришёл с клавиатуры/переключением — тоже открываем календарь
        etDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etDate.post { showDatePicker() }
            }
        }
    }

    private fun showDatePicker() {
        if (isDatePickerOpen) return
        isDatePickerOpen = true

        val picker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
            .setTitleText("Выберите дату")
            .build()

        picker.addOnPositiveButtonClickListener { millis ->
            val date = Date(millis)
            etDate.setText(df.format(date))

            // ✅ остаёмся на поле даты
            etDate.post { etDate.requestFocus() }
        }

        picker.show(supportFragmentManager, "date_picker")
    }

    // ---------------------------
    // Contractor autocomplete (СПОКОЙНО, без дерганий)
    // ---------------------------
    private fun setupContractorAutocomplete() {
        val contractors = listOf(
            "ИП Самкова",
            "ООО Самовар уюта",
            "ООО ТехноСнаб",
            "ИП Самойлов",
            "ООО Сказка-Трейд"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, contractors)
        etContractor.setAdapter(adapter)

        // ✅ только с 3 символов. Android сам покажет список при вводе.
        etContractor.threshold = 3

        // ✅ не показываем dropdown на фокусе (иначе дерготня)
        etContractor.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) etContractor.dismissDropDown()
        }

        // ✅ если стёрли до <3 — прячем
        etContractor.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if ((s?.length ?: 0) < 3) etContractor.dismissDropDown()
                // ВАЖНО: НЕ вызываем showDropDown()
            }
        })

        // ✅ после выбора — закрываем клавиатуру
        etContractor.setOnItemClickListener { _, _, _, _ ->
            etContractor.dismissDropDown()
            hideIme(etContractor)

            // По желанию можно перевести фокус дальше на №ТТН:
            etInvoice.requestFocus()
        }
    }

    // ---------------------------
    // IME navigation (без “прыжков”)
    // ---------------------------
    private fun setupImeActions() {
        // Контрагент -> Next на №ТТН (клавиатура остаётся)
        etContractor.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                etInvoice.requestFocus()
                true
            } else false
        }
    }

    // ---------------------------
    // Create button
    // ---------------------------
    private fun setupCreateButton() {
        val btnCreate =
            findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCreate)

        btnCreate.setOnClickListener {
            val docTypeText = etDocType.text?.toString().orEmpty().trim()
            val invoice = etInvoice.text?.toString().orEmpty().trim()
            val docDate = etDate.text?.toString().orEmpty().trim()
            val contractor = etContractor.text?.toString().orEmpty().trim()

            // Валидация
            if (invoice.isBlank()) {
                tilInvoice.error = "Введите №ТТН"
                return@setOnClickListener
            } else tilInvoice.error = null

            if (docDate.isBlank()) {
                tilDate.error = "Выберите дату"
                return@setOnClickListener
            } else tilDate.error = null

            if (contractor.isBlank()) {
                tilContractor.error = "Введите контрагента"
                return@setOnClickListener
            } else tilContractor.error = null

            val docTypeEnum =
                if (docTypeText.contains("Акт", ignoreCase = true)) {
                    ReturnDocType.DISCREPANCY_ACT
                } else {
                    ReturnDocType.RETURN_INVOICE
                }

            val repo = by.nik.warehouseapp.core.data.InMemoryRepository
            val nextId = (repo.returns.maxOfOrNull { it.id } ?: 0L) + 1L

            val newDoc = ReturnDocument(
                id = nextId,
                docType = docTypeEnum,
                invoiceNumber = invoice,
                documentDate = docDate,
                acceptanceDate = "",
                contractorName = contractor,
                status = ReturnStatus.CREATED
            )

            repo.returns.add(0, newDoc)
            finish()
        }
    }

    // ---------------------------
    // Helpers
    // ---------------------------
    private fun showIme(view: android.view.View) {
        view.post {
            view.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideIme(view: android.view.View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun TextInputEditText.addTextChangedListenerSimple(onChanged: (String) -> Unit) {
        this.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onChanged(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }
}
