package br.com.developes.sdui.authentication

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

class LogoutUseCase {

    suspend fun logout(): Boolean {
        try {
            val auth = Firebase.auth
            auth.signOut()
        } catch (e: Exception) {
            return true
        }

        return false
    }
}