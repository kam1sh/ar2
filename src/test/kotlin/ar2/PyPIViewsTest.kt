package ar2

import org.http4k.core.*
import org.http4k.lens.MultipartFormFile
import org.junit.Test
import java.io.OutputStreamWriter
import kotlin.test.assertEquals

class PyPIViewsTest : BaseTest() {
    @Test
    fun testUpload() {
        var body = MultipartFormBody()
                .plus("name" to "example")
                .plus("version" to "0.1.0")
                .plus("filetype" to "bdist_wheel")
                .plus("pyversion" to "py3")
                .plus("metadata_version" to "2.1")
                .plus("summary" to "example project")
                .plus("author" to "someone?")
                .plus("md5_digest" to "addf2c8a736bf77187ebcc4ff7368251")
                .plus("sha256_digest" to "95560ca9fff81ee83bf10725ecd11fb85b350630d8d02f5aed918d7700586b06")
                .plus("blake2_256_digest" to "19edfcc12dac1cccb4772c3894f7f45666dde8a2c7f9d1d4524f0882e0cead61")
                .plus("requires_python" to ">=3.8,<4.0")
                .plus(":action" to "file_upload")
                .plus("protocol_version" to "1")

        val emptyFields = listOf(
                "home_page", "author_email", "maintainer",
                "maintainer_email", "license", "description",
                "keywords", "download_url", "comment"
        )
        emptyFields.forEach { body = body.plus(it to "") }
        body = body.plus("file" to MultipartFormFile("example-0.1.0-py3-none-any.whl", ContentType.OCTET_STREAM, "content".byteInputStream()))

        val request = Request(Method.POST, "/py/test/test/upload")
            .header("Authorization", "Basic dGVzdGFkbWluOnRlc3Q=")
            .body(body)
        val resp = app.getWebHandler()(request)
        assertEquals(Status.INTERNAL_SERVER_ERROR, resp.status)
    }

    @Test
    fun testEmptyField() {
        val handler: HttpHandler = {request ->
            for (item in request.multipartIterator()) {
                when (item) {
                    is MultipartEntity.Field -> println("${item.name} = ${item.value}")
                }
            }
            Response(Status.OK)
        }
        val request = Request(Method.POST, "/")
                .body(MultipartFormBody().plus("test" to ""))
        assertEquals(Status.OK, handler(request).status)
    }
}