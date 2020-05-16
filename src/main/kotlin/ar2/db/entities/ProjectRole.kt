package ar2.db.entities

import ar2.users.Role
import javax.persistence.*

@Entity
@Table(name = "project_roles")
data class ProjectRole(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    var project: Project,

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    var role: Role
)
