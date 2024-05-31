package org.ivdnt.galahad.evaluation

import org.ivdnt.galahad.data.layer.toNonEmptyPair
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.ivdnt.galahad.port.csv.CSVFile

interface CsvSampleExporter {
   fun samplesToCSV(): String
   fun samplesToCSV(comps: List<TermComparison>?): String {
      var csv = ""
      comps?.forEach { termComp ->
         var literal = termComp.hypoTerm.literals
         if (literal.isEmpty()) {
            literal = termComp.refTerm.literals
         }
         val (pos1, lemma1) = termComp.refTerm.toNonEmptyPair()
         val (pos2, lemma2) = termComp.hypoTerm.toNonEmptyPair()
         csv += CSVFile.toCSVRecord(listOf(literal, lemma1, pos1, lemma2, pos2))
      }
        return csv
   }
}