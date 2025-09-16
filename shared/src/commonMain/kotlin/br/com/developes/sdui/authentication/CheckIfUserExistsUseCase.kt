package br.com.developes.sdui.authentication

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

class CheckIfUserExistsUseCase {

    fun check(): Boolean {
        val auth = Firebase.auth
        return auth.currentUser != null
    }
}