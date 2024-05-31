// Libraries & stores
import { ref } from "vue"
import { defineStore } from "pinia"
import yaml from 'js-yaml'
import { AxiosError } from "axios"
// Types & API
import * as API from "@/api/application"

// Custom types
type ErrorMessage = {
    statusCode: string
    message: string
}

/**
 * Mostly for global error handling.
 */
const app = defineStore('app', () => {
    // Fields
    const benchmarks = ref("" as string)
    const errors = ref([] as string[])

    // Methods
    function addError(message: string) {
        errors.value.push(message)
    }

    function resetErrors() {
        errors.value = []
    }

    /**
     * Display error in modal.
     * @param intent Human readable explanation.
     * @param error Axios error.
     */
    function handleServerError(intent: string, error: AxiosError<ErrorMessage>) {
        if (error.response) {
            // The request was made and the server responded with a status code
            // that falls out of the range of 2xx
            addError("Server error: Failed to " + intent + " with the following error:\n" + error?.response?.data?.message)
        } else if (error.request) {
            // The request was made but no response was received
            // `error.request` is an instance of XMLHttpRequest in the browser and an instance of
            // http.ClientRequest in node.js
            // this is a disconnect, it is handled by the user store
        } else {
            // Something happened in setting up the request that triggered an Error
            addError("Request error: Failed to " + intent + " because something went wrong setting up the request:\n" + error.message)
        }
    }

    function fetchBenchmarks() {
        API.getBenchmarks()
            .then(response => {
                benchmarks.value = yaml.load(response.data) as string
            })
            .catch(error => handleServerError("fetch benchmarks", error))
    }

    // Exports
    return { benchmarks, errors, resetErrors, fetchBenchmarks, handleServerError }
})

export default app
