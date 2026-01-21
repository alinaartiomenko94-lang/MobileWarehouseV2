package by.nik.warehouseapp.features.returns.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import by.nik.warehouseapp.R
import by.nik.warehouseapp.core.data.InMemoryRepository
import by.nik.warehouseapp.features.returns.model.ReturnLine
import android.app.AlertDialog
import android.widget.EditText

class ReturnItemsActivity : AppCompatActivity() {

    private lateinit var listContainer: LinearLayout
    private lateinit var tvEmpty: TextView
    private lateinit var tvHeader: TextView
    private lateinit var tvTotals: TextView

    private var returnId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_return_items)

        returnId = intent.getLongExtra(EXTRA_RETURN_ID, -1L)

        tvHeader = findViewById(R.id.tvHeader)
        tvTotals = findViewById(R.id.tvTotals)
        tvEmpty = findViewById(R.id.tvEmpty)
        listContainer = findViewById(R.id.listContainer)

        // Шапка документа
        val doc = InMemoryRepository.returns.firstOrNull { it.id == returnId }
        tvHeader.text = if (doc == null) {
            "Документ не найден"
        } else {
            "Контрагент: ${doc.contractorName}\n" +
                    "ТТН № ${doc.invoiceNumber}\n" +
                    "${doc.documentDate} • ${doc.docType}\n" +
                    "Статус: ${doc.status} • Приёмка: ${doc.acceptanceDate}"
        }

        // Кнопки снизу → открываем экран товара
        val btnAddItem = findViewById<Button>(R.id.btnAddItem)
        val btnScanItem = findViewById<Button>(R.id.btnScanItem)

        val openAddItem = {
            val intent = Intent(this, ReturnItemEditActivity::class.java)
            intent.putExtra(ReturnItemEditActivity.EXTRA_RETURN_ID, returnId)
            startActivity(intent)
        }

        btnAddItem.setOnClickListener { openAddItem() }
        btnScanItem.setOnClickListener {
            val input = EditText(this)
            input.hint = "Штрихкод (пусто = демо)"

            AlertDialog.Builder(this)
                .setTitle("Сканирование товара")
                .setMessage("Введите штрихкод (пока вместо реального сканера)")
                .setView(input)
                .setPositiveButton("ОК") { _, _ ->
                    val barcode = input.text?.toString()?.trim().orEmpty()

                    val intent = Intent(this, ReturnItemEditActivity::class.java)
                    intent.putExtra(ReturnItemEditActivity.EXTRA_RETURN_ID, returnId)
                    intent.putExtra(ReturnItemEditActivity.EXTRA_BARCODE, barcode)
                    intent.putExtra(ReturnItemEditActivity.EXTRA_SCAN_MODE, true)
                    startActivity(intent)
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

    }

    override fun onResume() {
        super.onResume()
        renderLines()
    }

    private fun renderLines() {
        listContainer.removeAllViews()

        val lines = InMemoryRepository.lines.filter { it.returnId == returnId }

        // Итоги
        val totalQty = lines.sumOf { it.quantity }
        val totalDef = lines.sumOf { it.defect }
        tvTotals.text = "Всего: $totalQty • Брак: $totalDef"

        if (lines.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            return
        } else {
            tvEmpty.visibility = View.GONE
        }

        lines.forEach { line ->
            listContainer.addView(createLineView(line))
        }
    }

    private fun createLineView(line: ReturnLine): View {
        val view = LayoutInflater.from(this).inflate(R.layout.item_return_line, listContainer, false)

        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvMeta = view.findViewById<TextView>(R.id.tvMeta)
        val tvQtyDef = view.findViewById<TextView>(R.id.tvQtyDef)

        tvName.text = line.product.name
        tvMeta.text = "Артикул: ${line.product.article} • ШК: ${line.product.barcode}"
        tvQtyDef.text = "Кол-во: ${line.quantity} • Брак: ${line.defect}"

        //по клику открывать редактирование этой строки
        view.setOnClickListener {
            val intent = Intent(this, ReturnItemEditActivity::class.java)
            intent.putExtra(ReturnItemEditActivity.EXTRA_RETURN_ID, returnId)
            intent.putExtra(ReturnItemEditActivity.EXTRA_LINE_ID, line.id)
            startActivity(intent)
        }

        return view
    }

    companion object {
        const val EXTRA_RETURN_ID = "extra_return_id"
    }
}