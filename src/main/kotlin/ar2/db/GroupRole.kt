package ar2.db

import ar2.users.Role
import org.jetbrains.exposed.dao.IntIdTable
import javax.persistence.*

@Entity
@Table(name = "group_roles")
data class GroupRole(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "group_id", nullable = false)
    var groupId: Int,

    @Column(name = "user_id", nullable = false)
    var userId: Int,

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    var role: Role
)

