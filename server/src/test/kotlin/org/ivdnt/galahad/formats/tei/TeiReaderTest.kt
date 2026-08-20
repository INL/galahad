package org.ivdnt.galahad.formats.tei

import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.formats.ReaderTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TeiReaderTest : ReaderTest() {
    override val format: DocumentFormat = DocumentFormat.TeiP5

    @Nested
    inner class TEIP5Test {

        @Test
        fun `Import huygens brieven TEI`() {
            assertLayerAndText("formats/tei/reader/brieven")
        }

        @Test
        fun `Import missiven TEI`() {
            assertLayerAndText("formats/tei/reader/missiven")
        }

        @Test
        fun `Sentence with mixed tag and tagless text`() {
            assertLayerAndText("formats/tei/reader/mixed-tags/")
        }

        @Test
        fun `Import TEI with whitespace in w tags`() {
            assertLayerAndText("formats/tei/reader/space-in-tag/")
        }

        @Test
        fun `Import TEI with w-tags without spaces in between`() {
            assertLayerAndText("formats/tei/reader/nospaces")
        }

        @Test
        fun `Note tag should be ignored`() {
            assertLayerAndText("formats/tei/reader/note/")
        }

        @Test
        fun `Import peerle TEI`() {
            assertLayerAndText("formats/tei/reader/peerle")
        }

        @Test
        fun `Import teicorpus`() {
            assertLayerAndText("formats/tei/reader/teicorpus")
        }

        @Test
        fun `Import seg tags`() {
            assertLayerAndText("formats/tei/reader/seg")
        }

        @Test
        fun `Import seg tags in w tags`() {
            assertLayerAndText("formats/tei/reader/seg-in-w")
        }

        @Test
        fun `Import highly intertwined tags`() {
            assertLayerAndText("formats/tei/reader/twine")
        }
    }

    @Nested
    inner class TEIP4Test {
        @Test
        fun `Import TEI P4`() {
            assertLayerAndText("formats/tei/reader/teip4")
        }
    }

    @Nested
    inner class TEIP5LegacyTest {
        @Test
        fun `Import TEI P5`() {
            assertLayerAndText("formats/tei/reader/teip5")
        }
    }
}
