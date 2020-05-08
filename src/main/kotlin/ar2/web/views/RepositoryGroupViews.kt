package ar2.web.views

import ar2.db.Group
import ar2.db.Groups
import ar2.web.checkApiAcceptHeader
import ar2.web.checkApiCTHeader
import org.http4k.core.*
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes

class RepositoryGroupViews {

    fun views() = routes(
            "/" bind Method.GET to ::listGroups,
            "/" bind Method.POST to ::newGroup
    )

    val listResponseLens = Body.auto<List<Group>>().toLens()

    private fun listGroups(request: Request): Response {
        request.checkApiAcceptHeader()
        val limit = request.query("limit") ?: "10"
        val offset = request.query("offset") ?: "0"
        val groups = Groups.findAll(limit = limit.toInt(), offset = offset.toInt())
        return listResponseLens(groups, Response(Status.OK))
    }

    data class NewGroupRequest(val name: String)
    val groupLens = Body.auto<NewGroupRequest>().toLens()

    private fun newGroup(request: Request): Response {
        request.checkApiAcceptHeader()
        request.checkApiCTHeader()
        return Response(Status.CREATED)
    }
}
