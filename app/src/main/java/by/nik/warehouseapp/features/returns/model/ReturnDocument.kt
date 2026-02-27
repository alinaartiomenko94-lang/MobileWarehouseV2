package by.nik.warehouseapp.features.returns.model

data class ReturnDocument(
    val id: Long,
    val docType: ReturnDocType,
    val invoiceNumber: String,
    val documentDate: String,
    val acceptanceDate: String,
    val contractorName: String,
    val contractorUnp: String = "",
    val ttCode: String = "",
    val ttAddress: String = "",
    val status: ReturnStatus
)