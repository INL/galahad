/** API for tagger health & queue. */

import axios, { type AxiosResponse } from "axios"
import { endpoints } from "@/api"

/** Whether the tagger is healthy. */
export function getTaggerHealth(tagger: string): Promise<AxiosResponse<boolean>> {
    return axios.get(endpoints.taggers.health({ tagger }))
}

/** Number of jobs in queue.*/
export function getQueue(): Promise<AxiosResponse<number>> {
    return axios.get(endpoints.taggers.queue())
}
