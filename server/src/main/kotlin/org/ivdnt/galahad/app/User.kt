package org.ivdnt.galahad.app

import jakarta.servlet.http.HttpServletRequest
import java.io.File

val adminFile = File("data/admins/admins.txt")
fun isAdmin(string: String): Boolean {
    if (!adminFile.exists()) return false // When no admins are set, no one is admin by default
    return adminFile.readLines().map { it.trim() }.contains(string) // Otherwise only declared admins are admins
}

class User(
    val id: String,
    val isAdmin: Boolean = false,
) {

    companion object {

        fun getUserFromRequestOrThrow(request: HttpServletRequest?): User {
            if (request == null) throw Exception("Request object is null")

            if (application_profile.contains("dev")) {
                // DEV: for sake of running unit tests in dev too, we still check the header
                return try {
                    val remoteUser = request.getHeader("remote_user")
                    User(id = remoteUser, isAdmin = isAdmin(remoteUser))
                } catch (e: Exception) {
                     User(id = "you", isAdmin = isAdmin("you"))
                }
            }

            // PROD
            return try {
                // This is the header used by the portal. We cannot spoof it
                val remoteUser = request.getHeader("remote_user")
                User(id = remoteUser, isAdmin = isAdmin(remoteUser))
            } catch (npe: java.lang.NullPointerException) {
                // java.lang.NullPointerException: remote_user must not be null
                // happens when the application is run in prod mode, but without the portal
                // In this case we default to a single user instance
                // Note that admin status is not set here
                User(id = "you", isAdmin = isAdmin("you"))
            } catch (ise: java.lang.IllegalStateException) {
                // java.lang.IllegalStateException: remote_user
                // happens when running with the proxy, but without the portal
                // TODO: figure this out. What to do with the proxy in both the portal and non-portal case?
                // probably we should not grant the user access here
                User(id = "you", isAdmin = isAdmin("you"))
            }
        }
    }
}