package ar2

import ar2.lib.session.Credentials
import ar2.lib.session.Session

fun adminSession(): Session {
    val sess = Session(Credentials("testadmin", "test"))
    sess.login()
    return sess
}