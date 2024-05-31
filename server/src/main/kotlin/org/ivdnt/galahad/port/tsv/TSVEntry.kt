package org.ivdnt.galahad.port.tsv

/**
 * Tsv entry with nullable lemma and PoS. E.g. punctuation might not have a lemma.
 */
class TSVEntry(
    /** Never null. If the literal is empty, the entry should not be added. */
    val literal: String,
    val lemma: String?,
    /**
     * If it contains features, expected be in parentheses. E.g. NOU-C(num=sg). CoNLL-U needs to construct this.
     */
    val pos: String?,
)