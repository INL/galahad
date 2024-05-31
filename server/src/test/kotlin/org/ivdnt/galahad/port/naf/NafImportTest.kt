package org.ivdnt.galahad.port.naf

import org.ivdnt.galahad.port.Resource
import org.ivdnt.galahad.port.assertPlaintextAndSourcelayer
import org.junit.jupiter.api.Test

class NafImportTest {
    @Test
    fun `Import a NAF`() {
        assertPlaintextAndSourcelayer("naf/import", NAFFile(Resource.get("naf/import/input.naf.xml")))
    }
}