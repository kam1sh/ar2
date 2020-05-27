package ar2.tests.e2e

import EndToEndTest
import ar2.web.WebHandler
import kotlin.test.assertEquals
import org.http4k.core.*
import org.http4k.lens.MultipartFormFile
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest
import org.koin.test.get

@EndToEndTest
class PyPIViewsTest : KoinTest {

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

        val request = Request(Method.POST, "/api/py/test/test/upload")
                .header("Authorization", "Basic dGVzdGFkbWluOnRlc3Q=")
                .header("content-type", "multipart/form-data; boundary=${body.boundary}")
                .body(body)
        val resp = get<WebHandler>()(request)
        assertEquals(Status.CREATED, resp.status)
    }
}
