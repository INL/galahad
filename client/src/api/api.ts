import axios from 'axios'

export const setAxiosBaseUrl = () => {
    // This defines the server API url
    const apiURL = window.location.hostname === 'localhost'
        ? window.location.protocol + '//' + window.location.hostname + ':8010' // assume dev
        : window.location.protocol + '//' + window.location.hostname + '/galahad/api/' // assume prod
    axios.defaults.baseURL = apiURL
}
