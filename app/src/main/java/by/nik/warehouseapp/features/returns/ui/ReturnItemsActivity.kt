package by.nik.warehouseapp.features.returns.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import by.nik.warehouseapp.R
import by.nik.warehouseapp.core.data.InMemoryRepository
import android.content.Intent
import android.widget.Button


class ReturnItemsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_return_items)

        val returnId = intent.getLongExtra(EXTRA_RETURN_ID, -1L)

        val tvHeader = findViewById<TextView>(R.id.tvHeader)
        val doc = InMemoryRepository.returns.firstOrNull { it.id == returnId }

        tvHeader.text = if (doc == null) {
            "Документ не найден"
        } else {
            "Контрагент: ${doc.contractorName}\n" +
                    "ТТН № ${doc.invoiceNumber}\n" +
                    "${doc.documentDate} • ${doc.docType}\n" +
                    "Статус: ${doc.status} • Приёмка: ${doc.acceptanceDate}"
        }

        val btnAddItem = findViewById<Button>(R.id.btnAddItem)
        val btnScanItem = findViewById<Button>(R.id.btnScanItem)

        btnAddItem.setOnClickListener {
            val intent = Intent(this, ReturnItemEditActivity::class.java)
            intent.putExtra(ReturnItemEditActivity.EXTRA_RETURN_ID, returnId)
            startActivity(intent)
        }

        btnScanItem.setOnClickListener {
            val intent = Intent(this, ReturnItemEditActivity::class.java)
            intent.putExtra(ReturnItemEditActivity.EXTRA_RETURN_ID, returnId)
            startActivity(intent)
        }

    }

    companion object {
        const val EXTRA_RETURN_ID = "extra_return_id"
    }
}