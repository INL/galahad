package org.ivdnt.galahad

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.logging.log4j.kotlin.Logging
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException
import java.nio.file.StandardOpenOption


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

        val channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)
        channel.use {
            val lock = lockFile( channel, false )
            lock.use {
                val fileSize = channel.size().toInt()
                val byteBuffer: ByteBuffer = ByteBuffer.allocate(fileSize)
                channel.read(byteBuffer)
                byteBuffer.flip()
                val bytes: ByteArray = byteBuffer.array()
                byteBuffer.clear()
                return mapper.readValue( bytes, object : TypeReference<S>() {})
            }
        }

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

        val channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING )
        channel.use {
            val lock = lockFile( channel, true )
            lock.use {
                val newValue = modification( oldValue )
                val newValBytes = mapper.writeValueAsBytes( newValue )
                logger.trace("will write $newValue")
                val byteBuffer = ByteBuffer.wrap( newValBytes )
                channel.write( byteBuffer )
            }
        }
    }

    fun lockFile( channel: FileChannel, exclusive: Boolean ): FileLock? {
        while (true) {
            try {
                return if( exclusive ) {
                    channel.lock()
                } else {
                    channel.lock(0L, Long.MAX_VALUE, true)
                }
            } catch (ignored: OverlappingFileLockException) {
            } catch (ignored: IOException) {
            }
            try {
                logger.info("Tried to read access $file but it was locked. Will sleep for ${LOCK_SLEEP_TIME}ms now then retry.")
                Thread.sleep(LOCK_SLEEP_TIME)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

}