package org.ivdnt.galahad.data.corpus

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.FileBackedValue
import org.ivdnt.galahad.app.JSONable
import org.ivdnt.galahad.app.User
import org.springframework.lang.Nullable
import java.net.URL

/**
 * Corpus metadata that can be changed by the user.
 * Although technically [owner] should only be set once.
 */
open class MutableCorpusMetadata(
    @JsonProperty("owner") var owner: String,
    @JsonProperty("name") var name: String,
    @JsonProperty("eraFrom") val eraFrom: Int,
    @JsonProperty("eraTo") val eraTo: Int,
    @JsonProperty("tagset") var tagset: String?,
    @JsonProperty("dataset") @JsonInclude(JsonInclude.Include.ALWAYS) val isDataset: Boolean,
    @JsonProperty("public") @JsonInclude(JsonInclude.Include.ALWAYS) var isPublic: Boolean,
    @JsonProperty("collaborators") @Nullable
    var collaborators: Set<String>?, // Empty lists show up as null after serialization
    @JsonProperty("viewers") @Nullable var viewers: Set<String>?,
    @JsonProperty("sourceName") @Nullable var sourceName: String?,
    @JsonProperty("sourceURL") @Nullable val sourceURL: URL?,
) : JSONable {

    /**
     * Whether the user is in the list of collaborators of this corpus.
     * Note that this is not the same as having write access: use [hasWriteAccess].
     */
    fun isCollaborator(user: User): Boolean {
        return collaborators?.contains(user.id) ?: false
    }

    /**
     * Whether the user is in the list of viewers of this corpus.
     * Note that this is not the same as having read access: use [hasReadAccess].
     */
    fun isViewer(user: User): Boolean {
        return viewers?.contains(user.id) ?: false
    }

    /** To have write access, you need to be an owner, collaborator or admin. */
    fun hasWriteAccess(user: User): Boolean {
        if (user.isAdmin) return true
        if (owner == user.id) return true
        return isCollaborator(user)
    }

    /** Only the owner can delete a corpus, unless you are an admin. */
    fun canDelete(user: User): Boolean {
        if (user.isAdmin) return true
        return owner == user.id
    }

    /** Only the owner can add new collaborators and viewers, unless you are an admin. */
    fun canAddNewUsers(user: User): Boolean {
        if (user.isAdmin) return true
        return owner == user.id
    }

    /** Only admins can make corpora public. */
    fun canMakePublic(user: User): Boolean {
        return user.isAdmin
    }

    /**
     * You can view a corpus if you are a viewer, collaborator or owner of that corpus, or if it's public.
     * Although admins have access to everything, you might not want to see all corpora listed in your own view,
     * so optionally exclude them.
     */
    fun hasReadAccess(user: User, excludeAdmin: Boolean = false): Boolean {
        if (!excludeAdmin) {
            if (user.isAdmin) return true
        }
        if (isDataset) return true // technically, datasets are always public, but still.
        if (isPublic) return true
        if (isCollaborator(user)) return true
        if (isViewer(user)) return true
        if (owner == user.id) return true
        return false
    }

    companion object {
        /** [FileBackedValue] needs a default value. */
        fun initValue(): MutableCorpusMetadata {
            return MutableCorpusMetadata(
                owner = "",
                name = "",
                eraFrom = 0,
                eraTo = 0,
                tagset = null,
                isDataset = false,
                isPublic = false,
                collaborators = null,
                sourceName = null,
                sourceURL = null,
                viewers = null,
            )
        }
    }
}