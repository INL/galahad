/** Add content type header. */
export function addContentTypeHeader(fd: FormData): Record<string, string> | null {
    const exts_and_headers = { tsv: "text/tab-separated-values", conllu: "text/tab-separated-values", naf: "text/xml", tei: "text/xml", xml: "text/xml" }

    let file = fd.get("file") as File
    const extension = fileExtension(file)
    let header = null

    if (Object.keys(exts_and_headers).includes(extension)) {
        const contentType = exts_and_headers[extension]
        file = new File([file], file.name, { type: contentType })
        header = { "Content-Type": contentType }
        fd.set("file", file)
    }
    return header
}

export function fileExtension(file: File): string | undefined {
    return file.name.toLowerCase().split(".").at(-1)
}
