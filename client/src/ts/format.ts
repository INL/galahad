import type { CorpusMetadata } from "@/types/corpora"
import type { Progress } from "@/types/jobs"
import type { LayerMetadata } from "@/types/layers"
import type { Period } from "@/types/taggers"

// https://stackoverflow.com/a/18650828
export function formatBytes(bytes: number, decimals = 0) {
    if (!+bytes || bytes < 1023) return "> 1 kB"
    const dm = 0 > decimals ? 0 : decimals
    const d = Math.floor(Math.log(bytes) / Math.log(1024))
    const sizes = ["B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"]
    return `${Number.parseFloat((bytes / 1024 ** d).toFixed(dm))} ${sizes[d]}`
}

export function formatDate(unixtime: number) {
    if (unixtime <= 0) {
        return "Never"
    }
    const d = new Date(unixtime)
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, "0")
    const day = String(d.getDate()).padStart(2, "0")
    const hours = String(d.getHours()).padStart(2, "0")
    const minutes = String(d.getMinutes()).padStart(2, "0")
    return `${year}-${month}-${day} ${hours}:${minutes}`
}

export function formatPeriod(period: Period): string | undefined {
    if (period) {
        const from = period.from ?? 0
        const to = period.to ?? 0
        return `${from} – ${to}`
    } else {
        return undefined
    }
}

export function formatShared(c: CorpusMetadata): string {
    if (c.dataset) return "Dataset"
    const numPeople = (c.collaborators?.length ?? 0) + (c.viewers?.length ?? 0)
    if (numPeople === 0) return "No one"
    return numPeople === 1 ? `${numPeople} person` : `${numPeople} people`
}

export function formatProgress(progress: Progress): string {
    // Format progress with Math.floor, because e.g. toFixed(0) rounds up 99.9% to 100%, which is confusing.
    return `${Math.floor((100 * progress.finished) / progress.total)}%`
}

/** Format as displayed in the <select> */
export function formatLayer(l: LayerMetadata): string {
    return `${l.tagger.name} (${l.tagger.description}) [${l.documents} documents]`
}

// 1 -> 1.000
// 0 -> 0.000
// 0.1 -> 0.100
// 0.999999 -> 0.999
export function formatDecimal(number: number) {
    return Number.isInteger(number) ? number : number.toPrecision(4).slice(0, 5)
}

export function formatClassification(classification: string) {
    return classification
        .replace(/([a-z0-9])([A-Z])/g, "$1 $2")
        .toLowerCase()
        .replace(/^./, (c) => c.toUpperCase())
}
