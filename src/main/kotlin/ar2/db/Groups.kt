package ar2.db

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Groups : IntIdTable("groups") {
    val name = varchar("name", 64)

    fun findIdByName(name: String): Int = transaction {
        Groups.select { Groups.name eq name }.single()[Groups.id].value
    }

    fun findAll(offset: Int, limit: Int): List<Group> = transaction {
        selectAll().limit(limit, offset).map { it.toGroup() }
    }
}

data class Group(val id: Int, val name: String)

fun ResultRow.toGroup() = Group(
        id = this[Groups.id].value,
        name = this[Groups.name]
)
