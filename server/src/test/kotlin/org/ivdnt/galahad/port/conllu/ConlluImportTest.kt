package org.ivdnt.galahad.port.conllu

import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.port.Resource
import org.ivdnt.galahad.port.assertPlaintextAndSourcelayer
import org.ivdnt.galahad.port.tsv.TSVEntry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class ConlluImportTest {

    // Conllu makes use of the TSV implementation. TSVs have headers, so the first line of the file is parsed differently.
    // In particular, we need to test whether a Conllu file that already has body values on the first line is parsed correctly.
    @Test
    fun `Parse basic Conllu with no leading newlines`() {
        val file = ConlluFile(Resource.get("conllu/basic/input.conllu"))
        assertPlaintextAndSourcelayer("conllu/basic", file)
    }

    @Test
    fun `Parse Conllu with comments`() {
        val file = ConlluFile(Resource.get("conllu/comments/input.conllu"))
        assertPlaintextAndSourcelayer("conllu/comments", file)
    }
}