package org.ivdnt.galahad.util

import java.util.*

fun Float.toFixed(n: Number=3): String = String.format(Locale.ENGLISH, "%.${n}f", this)
