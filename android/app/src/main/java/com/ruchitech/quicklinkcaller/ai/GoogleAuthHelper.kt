package com.ruchitech.quicklinkcaller.ai

import android.accounts.Account
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val GEMINI_SCOPE = "oauth2:https://www.googleapis.com/auth/generative-language"
    }

    sealed class TokenResult {
        data class Success(val token: String) : TokenResult()
        data class ConsentRequired(val intent: Intent) : TokenResult()
        data class Failure(val error: String) : TokenResult()
    }

    suspend fun getToken(email: String): TokenResult = withContext(Dispatchers.IO) {
        val account = Account(email, "com.google")
        try {
            val token = GoogleAuthUtil.getToken(context, account, GEMINI_SCOPE)
            TokenResult.Success(token)
        } catch (e: UserRecoverableAuthException) {
            val consentIntent = e.intent ?: return@withContext TokenResult.Failure("No consent intent")
            TokenResult.ConsentRequired(consentIntent)
        } catch (e: Exception) {
            TokenResult.Failure(e.message ?: "Failed to get token")
        }
    }

    suspend fun invalidateToken(email: String) = withContext(Dispatchers.IO) {
        try {
            val account = Account(email, "com.google")
            val token = GoogleAuthUtil.getToken(context, account, GEMINI_SCOPE)
            GoogleAuthUtil.clearToken(context, token)
        } catch (_: Exception) {}
    }
}
