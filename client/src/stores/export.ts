import * as API from "@/api/export"
import * as Utils from "@/api/utils"
import { plausible } from "@/ts/plausible"
import { Format } from "@/types/documents"
import type { SelectOption } from "@/types/ui/select"
import useCorpora from "@/stores/corpora"
import useLayers from "@/stores/layers"

/** Used to download exported corpora. */
const useExport = defineStore("export", () => {
    // Stores
    const { corpus, corpusId } = storeToRefs(useCorpora())
    const { hypothesisId, hypothesisLayer } = storeToRefs(useLayers())

    // Fields
    const loading = ref<boolean>()
    const format = ref<Format>()
    const options: SelectOption[] = [
        { value: Format.CONLLU, text: "CoNLL-U (Universal Dependencies)" },
        { value: Format.FOLIA, text: "FoLiA (Format for Linguistic Annotation)" },
        { value: Format.NAF, text: "NAF (NLP Annotation Format) " },
        { value: Format.TEI_P5, text: "TEI P5 (Text Encoding Initiative)" },
        { value: Format.TSV, text: "TSV (Tab-separated values)" },
        { value: Format.TXT, text: "TXT (Plain text, tokens only)" },
        { value: Format.JSON, text: "JSON (GaLAHaD internal format)" },
    ]

    // Methods
    function convert(shouldMerge: boolean, posHeadOnly: boolean): void {
        if (shouldMerge) {
            merge(posHeadOnly)
            return
        }
        loading.value = true
        API.convertCorpus(corpusId.value, hypothesisId.value, format.value, posHeadOnly)
            .then(Utils.browserDownloadResponseFile)
            .then(() => {
                plausible.export.exported(corpus.value, hypothesisLayer.value, format.value, false, posHeadOnly)
            })
            .finally(() => (loading.value = false))
    }

    function merge(posHeadOnly: boolean): void {
        loading.value = true
        API.mergeCorpus(corpusId.value, hypothesisId.value, format.value, posHeadOnly)
            .then(Utils.browserDownloadResponseFile)
            .then(() => {
                plausible.export.exported(corpus.value, hypothesisLayer.value, format.value, true, posHeadOnly)
            })
            .finally(() => (loading.value = false))
    }

    return { format, options, loading, convert }
})

export default useExport
