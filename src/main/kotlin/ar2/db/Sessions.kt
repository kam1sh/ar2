package ar2.db

import ar2.users.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object Sessions : Table("users_sessions") {
    val key = varchar("session_key", 40)
    val userId = integer("user_id").references(Users.id)
    val expires = datetime("expires")

    fun new(encodedCookie: String, user: User, expires: DateTime) {
        transaction {
            Sessions.insert {
                it[this.key] = encodedCookie
                it[this.userId] = user.id
                it[this.expires] = expires
            }
        }
    }

    fun findUser(cookieValue: String): User {
        return transaction {
            (Users leftJoin Sessions)
                    .slice(Users.id, Users.username, Users.email, Users.name, Users.lastLogin, Users.createdOn, Users.isAdmin)
                    .select { Sessions.key eq cookieValue }.single().toUser()
        }
    }

    fun pruneOld() {
        transaction {
            Sessions.deleteWhere { expires lessEq DateTime.now() }
        }
    }
}
