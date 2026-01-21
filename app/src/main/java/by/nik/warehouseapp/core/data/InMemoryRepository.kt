package by.nik.warehouseapp.core.data

import by.nik.warehouseapp.features.returns.model.ReturnDocument
import by.nik.warehouseapp.features.returns.model.ReturnLine


object InMemoryRepository {
    val returns: MutableList<ReturnDocument> = mutableListOf()
    val lines: MutableList<ReturnLine> = mutableListOf()
}