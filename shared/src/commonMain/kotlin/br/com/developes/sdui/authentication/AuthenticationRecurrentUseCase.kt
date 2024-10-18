package br.com.developes.sdui.authentication

import br.com.developes.sdui.SDCLibrary
import br.com.developes.sdui.logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

class AuthenticationRecurrentUseCase {

    suspend fun login(password: String?): Boolean {
        val auth = Firebase.auth
        val email = auth.currentUser?.email

        if (password?.trim()?.isEmpty() == true) {
            val errorMessage = "Informe a Senha !!!"
            SDCLibrary.onError(Throwable(errorMessage))
            logger.e(errorMessage)

        } else {
            try {
                val signInResult = auth.signInWithEmailAndPassword(email!!, password!!)
                return signInResult.user != null

            } catch (e: Exception) {
                logger.e(e.message!!)
                SDCLibrary.onError(Throwable("Senha Incorreta !!!"))
            }
        }

        return false
    }
}