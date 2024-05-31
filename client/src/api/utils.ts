/**
 * Utils for handling the blobs from some API responses.
 */

// Libraries & stores
import { AppStore } from "@/stores"
import axios, { AxiosResponse, AxiosError } from "axios"
import { parse } from 'content-disposition'

// Custom types
export type BlobResponse = AxiosResponse<Blob>

/**
 * Fetch a blob from a path.
 * @param path Request path.
 */
export function getBlob(path: string): Promise<BlobResponse> {
    return axios.get(path, { responseType: 'blob' })
}

/**
 * Downloads a file from a response object.
 * @param response Response with blob data.
 */
export function browserDownloadResponseFile(response: BlobResponse) {
    // Parse potential UTF8 filename.
    const filename = parse(response.headers['content-disposition']).parameters.filename
    // DOM link.
    const linkEl = document.createElement('a')
    linkEl.href = window.URL.createObjectURL(new Blob([response.data]))
    linkEl.setAttribute('download', filename)
    document.body.appendChild(linkEl)
    linkEl.click()
}

/**
 * Axios does not support multiple responseTypes. When trying to download a blob, 
 * we also receive a blob on error instead of json. So first parse to json.
 * https://medium.com/@fakiolinho/handle-blobs-requests-with-axios-the-right-way-bb905bdb1c04
 * @param error Axios error.
 * @param intent Human readable explanation.
 * @param app appStore.
 */
export function handleBlobError(error: AxiosError<Blob>, intent: string, app: AppStore) {
    const reader = new FileReader()
    // Setup the onload that fires after reading.
    reader.onload = () => {
        const json = JSON.parse(reader.result as string)
        const errObj = {
            response: {
                data: json
            }
        } as AxiosError
        app.handleServerError(intent, errObj)
    }
    // Now, read.
    reader.readAsText(error.response?.data as Blob)
}

// https://stackoverflow.com/a/18650828
export function formatBytes(bytes: number, decimals = 2) {
    if (!+bytes) return "0 Bytes"
    const dm = 0 > decimals ? 0 : decimals
    const d = Math.floor(Math.log(bytes) / Math.log(1024))
    const sizes = ["Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"]
    return `${parseFloat((bytes / Math.pow(1024, d)).toFixed(dm))} ${sizes[d]}`
}
