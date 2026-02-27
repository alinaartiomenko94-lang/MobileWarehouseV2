//package by.nik.warehouseapp.features.returns.ui
//
//import android.content.Context
//import android.os.Bundle
//import android.view.inputmethod.EditorInfo
//import android.view.inputmethod.InputMethodManager
//import android.widget.ArrayAdapter
//import android.widget.AutoCompleteTextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.WindowCompat
//import by.nik.warehouseapp.R
//import by.nik.warehouseapp.features.returns.model.ReturnDocType
//import by.nik.warehouseapp.features.returns.model.ReturnDocument
//import by.nik.warehouseapp.features.returns.model.ReturnStatus
//import com.google.android.material.textfield.MaterialAutoCompleteTextView
//import com.google.android.material.textfield.TextInputEditText
//import com.google.android.material.textfield.TextInputLayout
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//import android.text.InputFilter
//import androidx.activity.result.contract.ActivityResultContracts
//import android.content.Intent
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.core.view.updatePadding
//
//
//class ReturnCreateActivity : AppCompatActivity() {
//
//    private lateinit var etDocType: MaterialAutoCompleteTextView
//    private lateinit var tilInvoice: TextInputLayout
//    private lateinit var etInvoice: TextInputEditText
//
//    private lateinit var tilDate: TextInputLayout
//    private lateinit var etDate: TextInputEditText
//
//    private lateinit var tilContractor: TextInputLayout
//    private lateinit var etContractor: AutoCompleteTextView
//
//    private var isDatePickerOpen = false
//
//    private val df = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
//
//    private lateinit var tvFormError: android.widget.TextView
//
//    private val scanInvoiceLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == RESULT_OK) {
//                val raw = result.data?.getStringExtra(CameraScanActivity.EXTRA_RESULT).orEmpty()
//                onScanInvoiceRaw(raw)
//            }
//        }
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        // ✅ СТАБИЛЬНО: без edge-to-edge, без плясок inset’ов
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//
//
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_return_create)
//
//
//        val root = findViewById<android.view.View>(R.id.root)
//        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
//        val bottomBlock = findViewById<android.view.View>(R.id.bottomBlock)
//
//        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
//            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//
//            // ✅ 1) Заголовок опускаем ниже (не под статус-бар)
//            toolbar.updatePadding(top = sysBars.top)
//
//            // ✅ 2) Низ не перекрывается навигацией телефона
//            bottomBlock.updatePadding(bottom = sysBars.bottom)
//
//            insets
//        }
//
//        root.requestApplyInsets()
//
//        // Views
//        etDocType = findViewById(R.id.etDocType)
//        tilInvoice = findViewById(R.id.tilInvoice)
//        etInvoice = findViewById(R.id.etInvoice)
//
//        tilDate = findViewById(R.id.tilDate)
//        etDate = findViewById(R.id.etDate)
//        // Запретить клавиатуру на поле даты
//        etDate.showSoftInputOnFocus = false
//        etDate.keyListener = null
//
//        tilContractor = findViewById(R.id.tilContractor)
//        etContractor = findViewById(R.id.etContractor)
//        tvFormError = findViewById(R.id.tvFormError)
//        setupLiveValidation()
//
//
//        // Toolbar back (если нужно)
////        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
////        toolbar.setNavigationOnClickListener { finish() }
//
//        setupDocTypeDropdown()
//        setupInvoiceScanButton()
//        setupDatePicker()
//        setupContractorAutocomplete()
//        setupImeActions()
//        setupCreateButton()
//    }
//
//    // ---------------------------
//    // DocType dropdown
//    // ---------------------------
//    private fun setupDocTypeDropdown() {
//        val types = listOf("Возвратная накладная", "Акт расхождения")
//        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, types)
//        etDocType.setAdapter(adapter)
//
//        // Никаких автопрыжков — максимум мягко переведём фокус на контрагента (он сверху)
//        etDocType.setOnItemClickListener { _, _, _, _ ->
//            etContractor.requestFocus()
//            showIme(etContractor)
//
//        }
//    }
//
//    // ---------------------------
//    // Invoice scan
//    // ---------------------------
//
//    private fun setupInvoiceScanButton() {
//        val btnScanInvoice =
//            findViewById<com.google.android.material.button.MaterialButton>(R.id.btnScanInvoice)
//
//        // 1) Ограничение ввода: только цифры и максимум 7
//        etInvoice.filters = arrayOf(
//            InputFilter.LengthFilter(7),
//            InputFilter { source, _, _, _, _, _ ->
//                source.filter { it.isDigit() }
//            }
//        )
//
//        // 2) Нормализация, если прилетело >7 (вставка/скан как клавиатура)
//        etInvoice.addTextChangedListenerSimple { text ->
//            val digits = text.filter { it.isDigit() }
//            val normalized = if (digits.length > 7) digits.takeLast(7) else digits
//
//            if (normalized != etInvoice.text?.toString().orEmpty()) {
//                etInvoice.setText(normalized)
//                etInvoice.setSelection(normalized.length)
//            }
//
//            if (normalized.isNotBlank()) clearFieldError(tilInvoice)
//        }
//
//        // 3) Реальный запуск камеры-сканера
//        btnScanInvoice.setOnClickListener {
//            android.widget.Toast.makeText(this, "Нажато СКАН", android.widget.Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, CameraScanActivity::class.java)
//            scanInvoiceLauncher.launch(intent)
//        }
//
//    }
//
//
//    private fun extractInvoiceNumber(raw: String): String {
//        val digits = raw.filter { it.isDigit() }
//        if (digits.isEmpty()) return ""
//        return if (digits.length >= 7) digits.takeLast(7) else digits
//    }
//
//    private fun onScan(raw: String) {
//        val invoice = extractInvoiceNumber(raw)
//        if (invoice.isBlank()) return
//        etInvoice.setText(invoice)
//        etInvoice.setSelection(invoice.length)
//        // ✅ не открываем календарь, не прыгаем по полям
//    }
//
//    // ---------------------------
//    // Date picker
//    // ---------------------------
//    private fun setupDatePicker() {
//        // запрет клавиатуры
//        etDate.showSoftInputOnFocus = false
//        etDate.keyListener = null
//
//        // ✅ Открывать календарь с первого тапа
//        etDate.setOnTouchListener { v, event ->
//            if (event.action == android.view.MotionEvent.ACTION_UP) {
//                hideIme(v)
//                showDatePicker()
//            }
//            true // важно: событие обработали, чтобы не было "второго клика"
//        }
//
//        tilDate.setEndIconOnClickListener {
//            hideIme(etDate)
//            showDatePicker()
//        }
//
//        // На фокусе просто прячем клавиатуру (НЕ открываем календарь)
//        etDate.setOnFocusChangeListener { v, hasFocus ->
//            if (hasFocus) hideIme(v)
//        }
//    }
//
//    private fun showDatePicker() {
//        if (isDatePickerOpen) return
//        isDatePickerOpen = true
//
//        val picker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
//            .setTitleText("Выберите дату")
//            .build()
//
//        picker.addOnPositiveButtonClickListener { millis ->
//            val date = Date(millis)
//            etDate.setText(df.format(date))
//            hideIme(etDate)
//
//            // ✅ запоминаем, что надо вернуть фокус на дату
//            etDate.tag = "keep_focus"
//        }
//
//        picker.addOnDismissListener {
//            isDatePickerOpen = false
//
//            // ✅ после закрытия календаря фиксируем фокус на дате
//            if (etDate.tag == "keep_focus") {
//                etDate.tag = null
//                etDate.post {
//                    etDate.requestFocus()
//                    hideIme(etDate) // на всякий случай
//                }
//            }
//        }
//
//        picker.show(supportFragmentManager, "date_picker")
//    }
//
//    // ---------------------------
//    // Contractor autocomplete (СПОКОЙНО, без дерганий)
//    // ---------------------------
//    private fun setupContractorAutocomplete() {
//        val contractors = listOf(
//            "ИП Самкова",
//            "ООО Самовар уюта",
//            "ООО ТехноСнаб",
//            "ИП Самойлов",
//            "ООО Сказка-Трейд"
//        )
//
//        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, contractors)
//        etContractor.setAdapter(adapter)
//
//        // ✅ только с 3 символов. Android сам покажет список при вводе.
//        etContractor.threshold = 3
//
//        // ✅ не показываем dropdown на фокусе (иначе дерготня)
//        etContractor.setOnFocusChangeListener { _, hasFocus ->
//            if (!hasFocus) etContractor.dismissDropDown()
//        }
//
//        // ✅ если стёрли до <3 — прячем
//        etContractor.addTextChangedListener(object : android.text.TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun afterTextChanged(s: android.text.Editable?) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if ((s?.length ?: 0) < 3) etContractor.dismissDropDown()
//                // ВАЖНО: НЕ вызываем showDropDown()
//            }
//        })
//
//        // ✅ после выбора — закрываем клавиатуру
//        etContractor.setOnItemClickListener { _, _, _, _ ->
//            etContractor.dismissDropDown()
//            hideIme(etContractor)
//
//            // По желанию можно перевести фокус дальше на №ТТН:
//            etInvoice.requestFocus()
//        }
//    }
//
//    // ---------------------------
//    // IME navigation (без “прыжков”)
//    // ---------------------------
//    private fun setupImeActions() {
//        // Контрагент -> Next на №ТТН (клавиатура остаётся)
//        etContractor.setOnEditorActionListener { _, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_NEXT) {
//                etInvoice.requestFocus()
//                true
//            } else false
//        }
//    }
//
//    // ---------------------------
//    // Create button
//    // ---------------------------
//    private fun setupCreateButton() {
//        val btnCreate =
//            findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCreate)
//
//        btnCreate.setOnClickListener {
//
//            // Сброс общих ошибок
//            tvFormError.visibility = android.view.View.GONE
//            tvFormError.text = ""
//
//            // Считываем значения
//            val docTypeText = etDocType.text?.toString().orEmpty().trim()
//            val contractor = etContractor.text?.toString().orEmpty().trim()
//            val invoice = etInvoice.text?.toString().orEmpty().trim()
//            val docDate = etDate.text?.toString().orEmpty().trim()
//
//            // Сброс ошибок на полях
//            clearFieldError(findViewById(R.id.tilDocType))
//            clearFieldError(tilContractor)
//            clearFieldError(tilInvoice)
//            clearFieldError(tilDate)
//
//            // Валидация по порядку и фокус на первое пустое поле
//            var firstInvalidView: android.view.View? = null
//
//            if (docTypeText.isBlank()) {
//                showFieldError(findViewById(R.id.tilDocType), "Значение не задано")
//                if (firstInvalidView == null) firstInvalidView = etDocType
//            }
//
//            if (contractor.isBlank()) {
//                showFieldError(tilContractor, "Значение не задано")
//                if (firstInvalidView == null) firstInvalidView = etContractor
//            }
//
//            if (invoice.isBlank() || invoice.length != 7) {
//                showFieldError(tilInvoice, "Введите 7 цифр №ТТН")
//                if (firstInvalidView == null) firstInvalidView = etInvoice
//            } else {
//                clearFieldError(tilInvoice)
//            }
//
//
//            if (docDate.isBlank()) {
//                showFieldError(tilDate, "Значение не задано")
//                if (firstInvalidView == null) firstInvalidView = etDate
//            }
//
//            // Если есть ошибки — показать общий текст и сфокусироваться
//            if (firstInvalidView != null) {
//                tvFormError.text = "Заполните обязательные поля"
//                tvFormError.visibility = android.view.View.VISIBLE
//
//                // фокус + прокрутка к полю
//                firstInvalidView!!.requestFocus()
//                firstInvalidView!!.post {
//                    // если это контрагент — покажем клавиатуру; если дата — нет
//                    if (firstInvalidView == etContractor || firstInvalidView == etInvoice) {
//                        showIme(firstInvalidView!!)
//                    } else {
//                        hideIme(firstInvalidView!!)
//                    }
//                }
//                return@setOnClickListener
//            }
//
//            // --- если всё заполнено — создаём документ ---
//            val docTypeEnum =
//                if (docTypeText.contains("Акт", ignoreCase = true)) {
//                    ReturnDocType.DISCREPANCY_ACT
//                } else {
//                    ReturnDocType.RETURN_INVOICE
//                }
//
//            val repo = by.nik.warehouseapp.core.data.InMemoryRepository
//            val nextId = (repo.returns.maxOfOrNull { it.id } ?: 0L) + 1L
//
//            val newDoc = ReturnDocument(
//                id = nextId,
//                docType = docTypeEnum,
//                invoiceNumber = invoice,
//                documentDate = docDate,
//                acceptanceDate = "",
//                contractorName = contractor,
//                status = ReturnStatus.CREATED
//            )
//
//            repo.returns.add(0, newDoc)
//            finish()
//        }
//    }
//
//    // ---------------------------
//    // Helpers
//    // ---------------------------
//    private fun showIme(view: android.view.View) {
//        view.post {
//            view.requestFocus()
//            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
//        }
//    }
//
//    private fun hideIme(view: android.view.View) {
//        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(view.windowToken, 0)
//    }
//
//    private fun TextInputEditText.addTextChangedListenerSimple(onChanged: (String) -> Unit) {
//        this.addTextChangedListener(object : android.text.TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                onChanged(s?.toString().orEmpty())
//            }
//            override fun afterTextChanged(s: android.text.Editable?) {}
//        })
//    }
//
//    private fun showFieldError(til: TextInputLayout, message: String) {
//        til.isErrorEnabled = true
//        til.error = message
//    }
//
//    private fun clearFieldError(til: TextInputLayout) {
//        til.error = null
//        til.isErrorEnabled = false
//    }
//
//    private fun isFormValidNow(): Boolean {
//        val docTypeText = etDocType.text?.toString().orEmpty().trim()
//        val contractor = etContractor.text?.toString().orEmpty().trim()
//        val invoice = etInvoice.text?.toString().orEmpty().trim()
//        val docDate = etDate.text?.toString().orEmpty().trim()
//
//        return docTypeText.isNotBlank()
//                && contractor.isNotBlank()
//                && invoice.isNotBlank()
//                && docDate.isNotBlank()
//    }
//
//    private fun setupLiveValidation() {
//
//        fun maybeHideBottomError() {
//            if (isFormValidNow()) {
//                tvFormError.visibility = android.view.View.GONE
//                tvFormError.text = ""
//            }
//        }
//
//        // Тип документа
//        etDocType.setOnItemClickListener { _, _, _, _ ->
//            clearFieldError(findViewById(R.id.tilDocType))
//            maybeHideBottomError()
//        }
//        etDocType.addTextChangedListener(object : android.text.TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun afterTextChanged(s: android.text.Editable?) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (!s.isNullOrBlank()) {
//                    clearFieldError(findViewById(R.id.tilDocType))
//                    maybeHideBottomError()
//                }
//            }
//        })
//
//        // Контрагент
//        etContractor.addTextChangedListener(object : android.text.TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun afterTextChanged(s: android.text.Editable?) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (!s.isNullOrBlank()) {
//                    clearFieldError(tilContractor)
//                    maybeHideBottomError()
//                }
//            }
//        })
//
//        // №ТТН
//        etInvoice.addTextChangedListenerSimple { text ->
//            if (text.trim().isNotEmpty()) {
//                clearFieldError(tilInvoice)
//                maybeHideBottomError()
//            }
//        }
//
//        // Дата (у тебя задаётся программно из календаря, но на всякий случай)
//        etDate.addTextChangedListenerSimple { text ->
//            if (text.trim().isNotEmpty()) {
//                clearFieldError(tilDate)
//                maybeHideBottomError()
//            }
//        }
//    }
//
//    private fun onScanInvoiceRaw(raw: String) {
//        val digits = raw.filter { it.isDigit() }
//        if (digits.isBlank()) return
//
//        val invoice = if (digits.length >= 7) digits.takeLast(7) else digits
//        etInvoice.setText(invoice)
//        etInvoice.setSelection(invoice.length)
//
//        // если у тебя есть живая валидация — ошибка снимется автоматически,
//        // но на всякий случай:
//        clearFieldError(tilInvoice)
//    }
//
//}
