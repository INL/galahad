import type { EvaluationEntry } from "@/types/evaluation"

export type Confusion = Record<string, Record<string, EvaluationEntry>>
