package ar2.users

open class BaseUser(var username: String, var email: String, var name: String, var admin: Boolean)
class User(val id: Int, username: String, email: String, name: String, admin: Boolean) : BaseUser(username, email, name, admin)
