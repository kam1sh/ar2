package ar2.db.entities

import javax.persistence.*

@Entity
@Table(name = "repos")
data class Repository(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int,

    var name: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: Group
)
