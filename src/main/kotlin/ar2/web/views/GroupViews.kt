package ar2.web.views

import ar2.db.entities.Group
import ar2.exceptions.*
import ar2.exceptions.user.NoSuchUserException
import ar2.services.GroupsService
import ar2.services.UsersService
import ar2.services.extractUser
import ar2.users.Role
import ar2.web.*
import org.http4k.core.*
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.koin.core.KoinComponent

class GroupViews(
    val service: GroupsService,
    val usersService: UsersService
) : KoinComponent {

    fun views() = routes(
            "/" bind Method.GET to ::listGroups,
            "/" bind Method.POST to ::newGroup,
            "/{group}" bind Method.DELETE to ::remove,
            "/{group}/{username}" bind Method.PUT to ::addUserToGroup,
            "/{group}/{username}" bind Method.GET to ::getUserGroupRole
    )

    val listResponseLens = Body.auto<List<Group>>().toLens()

    private fun listGroups(request: Request): Response {
        request.checkApiAcceptHeader()
        val pr = request.toPageRequest()
        val groups = service.list(pr)
        return listResponseLens(groups, Response(Status.OK))
    }

    data class GroupForm(val name: String)
    val groupLens = Body.auto<GroupForm>().toLens()

    private fun newGroup(request: Request): Response {
        request.checkApiAcceptHeader()
        request.checkApiCTHeader()
        val groupForm = groupLens(request)
        service.new(groupForm.name, extractUser(request))
        return Response(Status.CREATED)
    }

    private fun remove(request: Request): Response {
        val name = request.path("group")!!
        service.remove(name)
        return Response(Status.NO_CONTENT)
    }

    data class RoleForm(val role: Role)
    val roleFormLens = Body.auto<RoleForm>().toLens()
    private fun addUserToGroup(request: Request): Response {
        request.checkApiCTHeader()
        val username = request.path("username")!!
        val group = request.path("group")!!
        val form = roleFormLens(request)
        val user = try {
            usersService.find(username)
        } catch (e: NoSuchUserException) {
            throw WebError(e, Status.BAD_REQUEST)
        }
        try {
            service.addUserRole(group, user, form.role)
        } catch (exc: NoSuchGroupException) {
            return exc.toHTTPResponse().status(Status.BAD_REQUEST)
        }
        return Response(Status.NO_CONTENT)
    }

    private fun getUserGroupRole(request: Request): Response {
        val username = request.path("username")!!
        val group = request.path("group")!!
        val user = usersService.find(username)
        val role = service.findUserRole(groupName = group, user = user) ?: throw WebError(
            Status.NOT_FOUND,
            "Role is not attached.",
            "ROLE_NOT_FOUND"
        )
        return roleFormLens(RoleForm(role), Response(Status.OK))
    }
}
