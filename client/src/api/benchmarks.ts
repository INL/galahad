import { endpoints } from "@/api"
import type { UUID } from "@/types/corpora"
import type { GlobalMetrics } from "@/types/evaluation/metrics"
import axios, { type AxiosResponse } from "axios"

export function getBenchmarks(
    corpus: UUID,
    annotations: string[],
    group: string,
): Promise<AxiosResponse<GlobalMetrics[]>> {
    return axios.get(endpoints.benchmarks({ corpus }, { annotations, group }))
}
