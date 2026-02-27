package by.nik.warehouseapp.features.returns.model

data class DeliveryPoint(
    val id: Long,
    val code: String,       // Код ТТ, например "00123"
    val address: String     // Адрес торговой точки
)

data class Contractor(
    val id: Long,
    val name: String,       // Наименование, например "ООО Табак-инвест"
    val unp: String,        // УНП, например "101234567"
    val deliveryPoints: List<DeliveryPoint>
)