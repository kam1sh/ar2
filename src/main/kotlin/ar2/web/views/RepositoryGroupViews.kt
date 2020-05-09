package ar2.web.views

import ar2.db.Group
import ar2.services.GroupsService
import ar2.web.checkApiAcceptHeader
import ar2.web.checkApiCTHeader
import org.http4k.core.*
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.koin.core.KoinComponent
import org.koin.core.inject

class RepositoryGroupViews: KoinComponent {

    private val groupsService: GroupsService by inject()

    fun views() = routes(
            "/" bind Method.GET to ::listGroups,
            "/" bind Method.POST to ::newGroup
    )

    val listResponseLens = Body.auto<List<Group>>().toLens()

    private fun listGroups(request: Request): Response {
        request.checkApiAcceptHeader()
        val limit = request.query("limit") ?: "10"
        val offset = request.query("offset") ?: "0"
        val groups =  groupsService.listGroups(limit.toInt(), offset.toInt())
        return listResponseLens(groups, Response(Status.OK))
    }

    data class GroupForm(val name: String)
    val groupLens = Body.auto<GroupForm>().toLens()

    private fun newGroup(request: Request): Response {
        request.checkApiAcceptHeader()
        request.checkApiCTHeader()
        val groupForm = groupLens(request)
        groupsService.newGroup(groupForm.name)
        return Response(Status.CREATED)
    }
}
