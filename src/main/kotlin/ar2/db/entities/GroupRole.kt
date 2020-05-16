package ar2.db.entities

import ar2.users.Role
import javax.persistence.*

@Entity
@Table(name = "group_roles")
data class GroupRole(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "user_id")
    var userId: Int? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    var user: User? = null,

    @Column(name = "group_id")
    var groupId: Int? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false, insertable = false, updatable = false)
    var group: Group? = null,

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    var role: Role? = null
)
