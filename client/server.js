const express = require('express')
const history = require('connect-history-api-fallback')

const app = express()
const port = 8080

// Due to production settings at INT, the Vue app has to be run at the /galahad path
// This path is set at build time and therefore can not be configured on the docker container itself
// We use this standard redirect so that the app also works in other contexts
// e.g. dev server

// See https://stackoverflow.com/questions/44226622/vue-router-and-express for router funkiness

const staticFileMiddleware = express.static('dist')

// app.get(/^(?!\/galahad)[\s\S]*/, (req, res) => {
//   // If path does not start with '/galahad'
//   res.redirect('/galahad')
// })


// This is a bit ugly, but it works for now
// Need to look at it again later
// The staticfiles at root are somehow used if the client is behind the portal
// but '/galahad' is the public path
app.use( staticFileMiddleware )
app.use('/galahad', staticFileMiddleware )
app.use(history({
  disableDotRule: true,
  index: '/galahad/index.html',
  verbose: true
}))
app.use('/galahad', staticFileMiddleware )
app.use( staticFileMiddleware )

app.listen(port, () => {
  console.log(`App listening at http://localhost:${port}`)
})

