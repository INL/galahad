package org.ivdnt.galahad.port.tei

import org.ivdnt.galahad.port.Resource
import org.ivdnt.galahad.port.assertPlainText
import org.ivdnt.galahad.port.assertPlaintextAndSourcelayer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TEIImportTest {
    @Nested
    inner class TEIP5Test {
        @Test
        fun `Multiple text elements`() {
            val teiFile = TEIFile(Resource.get("tei/dummies/multipletextelements.xml"))
            assertEquals("text1\ntext2 text3", teiFile.plainTextReader().readText().trim())
        }

        @Test
        fun `Simple word tags in one paragraph`() {
            val teiFile = TEIFile(Resource.get("tei/dummies/withwtags.xml"))
            assertEquals("word1 word2", teiFile.plainTextReader().readText().trim())
        }

        @Test
        fun `Simple word tags in two paragraph`() {
            val teiFile = TEIFile(Resource.get("tei/dummies/wandp.xml"))
            assertEquals("word1 word2\n\nword3 word4", teiFile.plainTextReader().readText().trim())
        }

        @Test
        fun `Import highly intertwined tags`() {
            val file = TEIFile(Resource.get("tei/twine/twine.input.xml"))
            assertPlaintextAndSourcelayer("tei/twine", file)
        }

        @Test
        fun `Import huygens brieven TEI`() {
            val file = TEIFile(Resource.get("tei/brieven/input.tei.xml"))
            // Has no source layer
            assertPlainText("tei/brieven", file)
        }

        @Test
        fun `Import TEI with w-tags without spaces in between`() {
            val file = TEIFile(Resource.get("tei/nospaces/input.tei.xml"))
            assertEquals("a a a", file.plainTextReader().readText().trim())

        }
    }

    @Nested
    inner class TEIP4Test {
        @Test
        fun `Import TEI P4`() {
            val file = TEIFile(Resource.get("tei/legacy/teip4/input.tei.xml"))
            assertPlaintextAndSourcelayer("tei/legacy/teip4", file)
        }
    }

    @Nested
    inner class TEIP5LegacyTest {
        @Test
        fun `Import TEI P5`() {
            val file = TEIFile(Resource.get("tei/legacy/teip5/input.tei.xml"))
            assertPlaintextAndSourcelayer("tei/legacy/teip5", file)
        }
    }
}