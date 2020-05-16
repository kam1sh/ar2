package ar2.lib.session

import ar2.users.Role
import ar2.web.views.GroupViews
import org.http4k.core.Method

class GroupsApi(val session: Session) {
    fun new(name: String) = session.request(Method.POST, "/api/v1/groups", GroupViews.GroupForm(name))
    fun remove(name: String) = session.request(Method.DELETE, "/api/v1/groups/$name")

    fun addRole(groupName: String, user: String, role: Role) =
        session.request(Method.PUT, "/api/v1/groups/$groupName/$user", GroupViews.RoleForm(role))
}
