import type { Term, TermComparison } from "@/types/evaluation"

// For some reason the terms are undefined sometimes
// We handle it here
export function literalsForTerm(term: Term): string {
    return term.annotations.token
}

export function literalsForTermComparison(termComparison: TermComparison): string {
    // the literals could be different for term1 and term2
    if (
        literalsForTerm(termComparison.hyp) === literalsForTerm(termComparison.ref) ||
        literalsForTerm(termComparison.ref) === ""
    ) {
        return literalsForTerm(termComparison.hyp)
    }
    if (literalsForTerm(termComparison.hyp) === "") {
        return literalsForTerm(termComparison.ref)
    }
    return `MISMATCH: [${literalsForTerm(termComparison.hyp)} — ${literalsForTerm(termComparison.ref)}]`
}
