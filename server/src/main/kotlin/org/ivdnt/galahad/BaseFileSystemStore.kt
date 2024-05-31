package org.ivdnt.galahad

import java.io.File


abstract class BaseFileSystemStore (
        protected val workDirectory: File,
        fileMode: Boolean = false
) {

    init {
        if( fileMode ) { workDirectory.parentFile.mkdirs(); /*workDirectory.createNewFile()*/ } else workDirectory.mkdirs()
    }

    val lastModified: Long
        get() = workDirectory.walkTopDown().map { it.lastModified() }.reduceOrNull { f,g -> f.coerceAtLeast(g) } ?: -1
}