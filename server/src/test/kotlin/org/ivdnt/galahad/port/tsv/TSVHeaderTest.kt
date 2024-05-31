package org.ivdnt.galahad.port.tsv

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

internal class TSVHeaderTest {
    @Test
    fun `Parse literal names in header`() {
        // This folder contains TSV files with various names for the literal in the headers.
        for (file in File("src/test/resources/tsv/literal-name-headers").listFiles()!!) {
            val tsvFile = TSVFile(file)
            tsvFile.parse()
            // The column positions are fixed
            assertEquals(tsvFile.literalIndex, 0)
            assertEquals(tsvFile.lemmaIndex, 1)
            assertEquals(tsvFile.posIndex, 2)
            // Check entries
            assertEquals(tsvFile.entries.size, 1)
            tsvFile.entries.forEach {
                assertEquals("scholen", it.literal)
                assertEquals("school", it.lemma)
                assertEquals("NOU", it.pos)
            }
        }
    }

    @Test
    fun `Parse header column orders`() {
        // This folder contains TSV files with various column orders in the headers.
        for (file in File("src/test/resources/tsv/header-order").listFiles()!!) {
            val tsvFile = TSVFile(file)
            tsvFile.parse()
            // The column positions change, so no checks here.
            // Instead, check entries.
            assertEquals(1, tsvFile.entries.size)
            tsvFile.entries.forEach {
                assertEquals("scholen", it.literal)
                assertEquals("school", it.lemma)
                assertEquals("NOU", it.pos)
            }
        }
    }

    @Test
    fun `Incorrect headers`() {
        // This folder contains TSV files with incorrect headers.
        for (file in File("src/test/resources/tsv/incorrect-headers").listFiles()!!) {
            val tsvFile = TSVFile(file)
            try {
                tsvFile.parse() // should throw
                fail<String>("Should have thrown")
            } catch (e: Exception) {
                val type = file.nameWithoutExtension.split("-")[1]
                assertTrue(e.message!!.contains(type))
            }
        }
    }
}