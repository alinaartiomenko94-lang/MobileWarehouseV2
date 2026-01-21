package by.nik.warehouseapp.features.returns.model

data class ReturnLine(
    val id: Long,
    val returnId: Long,
    val product: Product,
    var quantity: Int,
    var defect: Int
)
