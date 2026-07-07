package org.ivdnt.galahad.jobs

import java.io.InputStream
import java.util.UUID
import kotlin.collections.ArrayDeque
import kotlin.collections.count
import kotlin.collections.minusAssign
import kotlin.collections.plusAssign
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile
import org.ivdnt.galahad.documents.Document
import org.ivdnt.galahad.taggers.Tagger
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

object JobScheduler {
    private val queue: ArrayDeque<Job> = ArrayDeque<Job>()
    private var task: Task? = null
    val queueSize: Int
        get() = queue.size

    fun inQueue(job: Job): Boolean = job in queue

    fun queue(job: Job) {
        if (job in queue) {
            return // Already in queue, nothing to do.
        }
        queue += job
        start()
    }

    fun reset() {
        queue.clear()
        task = null
    }

    fun dequeue(job: Job) {
        if (job in queue) {
            queue -= job
        }
        if (task?.job == job) {
            task = null
            terminate(job.name)
        }
        // next job now that this one is gone
        start()
    }

    fun receive(uuid: UUID, input: InputStream, fileName: String) {
        if (task == null || uuid != task?.uuid) {
            throw Exception("No task found for UUID $uuid")
        }
        task!!.finish(input, fileName)
        // if no untagged documents are left, remove the job from the queue and terminate the tagger
        val numUntagged =
            task!!.job.corpus.documents.readAll().count {
                task!!.job.results.readOrNull(it.name) == null
            }
        if (numUntagged == 0) {
            dequeue(task!!.job)
        }
        // next document, or next job if all documents are tagged
        task = null
        start()
    }

    private fun start() {
        // Only one task at a time.
        if (task == null) {
            // First job in queue if it exists.
            queue.firstOrNull()?.let { job ->
                // Get all docs each time, because new ones might be added while tagging.
                val docs = job.corpus.documents.readAllSequence()
                // Untagged are those for which no result exists.
                val untagged = docs.filter { job.results.readOrNull(it.name) == null }
                // Is there even an untagged document?
                val doc = untagged.firstOrNull()
                if (doc == null) {
                    // Nothing to tag, dequeue.
                    dequeue(job)
                } else {
                    // Tag and register task.
                    val id = tag(job, doc)
                    task = Task(id, job, doc.name)
                }
            }
        }
    }

    private fun tag(job: Job, doc: Document): UUID {
        // Before sending to the [job] tagger, terminate all others
        Tagger.taggers.values.filter { it.name != job.name }.forEach { terminate(it.name) }
        // Now send
        val url = "${Tagger.readOrThrow(job.name).url}/input"
        val text = doc.layer.toString()
        val file = createTempFile().toFile().also { it.writeText(text) }
        val entity =
            HttpEntity(
                LinkedMultiValueMap<String, Any>().apply { add("file", FileSystemResource(file)) },
                HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA },
            )
        val response = RestTemplate().postForEntity<String>(url, entity)
        if (response.statusCode != HttpStatus.OK) {
            throw Exception("Error while tagging: ${response.statusCode}")
        }
        return UUID.fromString(response.body) ?: throw Exception("No UUID received from tagger")
    }

    /** Terminate the tagger associated with this job. */
    private fun terminate(taggerName: String) {
        try {
            val url = "${Tagger.readOrThrow(taggerName).url}/terminate"
            RestTemplate().postForEntity<String>(url, null)
        } catch (e: Exception) {
            // Ignore. Can only hope tagger has terminated.
        }
    }

    private class Task(val uuid: UUID, val job: Job, val doc: String) {
        fun finish(input: InputStream, fileName: String) {
            try {
                val ext = fileName.substring(fileName.lastIndexOf('.'))
                val file = createTempDirectory().toFile().resolve(doc + ext)
                input.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
                job.results.createOrThrow(doc)
                job.corpus.layers.createOrThrow(job.name).documents.createOrThrow(file)
            } catch (exception: Exception) {
                job.results.createOrThrow(doc).error = exception.message
            }
        }
    }
}
