package by.nik.warehouseapp.features.returns.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import by.nik.warehouseapp.R
import by.nik.warehouseapp.core.data.InMemoryRepository
import by.nik.warehouseapp.features.returns.model.ReturnDocument
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast


class ReturnListActivity : AppCompatActivity() {

    private lateinit var listContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_return_list)

        listContainer = findViewById(R.id.listContainer)

        val btnAddReturn = findViewById<Button>(R.id.btnAddReturn)
        btnAddReturn.setOnClickListener {
            startActivity(Intent(this, ReturnCreateActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        renderList()
    }

    private fun renderList() {
        listContainer.removeAllViews()

        val docs = InMemoryRepository.returns
        if (docs.isEmpty()) {
            val tv = TextView(this).apply {
                text = "Документов пока нет"
                textSize = 16f
                setPadding(8, 16, 8, 16)
            }
            listContainer.addView(tv)
            return
        }

        docs.forEach { doc ->
            listContainer.addView(createDocView(doc))
        }
    }

    private fun createDocView(doc: ReturnDocument): View {
        val view = LayoutInflater.from(this).inflate(R.layout.item_return_doc, listContainer, false)

        val tvContractor = view.findViewById<TextView>(R.id.tvContractor)
        val tvInvoice = view.findViewById<TextView>(R.id.tvInvoice)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

        val btnDetails = view.findViewById<Button>(R.id.btnDetails)
        val btnItems = view.findViewById<Button>(R.id.btnItems)

        tvContractor.text = "Контрагент: ${doc.contractorName}"
        tvInvoice.text = "ТТН № ${doc.invoiceNumber}"
        tvStatus.text = "${doc.documentDate} • ${docTypeText(doc.docType)}\n${statusText(doc.status)} • Приёмка: ${doc.acceptanceDate}"

        btnDetails.setOnClickListener {
            Toast.makeText(this, "Детали (позже сделаем раскрытие)", Toast.LENGTH_SHORT).show()
        }

        btnItems.setOnClickListener {
            val intent = Intent(this, ReturnItemsActivity::class.java)
            intent.putExtra(ReturnItemsActivity.EXTRA_RETURN_ID, doc.id)
            startActivity(intent)
        }

        return view
    }

    private fun docTypeText(type: by.nik.warehouseapp.features.returns.model.ReturnDocType): String {
        return when (type) {
            by.nik.warehouseapp.features.returns.model.ReturnDocType.RETURN_INVOICE -> "Возвратная накладная"
            by.nik.warehouseapp.features.returns.model.ReturnDocType.DISCREPANCY_ACT -> "Акт расхождения"
        }
    }

    private fun statusText(status: by.nik.warehouseapp.features.returns.model.ReturnStatus): String {
        return when (status) {
            by.nik.warehouseapp.features.returns.model.ReturnStatus.CREATED -> "СОЗДАН"
            by.nik.warehouseapp.features.returns.model.ReturnStatus.IN_WORK -> "В РАБОТЕ"
            by.nik.warehouseapp.features.returns.model.ReturnStatus.ACCEPTED -> "ПРИНЯТ"
            by.nik.warehouseapp.features.returns.model.ReturnStatus.UPLOADED -> "ВЫГРУЖЕН"
        }
    }


}