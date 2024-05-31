// Libraries & stores
import axios, { AxiosResponse } from "axios"
// API & types
import { User } from '@/types/user'

// Paths
const benchmarksPath = `/benchmarks`
const userPath = `/user`
const versionPath = `/version`

// Custom types
type BenchmarksResponse = AxiosResponse<string>
type UserResponse = AxiosResponse<User>
type VersionResponse = AxiosResponse<string>

// Public methods
/**
 * Poll user account.
 */
export function getUser(): Promise<UserResponse> {
    return axios.get(userPath)
}

export function getBenchmarks(): Promise<BenchmarksResponse> {
    return axios.get(benchmarksPath)
}

export function getVersion(): Promise<VersionResponse> {
    return axios.get(versionPath)
}
