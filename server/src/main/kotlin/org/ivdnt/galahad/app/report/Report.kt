package org.ivdnt.galahad.app.report

import org.apache.logging.log4j.kotlin.Logging
import org.apache.logging.log4j.kotlin.logger
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.data.layer.WordForm

class Report : Logging {

    companion object {
        // For now we are just collecting reports here
        // In the future we would like to export them for the user

        fun spottedIncompatibleTokenization(wf1: WordForm, wf2: WordForm) {
            // Note that this is not necessarily bad. For example if the wordform is 'Bantam;'
            // And the original tokenization is <w>Bantam<</w><pc>;</pc>
            // Then based on the (begin)offset we can still map Bantam to Bantam.
            // println("Spotted incompatible tokenization for \"${wf.literal}\" at offset ${wf.offset}")
            // Now we do nothing, but it is good to centrally register this
            logger().warn( "REPORT: Spotted incompatible tokenization for wordforms \n" +
                    "    - ${wf1.literal} \n" +
                    "    - ${wf2.literal}"
            )
        }

        fun wordformMismatchForTerm(wf: WordForm, term: Term) {
            logger().warn( "REPORT: Wordform $wf has a mismatch with $term" )
        }

        fun tokenMissingAnnotation( literal: String, offset: Int ) {
            logger().warn( "REPORT: Literal $literal at offset $offset does not have an annotation" )
        }

        fun annotationAfterPlaintext( literal: String, offset: Int ) {
            logger().warn( "REPORT: Literal $literal, and possibly more, at offset $offset is after the end of the plaintext" )
        }
    }
}