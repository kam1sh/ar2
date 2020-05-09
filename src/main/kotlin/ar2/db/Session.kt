package ar2.db

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "users_sessions")
data class Session(
    @Id
    @Column(name = "session_key")
    var key: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User,

    @Column
    var expires: LocalDateTime
)
