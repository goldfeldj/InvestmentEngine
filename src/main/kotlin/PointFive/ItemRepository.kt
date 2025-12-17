package PointFive

interface ItemRepository {
    suspend fun getAll(): List<Item>
    suspend fun getById(id: Int): Item?
    suspend fun add(item: Item): Item
    suspend fun update(id: Int, item: Item): Boolean
    suspend fun delete(id: Int): Boolean
}