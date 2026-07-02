package org.ivdnt.galahad.web.controller

object Endpoints {
    const val BASE: String = "/"
    const val VERSION: String = "/version"
    const val USER: String = "/user"
    const val SWAGGER: String = "/swagger-ui/index.html"

    object Corpora {
        const val BASE: String = "/corpora"
        const val CORPUS: String = "$BASE/{corpus}"
    }

    object Layers {
        const val BASE: String = "${Corpora.CORPUS}/layers"
        const val LAYER: String = "$BASE/{layer}"

        object Documents {
            const val BASE: String = "$LAYER/documents"
            const val DOCUMENT: String = "$BASE/{document}"
            const val DOWNLOAD: String = "${DOCUMENT}/download"
        }
    }

    object Jobs {
        const val BASE: String = "${Corpora.CORPUS}/jobs"
        const val JOB: String = "$BASE/{job}"
        const val PROGRESS: String = "$JOB/progress"

        // TODO retrieve job status (Jobmetadata) of individual documents?
        //        object Documents {
        //            const val BASE: String = "$JOB/documents"
        //            const val DOCUMENT: String = "$BASE/{document}"
        //        }
    }

    object Internal {
        const val BASE: String = "/internal"
        const val JOBS: String = "$BASE/jobs"
        const val RESULT: String = "$JOBS/result"
        const val ERROR: String = "$JOBS/error"
    }

    object Evaluation {

        object Corpus {
            const val BASE: String = "${Corpora.CORPUS}/evaluation"

            object Metrics {
                const val BASE: String = "${Corpus.BASE}/metrics"
            }
        }

        object Layer {
            const val BASE: String = "${Layers.LAYER}/evaluation"
            const val DOWNLOAD: String = "${BASE}/download"

            object Metrics {
                const val BASE: String = "${Layer.BASE}/metrics"
                const val DOWNLOAD: String = "${BASE}/download"
            }

            object Confusion {
                const val BASE: String = "${Layer.BASE}/confusion"
                const val DOWNLOAD: String = "${BASE}/download"
            }

            object Distribution {
                const val BASE: String = "${Layer.BASE}/distribution"
                const val DOWNLOAD: String = "${BASE}/download"
            }
        }

        object Document {
            const val BASE: String = "${Layers.Documents.DOCUMENT}/evaluation"

            object Metrics {
                const val BASE: String = "${Document.BASE}/metrics"
                const val DOWNLOAD: String = "${BASE}/download"
            }

            object Confusion {
                const val BASE: String = "${Document.BASE}/confusion"
                const val DOWNLOAD: String = "${BASE}/download"
            }

            object Distribution {
                const val BASE: String = "${Document.BASE}/distribution"
                const val DOWNLOAD: String = "${BASE}/download"
            }
        }
    }

    object Benchmarks {
        const val BASE: String = "${Corpora.CORPUS}/benchmarks"
    }

    object Export {
        const val BASE: String = "${Layers.LAYER}/export"
        const val CONVERT: String = "$BASE/convert"
        const val MERGE: String = "$BASE/merge"

        object Documents {
            const val BASE: String = "${Layers.Documents.DOCUMENT}/export"
            const val CONVERT: String = "$BASE/convert"
            const val MERGE: String = "$BASE/merge"
        }
    }

    object Taggers {
        const val BASE: String = "/taggers"
        const val TAGGER: String = "$BASE/{tagger}"
        const val QUEUE: String = "$BASE/queue"
        const val HEALTH: String = "$TAGGER/health"
    }

    object Tagsets {
        const val BASE: String = "/tagsets"
        const val TAGSET: String = "$BASE/{tagset}"
    }
}
