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
import android.view.animation.AccelerateDecelerateInterpolator
import android.animation.ValueAnimator
import android.view.ViewGroup



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
        val tvInvoiceBadge = view.findViewById<TextView>(R.id.tvInvoiceBadge)
        val tvStatusBadge = view.findViewById<TextView>(R.id.tvStatusBadge)
        val tvDocInfo = view.findViewById<TextView>(R.id.tvDocInfo)
        val tvAcceptance = view.findViewById<TextView>(R.id.tvAcceptance)


        val btnDetails = view.findViewById<Button>(R.id.btnDetails)
        val btnItems = view.findViewById<Button>(R.id.btnItems)

        val detailsLayout = view.findViewById<View>(R.id.layoutDetails)
        val tvTotalQty = view.findViewById<TextView>(R.id.tvTotalQty)
        val tvTotalDef = view.findViewById<TextView>(R.id.tvTotalDef)

        val lines = InMemoryRepository.lines.filter { it.returnId == doc.id }
        val totalQty = lines.sumOf { it.quantity }
        val totalDef = lines.sumOf { it.defect }

        tvTotalQty.text = "Всего: $totalQty шт."
        tvTotalDef.text = "Брак: $totalDef шт."

        tvContractor.text = "Контрагент: ${doc.contractorName}"
        tvInvoiceBadge.text = "ТТН № ${doc.invoiceNumber}"
        tvStatusBadge.text = statusText(doc.status)

        val bgRes = when (doc.status) {
            by.nik.warehouseapp.features.returns.model.ReturnStatus.CREATED -> R.drawable.bg_badge_status_created
            by.nik.warehouseapp.features.returns.model.ReturnStatus.IN_WORK -> R.drawable.bg_badge_status_inwork
            by.nik.warehouseapp.features.returns.model.ReturnStatus.ACCEPTED -> R.drawable.bg_badge_status_accepted
            by.nik.warehouseapp.features.returns.model.ReturnStatus.UPLOADED -> R.drawable.bg_badge_status_uploaded
        }
        tvStatusBadge.setBackgroundResource(bgRes)

        tvDocInfo.text = "${doc.documentDate} • ${docTypeText(doc.docType)}"
        tvAcceptance.text = "Приёмка: ${doc.acceptanceDate}"

        btnDetails.text = "Детали"
        detailsLayout.visibility = View.GONE

        btnDetails.setOnClickListener {
            val isOpen = detailsLayout.visibility == View.VISIBLE
            if (isOpen) {
                collapse(detailsLayout)
                btnDetails.text = "Детали"
            } else {
                expand(detailsLayout)
                btnDetails.text = "Скрыть"
            }
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

    private fun expand(v: View) {
        v.measure(
            View.MeasureSpec.makeMeasureSpec((v.parent as View).width, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val targetHeight = v.measuredHeight

        v.layoutParams.height = 0
        v.visibility = View.VISIBLE

        ValueAnimator.ofInt(0, targetHeight).apply {
            duration = 180
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                v.layoutParams.height = animator.animatedValue as Int
                v.requestLayout()
            }
            start()
        }
    }

    private fun collapse(v: View) {
        val initialHeight = v.height

        ValueAnimator.ofInt(initialHeight, 0).apply {
            duration = 180
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                v.layoutParams.height = animator.animatedValue as Int
                v.requestLayout()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    v.visibility = View.GONE
                    v.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
            })
            start()
        }
    }

}