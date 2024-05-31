export enum TableCorporaType {
    Dataset = 'benchmark',
    Public = 'benchmark',
    User = 'user'
}

export enum TableDocumentsType {
    Dataset = 'dataset',
    Public = 'public',
    User = 'user'
}

/* sortOn defines what to sort field values on */
export type Field = {
    key: string
    label?: string
    sortOn?: (value: any) => any
    textAlign?: string
    isPrimaryField?: boolean
    hidden?: boolean
}

export type TableData<T> = {
    field: Field
    item: T
    value: any
}