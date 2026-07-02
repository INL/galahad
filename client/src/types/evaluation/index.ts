import type { Term } from "@/types/annotation"

export type TermComparison = { hyp: Term; ref: Term }
export type EvaluationEntry = { count: number; samples: TermComparison[] }
