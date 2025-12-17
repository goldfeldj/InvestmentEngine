package PointFive

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class InMemoryItemRepository : ItemRepository {
    private val items = ConcurrentHashMap<Int, Item>()
    private val idCounter = AtomicInteger(1)

    override suspend fun getAll(): List<Item> = items.values.toList()

    override suspend fun getById(id: Int): Item? = items[id]

    override suspend fun add(item: Item): Item {
        val id = idCounter.getAndIncrement()
        val newItem = item.copy(id = id)
        items[id] = newItem
        return newItem
    }

    override suspend fun update(id: Int, item: Item): Boolean {
        return if (items.containsKey(id)) {
            items[id] = item.copy(id = id)
            true
        } else {
            false
        }
    }

    override suspend fun delete(id: Int): Boolean = items.remove(id) != null
}