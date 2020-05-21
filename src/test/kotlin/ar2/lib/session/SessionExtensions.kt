package ar2.lib.session

val adminCredentials = Credentials("testadmin", "test")

fun adminSession(): Session {
    val sess = Session(adminCredentials)
    sess.login()
    return sess
}
