package org.ivdnt.galahad.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ThreadPoolUtil {
    // 100 seems high, but we have various IO bursts
    // like on upload or on evaluation in corpora with many documents.
    val pool: ExecutorService = Executors.newFixedThreadPool(100)
}

fun <A, B> Iterable<A>.parallelMap(f: (A) -> B): List<B> {
    val futures = map { item -> ThreadPoolUtil.pool.submit<B> { f(item) } }
    return futures.map { it.get() }
}

fun <A, B> Iterable<A>.parallelFlatMap(f: (A) -> Iterable<B>): List<B> {
    val futures = map { item -> ThreadPoolUtil.pool.submit<List<B>> { f(item).toList() } }
    return futures.flatMap { it.get() }
}
