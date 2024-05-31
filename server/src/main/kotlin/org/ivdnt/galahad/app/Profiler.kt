package org.ivdnt.galahad.app

@Deprecated("use spring functionality")
inline fun <R> executeAndLogTime( identifier: String, block: () -> R ): R {
    val start = System.currentTimeMillis()
    val result = block()
    println( "$identifier took ${System.currentTimeMillis() - start}ms" )
    return result
}