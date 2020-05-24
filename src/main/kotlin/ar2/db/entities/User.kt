package ar2.db.entities

import ar2.exceptions.NoPermissionException
import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDateTime
import javax.persistence.*
import javax.persistence.Column
import javax.persistence.Table

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(nullable = false)
    var username: String,

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    var passwordHash: String? = null,

    @Column(nullable = false)
    var email: String,

    @Column(nullable = false)
    var name: String,

    @Column(name = "is_admin", nullable = false)
    var isAdmin: Boolean,

    @Column(name = "created_on", nullable = false)
    var createdOn: LocalDateTime? = null,

    @Column(name = "last_login")
    var lastLogin: LocalDateTime? = null,

    @Column(name = "disabled")
    var disabled: Boolean = false
)

fun User.assertAdmin(msg: String? = null) {
    if (!isAdmin) throw NoPermissionException(
        msg ?: "You don't have permission to do this."
    )
}
