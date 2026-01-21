package by.nik.warehouseapp.features.returns.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import by.nik.warehouseapp.R
import by.nik.warehouseapp.core.data.InMemoryRepository
import by.nik.warehouseapp.features.returns.model.Product
import by.nik.warehouseapp.features.returns.model.ReturnLine

class ReturnItemEditActivity : AppCompatActivity() {

    private var quantity = 1
    private var defect = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_return_item_edit)

        val returnId = intent.getLongExtra(EXTRA_RETURN_ID, -1L)

        // Фейковый товар (пока). Потом придёт из поиска/сканирования.
        val product = Product(
            id = 1001L,
            name = "Игрушка мягкая «Зайчик» 20см",
            article = "ART-001",
            barcode = "4601234567890"
        )

        findViewById<TextView>(R.id.tvProductName).text = product.name
        findViewById<TextView>(R.id.tvProductArticle).text = "Артикул: ${product.article}"
        findViewById<TextView>(R.id.tvProductBarcode).text = "Штрихкод: ${product.barcode}"

        val tvQty = findViewById<TextView>(R.id.tvQtyValue)
        val tvDef = findViewById<TextView>(R.id.tvDefValue)

        fun refresh() {
            tvQty.text = quantity.toString()
            tvDef.text = defect.toString()
        }

        findViewById<Button>(R.id.btnQtyMinus).setOnClickListener {
            if (quantity > 1) {
                quantity--
                if (defect > quantity) defect = quantity
                refresh()
            }
        }

        findViewById<Button>(R.id.btnQtyPlus).setOnClickListener {
            quantity++
            refresh()
        }

        findViewById<Button>(R.id.btnDefMinus).setOnClickListener {
            if (defect > 0) {
                defect--
                refresh()
            }
        }

        findViewById<Button>(R.id.btnDefPlus).setOnClickListener {
            if (defect < quantity) {
                defect++
                refresh()
            } else {
                Toast.makeText(this, "Брак не может быть больше количества", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            val line = ReturnLine(
                id = System.currentTimeMillis(),
                returnId = returnId,
                product = product,
                quantity = quantity,
                defect = defect
            )
            InMemoryRepository.lines.add(line)
            finish()
        }

        refresh()
    }

    companion object {
        const val EXTRA_RETURN_ID = "extra_return_id"
    }
}