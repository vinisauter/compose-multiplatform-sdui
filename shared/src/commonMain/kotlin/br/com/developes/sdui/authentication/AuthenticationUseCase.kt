package br.com.developes.sdui.authentication

import br.com.developes.sdui.SDCLibrary
import br.com.developes.sdui.logger
import br.com.developes.sdui.utils.emailAddressRegex
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

class AuthenticationUseCase {

    suspend fun login(email: String?, password: String?): Boolean {
        if (email?.trim()?.isEmpty() == true || password?.trim()?.isEmpty() == true) {
            val errorMessage = "Informe o Email e a Senha !!!"
            SDCLibrary.onError(Throwable(errorMessage))
            logger.e(errorMessage)

        } else if (email?.matches(emailAddressRegex) == false) {
            val errorMessage = "Email inválido !!!"
            SDCLibrary.onError(Throwable(errorMessage))
            logger.e(errorMessage)

        } else {
            val auth = Firebase.auth

            try {
                val signInResult = auth.signInWithEmailAndPassword(email!!, password!!)
                return signInResult.user != null

            } catch (e: Exception) {
                logger.e(e.message!!)
                SDCLibrary.onError(Throwable("Email e/ou senha incorreto !!!"))
            }
        }

        return false
    }
}