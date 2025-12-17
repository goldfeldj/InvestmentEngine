package PointFive

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object Items : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val value = double("value")
    override val primaryKey = PrimaryKey(id)
}

class PostgresItemRepository(database: Database) : ItemRepository {
    init {
        transaction(database) {
            SchemaUtils.create(Items)
        }
    }

    override suspend fun getAll(): List<Item> = newSuspendedTransaction {
        Items.selectAll().map { Item(it[Items.id], it[Items.name], it[Items.value]) }
    }

    override suspend fun getById(id: Int): Item? = newSuspendedTransaction {
        Items.select { Items.id eq id }
            .map { Item(it[Items.id], it[Items.name], it[Items.value]) }
            .singleOrNull()
    }

    override suspend fun add(item: Item): Item = newSuspendedTransaction {
        val result = Items.insert {
            it[name] = item.name
            it[value] = item.value
        }
        val generatedId = result[Items.id]
        item.copy(id = generatedId)
    }

    override suspend fun update(id: Int, item: Item): Boolean = newSuspendedTransaction {
        Items.update({ Items.id eq id }) {
            it[name] = item.name
            it[value] = item.value
        } > 0
    }

    override suspend fun delete(id: Int): Boolean = newSuspendedTransaction {
        Items.deleteWhere { Items.id eq id } > 0
    }
}