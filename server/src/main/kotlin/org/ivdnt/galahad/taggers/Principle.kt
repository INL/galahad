package org.ivdnt.galahad.taggers

import java.io.File
import org.ivdnt.galahad.exceptions.PrincipleNotFoundException
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

data class Principle(
    val annotation: Annotation,
    val principle: Tagger.LinkItem,
) {
    // TODO for now this has its own yaml file
    // in the future we want to extract all unique principles from
    // all the tools in the platform to avoid duplication
    // Although that does mean we can't include principles we don't have taggers for
    companion object {
        private const val PRINCIPLES_FILE: String = "data/principles/principles.yaml"

        val principles: List<Principle> =
            Yaml(Constructor(List::class.java, LoaderOptions()))
                .load<List<Principle>>(File(PRINCIPLES_FILE).inputStream())

        fun readOrNull(name: String?): Principle? = principles.find {
            it.principle.name == name
        }

        fun readOrThrow(name: String): Principle =
            readOrNull(name) ?: throw PrincipleNotFoundException(name)
    }
}
