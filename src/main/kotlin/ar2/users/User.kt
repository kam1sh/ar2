package ar2.users

data class BaseUser(val username: String, val email: String, val name: String, val admin: Boolean)
data class User(val id: Int, val obj: BaseUser) {
    val username: String
        get() = obj.username

    val email: String
        get() = obj.email

    val name: String
        get() = obj.name

    val admin: Boolean
        get() = obj.admin
}
