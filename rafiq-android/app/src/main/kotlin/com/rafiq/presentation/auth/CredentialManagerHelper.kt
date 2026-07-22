package com.rafiq.presentation.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import kotlinx.coroutines.CancellationException

object CredentialManagerHelper {

    suspend fun savePassword(
        context: Context,
        email: String,
        password: String
    ): Boolean {
        try {
            val credentialManager = CredentialManager.create(context)
            val request = CreatePasswordRequest(
                id = email,
                password = password
            )
            credentialManager.createCredential(
                context = context,
                request = request
            )
            return true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("CredentialManager", "Error saving password", e)
            return false
        }
    }

    suspend fun getPassword(context: Context): PasswordCredential? {
        try {
            val credentialManager = CredentialManager.create(context)
            
            val getPasswordOption = GetPasswordOption()
            val request = GetCredentialRequest(
                listOf(getPasswordOption)
            )

            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = result.credential
            if (credential is PasswordCredential) {
                return credential
            }
        } catch (e: GetCredentialCancellationException) {
            Log.i("CredentialManager", "User cancelled the credential request")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("CredentialManager", "Error getting password", e)
        }
        return null
    }
}
