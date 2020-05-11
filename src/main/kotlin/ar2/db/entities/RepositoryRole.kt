package ar2.db.entities

import ar2.users.Role
import javax.persistence.*

@Entity
@Table(name = "repository_roles")
data class RepositoryRole(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_id", nullable = false)
    var repo: Repository,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    var role: Role
)
