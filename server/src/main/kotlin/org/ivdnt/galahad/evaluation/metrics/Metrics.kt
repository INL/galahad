package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.ivdnt.galahad.port.csv.CSVFile
import org.ivdnt.galahad.port.csv.CSVHeader

const val TRUNCATE = 100
/**
 * Generic class for benchmark [Metric]s of a corpus or document.
 * The idea is to sum up the distribution as we go through the terms one by one using [add].
 */
open class Metrics(
    @JsonIgnore val settings: List<MetricsSettings>,
    @JsonIgnore val truncate: Boolean = true
) {
    @JsonProperty("metrics")
    val metricTypes: MutableMap<String, MetricsType> = HashMap()

    init {
        settings.forEach { metricTypes[it.id] = MetricsType(it).also { it.truncate = truncate } }
    }

    fun toGlobalCsv(): String {
        var csv: String = getCsvHeader()
        metricTypes.values.forEach { csv += it.toGlobalCsv() }
        return csv
    }

    protected fun add(other: Metrics) {
        other.metricTypes.values.toSet().forEach{ metricTypes[it.setting.id]?.add(it) }
    }

    fun add(comp: TermComparison) {
        settings.forEach { metricTypes[it.id]?.add(comp) }
    }

    companion object {
        fun getCsvHeader(): CSVHeader {
            return CSVFile.toCSVHeader(listOf(
                "annotation",
                "grouped by",
                "macro precision",
                "macro recall",
                "macro f1",
                "micro accuracy",
                "count",
                "true positive count",
                "false negative count",
                "no match count")
            )
        }
    }
}
