package ar2.db.entities

import ar2.users.Role
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
