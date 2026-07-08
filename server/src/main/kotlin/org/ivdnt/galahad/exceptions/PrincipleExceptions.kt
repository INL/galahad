package org.ivdnt.galahad.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class PrincipleNotFoundException(principle: String) : Exception("Principle $principle not found.")
