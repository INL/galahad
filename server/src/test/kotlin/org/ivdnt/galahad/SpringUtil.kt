package org.ivdnt.galahad

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.data.corpus.Corpus
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.io.File

object JSON {
    val mapper = ObjectMapper()
    inline fun <reified T> fromStr(json: String): T = mapper.readValue(json, object : TypeReference<T>() {})
    fun toStr(it: Any): String = mapper.writeValueAsString(it)
}

object UserHeader {
    fun get(username: String = "testUser"): HttpHeaders {
        val map: MutableMap<String, String> = HashMap()
        map["remote_user"] = username
        val httpHeaders = HttpHeaders()
        httpHeaders.setAll(map)
        return httpHeaders
    }
}

fun createCorpus(config: Config): Corpus {
    val workdir = config.getWorkingDirectory().resolve("corpora").resolve("custom")
    return org.ivdnt.galahad.port.createCorpus(workdir)
}

fun MockMvc.uploadFile(file: File, corpus: Corpus, mediaType: String = MediaType.TEXT_PLAIN_VALUE): MvcResult {
    // Create file
    val mockFile = MockMultipartFile(
        "file",
        file.name,
        mediaType,
        file.readBytes()
    )
    // Perform request
    val uuid = corpus.metadata.expensiveGet().uuid
    return this.perform(
        MockMvcRequestBuilders.multipart("/corpora/$uuid/documents")
            .file(mockFile)
            .headers(UserHeader.get())
    ).andReturn()
}