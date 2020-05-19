package ar2.exceptions.user

import ar2.db.entities.User
import ar2.exceptions.Ar2Exception
import org.http4k.core.Status

class UserExistsException(val user: User) : Ar2Exception("User exists.", "USER_EXISTS") {
    override val httpStatus = Status.CONFLICT
}
