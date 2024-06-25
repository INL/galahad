package org.ivdnt.galahad.tagset

import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.TAGSETS_URL
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class TagsetController : Logging {

    val tagsets = TagsetStore()

    @GetMapping( TAGSETS_URL )
    @CrossOrigin
    fun getTagsets(): Set<Tagset> {
        logger.info( "Get tagsets" )
        return tagsets.tagsets
    }

    @GetMapping("$TAGSETS_URL/{tagset}")
    @CrossOrigin
    fun getTagset(
        @PathVariable("tagset") tagsetName: String
    ): Tagset {
        logger.info( "Get tagset for $tagsetName")
        val tagset = tagsets.getOrNull( tagsetName )
        return tagset ?: throw  ResponseStatusException(
                HttpStatus.NOT_FOUND, "tagset not found"
        )
    }

}