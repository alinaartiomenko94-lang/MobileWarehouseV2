package by.nik.warehouseapp.core.data

import by.nik.warehouseapp.features.returns.model.Contractor
import by.nik.warehouseapp.features.returns.model.DeliveryPoint

object ContractorRepository {

    val all: List<Contractor> = listOf(

        Contractor(
            id = 1,
            name = "ООО Табак-инвест",
            unp = "101234567",
            deliveryPoints = listOf(
                DeliveryPoint(1, "00101", "ул. Ленина, 5"),
                DeliveryPoint(2, "00102", "пр. Победы, 12"),
                DeliveryPoint(3, "00103", "ул. Советская, 8")
            )
        ),

        Contractor(
            id = 2,
            name = "ИП Самкова Е.В.",
            unp = "200345678",
            deliveryPoints = listOf(
                DeliveryPoint(4, "00201", "ул. Садовая, 3")
            )
        ),

        Contractor(
            id = 3,
            name = "ООО Ромашка",
            unp = "300456789",
            deliveryPoints = listOf(
                DeliveryPoint(5, "00301", "пр. Мира, 45"),
                DeliveryPoint(6, "00302", "ул. Цветочная, 2")
            )
        ),

        Contractor(
            id = 4,
            name = "ИП Смирнов А.А.",
            unp = "400567890",
            deliveryPoints = listOf(
                DeliveryPoint(7, "00401", "ул. Лесная, 17")
            )
        )
    )

    // Поиск по названию или УНП
    fun search(query: String): List<Contractor> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return all
        return all.filter {
            it.name.lowercase().contains(q) || it.unp.contains(q)
        }
    }
}