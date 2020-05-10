package ar2.db

import ar2.users.Role
import javax.persistence.*

@Entity
@Table(name = "repository_roles")
data class RepositoryRole(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "repo_id", nullable = false)
    var repoId: Int,

    @Column(name = "user_id", nullable = false)
    var userId: Int,

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    var role: Role
)
