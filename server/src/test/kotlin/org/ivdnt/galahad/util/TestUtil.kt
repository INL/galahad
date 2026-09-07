package org.ivdnt.galahad.util

import java.io.File
import java.net.URL
import java.util.*
import kotlin.io.path.createTempDirectory
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.corpora.Corpora
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.corpora.CorpusMetadata
import org.ivdnt.galahad.metadata.Period
import org.ivdnt.galahad.metadata.Source
import org.springframework.http.HttpHeaders

object TestUtil {
    const val TAGGER: String = "pie-tdn-all"
    const val TAGSET_NAME: String = "TDN-Core"
    const val USER: String = "testUser"
    const val WEB_CORPUS: String = "web/corpus"

    fun get(path: String): File = File(this::class.java.classLoader.getResource(path)!!.toURI())

    fun createJobbedCorpus(config: Config, dataset: Boolean = false): Corpus {
        // contains docs
        val corpus = createFilledCorpus(config, dataset)
        // add layer
        val file = get(WEB_CORPUS).listFiles().first()
        corpus.layers.createOrThrow(TAGGER)
        // create job in progress with 1 finished
        config
            .getWorkingDirectory()
            .resolve("corpora/user/${corpus.uuid}")
            .resolve("jobs")
            .resolve(TAGGER)
            .resolve("documents")
            .resolve(file.withoutFormatExt)
            .mkdirs()
        return corpus
    }

    fun createFilledCorpus(config: Config, dataset: Boolean = false): Corpus {
        val corpus = createCorpus(config, dataset)
        val files = get(WEB_CORPUS).listFiles()
        files.forEach { corpus.documents.createOrThrow(it) }
        return corpus
    }

    fun createCorpus(config: Config? = null, dataset: Boolean = false): Corpus {
        val parent =
            config?.getWorkingDirectory()?.resolve("corpora")?.resolve("user")
                ?: createTempDirectory().toFile()
        val corpora = Corpora(parent)
        val user = if (dataset) User("admin") else User(USER)
        val meta =
            CorpusMetadata(
                "testCorpus",
                user.name,
                dataset,
                Period(1200, 1300),
                "Dutch",
                "TDN-Core",
                Source("source name", URL("http://source.url")),
                mutableSetOf("collaborator"),
                mutableSetOf("viewer"),
            )
        meta.user = user
        meta.id = UUID.randomUUID()
        return corpora.createOrThrow(meta)
    }

    fun assignHeaders(headers: HttpHeaders, user: String = USER) {
        headers.set(User.USER_HEADER, user)
    }
}
