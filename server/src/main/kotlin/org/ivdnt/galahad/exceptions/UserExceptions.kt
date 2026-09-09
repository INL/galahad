package org.ivdnt.galahad.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

// HttpStatus UNAUTHORIZED is for login, FORBIDDEN is for access rights after login
/** Thrown when a user cannot read or write a corpus. */
@ResponseStatus(value = HttpStatus.FORBIDDEN)
class UserUnauthorizedException(action: String) : Exception("Unauthorized. $action")
