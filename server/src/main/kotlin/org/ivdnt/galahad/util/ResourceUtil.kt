package org.ivdnt.galahad.util

import java.io.InputStream

fun getResourceStream(path: String): InputStream? =
    object {}.javaClass.classLoader.getResourceAsStream(path)
