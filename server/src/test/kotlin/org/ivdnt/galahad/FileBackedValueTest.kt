package org.ivdnt.galahad

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.io.File

internal class FileBackedValueTest {

    @Test
    fun getFile() {
        val file = File.createTempFile("temp", null)
        val fbv = FileBackedValue<String>(file, "a string")
        assertEquals("a string", fbv.read<String>())
        fbv.modify<String> { "some characters" }
        assertEquals("some characters", fbv.read<String>())

        // Try to force some concurrent modifications
        GlobalScope.launch { for (i in 0..100) fbv.modify<String> { "some characters" } }
        for (i in 0..10) {
            fbv.modify<String> { "some characters" }
            fbv.read<String>()
        }
        assertEquals("some characters", fbv.read<String>())
    }
}