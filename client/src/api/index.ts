/** Axios config & endpoints. */

import axios from "axios"

export type ErrorMessage = { statusCode: string; message: string }

type PathResolver<T extends string> = T extends `${infer _Start}{${infer Param}}${infer Rest}`
    ? (params: Record<Param | keyof PathParams<Rest>, string>, query?: Record<string, string>) => string
    : (query?: Record<string, string>) => string

type PathParams<T extends string> = T extends `${infer _Start}{${infer Param}}${infer Rest}`
    ? Record<Param, string> & PathParams<Rest>
    : Record<never, string>

function resolvePath(path: string, params?: Record<string, string>, query?: Record<string, string>): string {
    const resolvedPath = params
        ? path.replace(/\{(\w+)\}/g, (_, key) => {
              if (!(key in params)) throw new Error(`Missing path parameter: ${key}`)
              return params[key]
          })
        : path

    if (!query || Object.keys(query).length === 0) return resolvedPath
    console.log("query", query)
    // clear any undefineds from query
    Object.keys(query).forEach((key) => {
        if (query[key] === undefined) {
            delete query[key]
        }
    })
    const searchParams = new URLSearchParams(query)
    console.log("searchParams", searchParams)
    const queryString = searchParams.toString()

    return queryString ? `${resolvedPath}?${queryString}` : resolvedPath
}

function endpoint<T extends string>(path: T): PathResolver<T> {
    return ((paramsOrQuery?: Record<string, string>, query?: Record<string, string>) => {
        const hasPathParams = /\{\w+\}/.test(path)
        return hasPathParams ? resolvePath(path, paramsOrQuery, query) : resolvePath(path, undefined, paramsOrQuery)
    }) as PathResolver<T>
}

export const endpoints = {
    user: endpoint("/user"),
    corpora: { base: endpoint("/corpora"), corpus: endpoint("/corpora/{corpus}") },
    layers: { base: endpoint("/corpora/{corpus}/layers"), layer: endpoint("/corpora/{corpus}/layers/{layer}") },
    documents: {
        base: endpoint("/corpora/{corpus}/layers/{layer}/documents"),
        document: endpoint("/corpora/{corpus}/layers/{layer}/documents/{document}"),
        download: endpoint("/corpora/{corpus}/layers/{layer}/documents/{document}/download"),
    },
    jobs: {
        base: endpoint("/corpora/{corpus}/jobs"),
        job: endpoint("/corpora/{corpus}/jobs/{job}"),
        progress: endpoint("/corpora/{corpus}/jobs/{job}/progress"),
    },
    evaluation: {
        download: endpoint("/corpora/{corpus}/layers/{layer}/evaluation/download"),
        distribution: endpoint("/corpora/{corpus}/layers/{layer}/evaluation/distribution"),
        confusion: {
            base: endpoint("/corpora/{corpus}/layers/{layer}/evaluation/confusion"),
            samples: endpoint("/corpora/{corpus}/layers/{layer}/evaluation/confusion/download"),
        },
        metrics: {
            base: endpoint("/corpora/{corpus}/layers/{layer}/evaluation/metrics"),
            download: endpoint("/corpora/{corpus}/layers/{layer}/evaluation/metrics/download"),
        },
    },
    benchmarks: endpoint("/corpora/{corpus}/evaluation/metrics"),
    export: {
        convert: endpoint("/corpora/{corpus}/layers/{layer}/export/convert"),
        merge: endpoint("/corpora/{corpus}/layers/{layer}/export/merge"),
    },
    taggers: {
        base: endpoint("/taggers"),
        queue: endpoint("/taggers/queue"),
        health: endpoint("/taggers/{tagger}/health"),
    },
    principles: endpoint("/principles"),
}

/**
 * Set the axios request base to localhost:8010 if running locally,
 * or https://<hostname>/galahad/api/ if running in production.
 */
export function setAxiosBaseUrl(): void {
    axios.defaults.baseURL =
        location.hostname === "localhost"
            ? `${location.protocol}//localhost:8010`
            : `${location.protocol}//${location.hostname}/galahad/api/`
}
