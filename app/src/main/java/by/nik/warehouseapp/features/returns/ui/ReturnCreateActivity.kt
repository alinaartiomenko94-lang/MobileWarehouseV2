package by.nik.warehouseapp.features.returns.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import by.nik.warehouseapp.R
import by.nik.warehouseapp.core.data.InMemoryRepository
import by.nik.warehouseapp.features.returns.model.ReturnDocType
import by.nik.warehouseapp.features.returns.model.ReturnDocument
import by.nik.warehouseapp.features.returns.model.ReturnStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.DatePickerDialog
import java.util.Calendar



class ReturnCreateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_return_create)

        val etInvoice = findViewById<EditText>(R.id.etInvoice)
        val etDocDate = findViewById<EditText>(R.id.etDocDate)
        val etContractor = findViewById<EditText>(R.id.etContractor)
        val btnCreate = findViewById<Button>(R.id.btnCreateReturn)

        etDocDate.setOnClickListener {
            showDatePicker(etDocDate)
        }


        btnCreate.setOnClickListener {
            val invoice = etInvoice.text?.toString()?.trim().orEmpty()
            val docDate = etDocDate.text?.toString()?.trim().orEmpty()
            val contractor = etContractor.text?.toString()?.trim().orEmpty()

            // пока без сложной валидации — лишь минимум
            val doc = ReturnDocument(
                id = System.currentTimeMillis(),
                docType = ReturnDocType.RETURN_INVOICE,
                invoiceNumber = invoice.ifBlank { "Без номера" },
                documentDate = docDate.ifBlank { "—" },      // дата документа (напечатанная)
                acceptanceDate = today(),                    // фактическая приёмка = сегодня
                contractorName = contractor.ifBlank { "Без контрагента" },
                status = ReturnStatus.IN_WORK
            )

            InMemoryRepository.returns.add(0, doc) // добавляем в начало списка
            finish() // возвращаемся назад на список
        }
    }

    private fun today(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun showDatePicker(target: EditText) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                target.setText(sdf.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }


}
