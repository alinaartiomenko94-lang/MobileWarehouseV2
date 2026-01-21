package by.nik.warehouseapp.features.returns.ui

import android.os.Bundle
import android.view.View
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
        val lineId = intent.getLongExtra(EXTRA_LINE_ID, -1L)
        val barcode = intent.getStringExtra(EXTRA_BARCODE).orEmpty()
        val scanMode = intent.getBooleanExtra(EXTRA_SCAN_MODE, false)

        val existingLineById = InMemoryRepository.lines.firstOrNull { it.id == lineId }

        // "Каталог" на старте очень простой: один тестовый товар + fallback по штрихкоду
        val product = resolveProduct(barcode)

        // Если зашли по редактированию конкретной строки — подставляем её значения
        if (existingLineById != null) {
            quantity = existingLineById.quantity
            defect = existingLineById.defect
        }

        // Логика повторного сканирования:
        // Если scanMode = true и товар уже есть в документе — показываем "уже в возврате"
        // и ставим quantity = (было в документе) + 1
        val existingByProduct = InMemoryRepository.lines.firstOrNull {
            it.returnId == returnId && it.product.barcode == product.barcode
        }

        val alreadyBlock = findViewById<View>(R.id.layoutAlreadyInReturn)
        val tvAlreadyQty = findViewById<TextView>(R.id.tvAlreadyQty)
        val tvAlreadyDef = findViewById<TextView>(R.id.tvAlreadyDef)

        if (scanMode && existingByProduct != null) {
            // Показать блок "Уже в возврате"
            alreadyBlock.visibility = View.VISIBLE
            tvAlreadyQty.text = "Уже в возврате: ${existingByProduct.quantity} шт."
            tvAlreadyDef.text = "Брак: ${existingByProduct.defect} шт."

            // Важно: quantity берём из ДОКУМЕНТА, а не из экрана → +1
            quantity = existingByProduct.quantity + 1
            defect = existingByProduct.defect
        } else {
            alreadyBlock.visibility = View.GONE
        }

        // Заполняем карточку товара
        findViewById<TextView>(R.id.tvProductName).text = product.name
        findViewById<TextView>(R.id.tvProductArticle).text = "Артикул: ${product.article}"
        findViewById<TextView>(R.id.tvProductBarcode).text = "Штрихкод: ${product.barcode}"

        val tvQty = findViewById<TextView>(R.id.tvQtyValue)
        val tvDef = findViewById<TextView>(R.id.tvDefValue)

        fun refresh() {
            tvQty.text = quantity.toString()
            tvDef.text = defect.toString()
        }

        // Количество
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

        // Брак
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

        // Отмена
        findViewById<Button>(R.id.btnCancel).setOnClickListener { finish() }

        // Сохранение:
        // 1) если редактируем по lineId → обновляем эту строку
        // 2) иначе если пришли из скана и товар уже был → обновляем найденную строку товара
        // 3) иначе → добавляем новую строку
        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            val targetLine = when {
                existingLineById != null -> existingLineById
                scanMode && existingByProduct != null -> existingByProduct
                else -> null
            }

            if (targetLine != null) {
                targetLine.quantity = quantity
                targetLine.defect = defect
            } else {
                val newLine = ReturnLine(
                    id = System.currentTimeMillis(),
                    returnId = returnId,
                    product = product,
                    quantity = quantity,
                    defect = defect
                )
                InMemoryRepository.lines.add(newLine)
            }

            finish()
        }

        refresh()
    }

    private fun resolveProduct(barcode: String): Product {
        // Тестовый товар по умолчанию
        val demo = Product(
            id = 1001L,
            name = "Набор для покера Poker chips (демо)",
            article = "ИН-3727",
            barcode = "4665303237278"
        )

        // Если штрихкод пустой — считаем, что сканнули демо-товар
        if (barcode.isBlank()) return demo

        // Если совпадает — тоже демо
        if (barcode == demo.barcode) return demo

        // Иначе создаём простой placeholder (потом заменим на поиск по каталогу)
        return Product(
            id = barcode.hashCode().toLong(),
            name = "Товар по штрихкоду $barcode",
            article = "—",
            barcode = barcode
        )
    }

    companion object {
        const val EXTRA_RETURN_ID = "extra_return_id"
        const val EXTRA_LINE_ID = "extra_line_id"
        const val EXTRA_BARCODE = "extra_barcode"
        const val EXTRA_SCAN_MODE = "extra_scan_mode"
    }
}