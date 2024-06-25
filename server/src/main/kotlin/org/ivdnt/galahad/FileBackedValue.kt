package org.ivdnt.galahad

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.kotlin.Logging
import java.io.File


val mapper: ObjectMapper by lazy { ObjectMapper() }
const val LOCK_SLEEP_TIME = 100L // ms to sleep before retrying to access locked file.

abstract class FileBackedCache<T>(
    file: File,
    initValue: T,
) : FileBackedValue<T>( file, initValue ) {

    abstract fun isValid( lastModified: Long ): Boolean // is cache valid?
    abstract fun set(): T

    inline fun <reified S : T>get(): T {
        return if( isValid( lastModified ) ) {
            read<S>()
        } else {
            // log
            println("Cache of type ${S::class.simpleName} is not valid. Will set new value.")
            // if not valid:
            val newValue = set()
            modify<S> { newValue }
            newValue
        }
    }

}

// the implementation of this class might be open for improvement,
// but it suffices for now.
// for examples see https://stackoverflow.com/questions/44589669/correctly-implementing-wait-and-notify-in-kotlin
open class FileBackedValue<T>(
    val file: File,
    val initValue: T, // required to avoid null
) : Logging {

    init {
        file.parentFile.mkdirs()
        //file.createNewFile()//
        // Would like to write/modify here to set the default value,
        // but we don't have access to the reified type here.
    }

    val lastModified: Long
        get() = file.lastModified()

    inline fun <reified S : T>read(): T {
        if( !file.exists() || file.length() == 0L ) {
            // It was not set yet
            return initValue
        }
        val bytes: ByteArray = runBlocking(Dispatchers.IO) { file.inputStream().use { it.readBytes() }}
        return mapper.readValue(bytes, object : TypeReference<S>() {})
    }

    /**
     * modify can be used to set a new value based on the current value
     * in particular you can use it as a 'write' like:
     * modify<MyType> { newValue }
     * An example of modification:
     * modify<Int> { oldValue++ }
     */
    inline fun <reified S : T>modify( modification: (T) -> T ) {
        if( !file.exists() ) {
            file.createNewFile()
        }
        // Would love to do this atomically, but for now we won't
        val oldValue = read<S>()
        val newValue = modification(oldValue)
        val newValBytes = mapper.writeValueAsBytes(newValue)
        runBlocking(Dispatchers.IO) { file.writeBytes(newValBytes)}
    }
}