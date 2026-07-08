import { endpoints } from "@/api"
import { useAxios } from "@/api/useAxios"
import type { Principle } from "@/types/principles"

/** Stores available principles. */
const usePrinciples = defineStore("principles", () => {
    const { data: principles, loading } = useAxios<Principle[]>(endpoints.principles(), [])
    return { loading, principles }
})

export default usePrinciples
