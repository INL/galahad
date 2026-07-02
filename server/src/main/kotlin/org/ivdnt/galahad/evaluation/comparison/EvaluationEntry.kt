package org.ivdnt.galahad.evaluation.comparison

import kotlin.random.Random

data class EvaluationEntry(
    val count: Int = 0,
    val samples: MutableList<TermComparison> = mutableListOf(),
) {
    //    @get:JsonProperty("samples")
    //    val jsonSamples: List<TermComparison> get() =
    // samples.asSequence().shuffled().take(MAX_SAMPLE_LENGTH).toList()

    fun truncate() {
        val truncated = samples.asSequence().shuffled().take(MAX_SAMPLE_LENGTH).toMutableList()
        samples.clear()
        samples.addAll(truncated)
    }

    companion object {
        private const val MAX_SAMPLE_LENGTH: Int = 10

        /** Combine EvaluatationEntries, optionally truncating the number of samples. */
        fun add(a: EvaluationEntry, b: EvaluationEntry, truncate: Boolean = true): EvaluationEntry {
            if (a.samples.size >= MAX_SAMPLE_LENGTH && truncate) {
                // Samples.size stays the same, but we randomly replace samples
                if (b.samples.isNotEmpty() && Random.nextFloat() < 0.01f) {
                    val randomI = Random.nextInt(a.samples.size)
                    a.samples[randomI] = b.samples.first()
                }
            } else {
                // Add more samples
                a.samples.addAll(b.samples)
            }
            return EvaluationEntry(a.count + b.count, a.samples)
        }

        fun from(a: EvaluationEntry, b: EvaluationEntry): EvaluationEntry {
            return EvaluationEntry(
                a.count + b.count,
                (a.samples + b.samples).shuffled().take(MAX_SAMPLE_LENGTH).toMutableList(),
            )
        }
    }
}
