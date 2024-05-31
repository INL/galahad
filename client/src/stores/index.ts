import useApp from './app'
import useUser from './user'

import useCorpora from './corpora'

import useDocuments from './documents'

import useJobs from './jobs'
import useJobSelection from './jobselection'

import useTaggers from './taggers'
import useTagsets from './tagsets'
import useAssays from './assays'

import useEvaluation from './evaluation'
import useDistribution from './evaluation/distribution'
import useMetrics from './evaluation/metrics'
import useConfusion from './evaluation/confusion'

import useExportStore from './exportstore'

// Custom types
export type AppStore = ReturnType<typeof useApp>
export type UserStore = ReturnType<typeof useUser>
export type CorporaStore = ReturnType<typeof useCorpora>
export type DocumentsStore = ReturnType<typeof useDocuments>
export type JobsStore = ReturnType<typeof useJobs>
export type JobSelectionStore = ReturnType<typeof useJobSelection>
export type TaggersStore = ReturnType<typeof useTaggers>
export type TagsetsStore = ReturnType<typeof useTagsets>
export type AssaysStore = ReturnType<typeof useAssays>
export type EvaluationStore = ReturnType<typeof useEvaluation>
export type DistributionStore = ReturnType<typeof useDistribution>
export type MetricsStore = ReturnType<typeof useMetrics>
export type ConfusionStore = ReturnType<typeof useConfusion>
export type ExportStore = ReturnType<typeof useExportStore>

export default {
    useApp,
    useAssays,
    useConfusion,
    useCorpora,
    useDistribution,
    useDocuments,
    useExportStore,
    useMetrics,
    useJobs,
    useJobSelection,
    useUser,
    useEvaluation,
    useTaggers,
    useTagsets,
}