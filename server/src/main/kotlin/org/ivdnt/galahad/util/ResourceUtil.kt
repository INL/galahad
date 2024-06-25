package org.ivdnt.galahad.util

import java.io.InputStream

/** Get a resource from src/main/resources. */
fun getResourceStream(path: String): InputStream? =
    object {}.javaClass.classLoader.getResourceAsStream(path)
