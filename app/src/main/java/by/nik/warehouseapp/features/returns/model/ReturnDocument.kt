package by.nik.warehouseapp.features.returns.model

data class ReturnDocument(
    val id: Long,
    val docType: ReturnDocType,
    val invoiceNumber: String,
    val documentDate: String,   // пока строкой, позже сделаем нормальную дату
    val acceptanceDate: String, // пока строкой
    val contractorName: String,
    val status: ReturnStatus
)
