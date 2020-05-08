package ar2.users

enum class Role(code: Int) {
    NONE(0),
    GUEST(10),
    DEVELOPER(20),
    MAINTAINER(30)
}
