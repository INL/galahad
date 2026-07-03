/** Utils for handling blobs from some API responses. */

import axios from "axios"
import type { AxiosRequestConfig, AxiosResponse } from "axios"
import { parse } from "content-disposition"

export type BlobResponse = AxiosResponse<Blob>

/**
 * Fetch a blob from a path.
 * @param path Request path.
 */
export function getBlob(path: string, config?: AxiosRequestConfig): Promise<BlobResponse> {
    return axios.get(path, { responseType: "blob", ...config })
}

/**
 * Downloads a file from a response object.
 * @param response Response with blob data.
 */
export function browserDownloadResponseFile(response: BlobResponse): void {
    // Parse potential UTF8 filename.
    const filename = parse(response.headers["content-disposition"]).parameters.filename
    // DOM link.
    const linkEl = document.createElement("a")
    linkEl.href = window.URL.createObjectURL(new Blob([response.data]))
    linkEl.setAttribute("download", filename)
    document.body.appendChild(linkEl)
    linkEl.click()
}
