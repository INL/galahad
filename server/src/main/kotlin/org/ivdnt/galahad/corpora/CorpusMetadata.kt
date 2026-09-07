package org.ivdnt.galahad.corpora

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.exceptions.CorpusInvalidException
import org.ivdnt.galahad.exceptions.UserUnauthorizedException
import org.ivdnt.galahad.metadata.Period
import org.ivdnt.galahad.metadata.Source

/**
 * Corpus metadata that can be changed by the user. Although technically [owner] should only be set
 * once.
 */
open class CorpusMetadata(
    var name: String,
    var owner: String? = null,
    var dataset: Boolean? = null,
    var period: Period? = null,
    var language: String? = null,
    var tagset: String? = null,
    var source: Source? = null,
    var collaborators: MutableSet<String>? = null,
    var viewers: MutableSet<String>? = null,
) {

    @JsonIgnore var id: UUID? = null

    @JsonIgnore var user: User? = null

    @get:JsonIgnore
    val langCode: String
        // iso 639 code. "und" for undefined.
        get() =
            Locale.getAvailableLocales()
                .find { it.getDisplayLanguage(Locale.ENGLISH).equals(language, true) }
                ?.isO3Language ?: "und"

    /**
     * Whether the user is in the list of collaborators of this corpus. Note that this is not the
     * same as having write access: use [canWrite].
     */
    private fun isCollaborator(user: User): Boolean = collaborators?.contains(user.name) == true

    /**
     * Whether the user is in the list of viewers of this corpus. Note that this is not the same as
     * having read access: use [canRead].
     */
    private fun isViewer(user: User): Boolean = viewers?.contains(user.name) == true

    /** To have write access, you need to be an owner, collaborator or admin. */
    fun canWrite(user: User): Boolean {
        if (user.admin) return true
        if (owner == user.name) return true
        return isCollaborator(user)
    }

    /** Only the owner can delete a corpus, unless you are an admin. */
    fun canDelete(user: User): Boolean {
        if (user.admin) return true
        return owner == user.name
    }

    /** Only the owner and admin can add new collaborators and viewers. */
    private fun canAddNewUsers(user: User): Boolean {
        if (user.admin) return true
        return owner == user.name
    }

    private fun removeAsViewer(user: User) {
        viewers?.removeIf { i -> i == user.name }
    }

    private fun removeAsCollaborator(user: User) {
        collaborators?.removeIf { i -> i == user.name }
    }

    /**
     * You can view a corpus if you are a viewer, collaborator or owner of that corpus, or if it's
     * public. Although admins have access to everything, you might not want to see all corpora
     * listed in your own view, so optionally exclude them.
     */
    fun canRead(user: User, excludeAdmin: Boolean = false): Boolean {
        if (!excludeAdmin) {
            if (user.admin) return true
        }
        if (dataset == true) return true
        if (isCollaborator(user)) return true
        if (isViewer(user)) return true
        if (owner == user.name) return true
        return false
    }

    companion object {
        private fun assertCorpusNameValidOrThrow(corpusName: String) {
            if (corpusName.isBlank()) {
                throw CorpusInvalidException("Corpus name is invalid.")
            }
        }

        /**
         * Clean up certain values in [newMeta], checking against [oldMeta], namely:
         * - overwrite [newMeta].owner with the original owner, unless it's a new corpus.
         * - ignore metadata changes if a user is no longer a viewer or collaborator.
         * - validate corpus name.
         * - trim textual inputs.
         * - validate permissions for changing collaborators, viewers, and dataset.
         * - If a user appears multiple times in the permission hierarchy, only the upper level
         *   remains.
         */
        fun clean(newMeta: CorpusMetadata, oldMeta: CorpusMetadata? = null): CorpusMetadata {
            val user = newMeta.user!!
            // Overwrite the owner with the original, so collaborators can't change it,
            // unless it's empty, in which case it's a new corpus.
            newMeta.owner = oldMeta?.owner ?: user.name

            // Is this user a viewer?
            if (oldMeta?.isViewer(user) == true) {
                // Viewers are allowed to remove themselves, but no more than that.
                if (!newMeta.isViewer(user)) {
                    oldMeta.removeAsViewer(user)
                    // ignore all other changes and return the old metadata, minus the viewer
                    return oldMeta
                } else {
                    // If the viewer is not removing themselves, this action is unauthorized.
                    throw UserUnauthorizedException("Cannot edit corpus.")
                }
            }

            // Same for collaborators
            if (!newMeta.isCollaborator(user) && oldMeta?.isCollaborator(user) == true) {
                oldMeta.removeAsCollaborator(user)
                // Although collaborators could change other metadata,
                // if you have chosen to remove yourself as a collaborator,
                // you probably don't want to change anything else.
                return oldMeta
            }

            assertCorpusNameValidOrThrow(newMeta.name)

            if (oldMeta?.dataset != true && newMeta.dataset == true) {
                // Corpus is being set to public
                if (!user.admin) {
                    throw UserUnauthorizedException("Cannot create a dataset.")
                }
            }

            if (
                oldMeta?.collaborators != newMeta.collaborators ||
                    oldMeta?.viewers != newMeta.viewers
            ) {
                // Collaborators have changed
                if (!newMeta.canAddNewUsers(user)) {
                    throw UserUnauthorizedException("Cannot change collaborators or viewers.")
                }
            }

            // Trim textual inputs
            newMeta.apply {
                name = name.trim()
                source?.name = source?.name?.trim()
                tagset = tagset?.trim()
                language = language?.trim()
                collaborators = collaborators?.map { it.trim() }?.toMutableSet()
                viewers = viewers?.map { it.trim() }?.toMutableSet()
            }

            // Remove owner from list of collaborators & viewers
            newMeta.collaborators?.remove(newMeta.owner)
            newMeta.viewers?.remove(newMeta.owner)

            // Remove collaborators from list of viewers
            newMeta.viewers?.removeIf { newMeta.collaborators?.contains(it) == true }

            return newMeta
        }
    }
}
