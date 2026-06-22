package com.example.ui.state

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSession(
    val email: String,
    val fullName: String,
    val token: String,
    val refreshToken: String,
    val isVerified: Boolean = true
)

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val session: UserSession) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("paynexa_auth_prefs", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Attempt token restore to keep authentication state persistent
        val savedEmail = sharedPrefs.getString("user_email", null)
        val savedName = sharedPrefs.getString("user_name", null)
        val savedToken = sharedPrefs.getString("access_token", null)
        val savedRefreshToken = sharedPrefs.getString("refresh_token", null)

        if (savedEmail != null && savedName != null && savedToken != null && savedRefreshToken != null) {
            _authState.value = AuthState.Authenticated(
                UserSession(
                    email = savedEmail,
                    fullName = savedName,
                    token = savedToken,
                    refreshToken = savedRefreshToken
                )
            )
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password fields are required.")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Please enter a valid global email address.")
            return
        }

        if (password.length < 6) {
            _authState.value = AuthState.Error("Security requires passwords to be 6+ characters.")
            return
        }

        _authState.value = AuthState.Loading

        // Simulate secure server-side JWT authentication check
        val dummyName = email.substringBefore("@").replaceFirstChar { it.uppercase() } + " Doe"
        val mockAccessToken = "eyK1002.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJmYXJqYW5hbWluMTExMUBnbWFpbC5jb20iLCJpYXQiOjE1MTYyMzkwMjJ9.signature"
        val mockRefreshToken = "rf_eyK1002.eyJzdWIiOiIxMj"

        sharedPrefs.edit().apply {
            putString("user_email", email)
            putString("user_name", dummyName)
            putString("access_token", mockAccessToken)
            putString("refresh_token", mockRefreshToken)
            apply()
        }

        _authState.value = AuthState.Authenticated(
            UserSession(
                email = email,
                fullName = dummyName,
                token = mockAccessToken,
                refreshToken = mockRefreshToken
            )
        )
    }

    fun register(fullName: String, email: String, password: String) {
        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("All registration fields must be completed.")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Please enter a valid global email address.")
            return
        }

        if (password.length < 6) {
            _authState.value = AuthState.Error("Security requires passwords to be 6+ characters.")
            return
        }

        _authState.value = AuthState.Loading

        // Create user session and generate encrypted JSON Web Token simulations
        val mockAccessToken = "eyK1002.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJmYXJqYW5hbWluMTExMUBnbWFpbC5jb20iLCJpYXQiOjE1MTYyMzkwMjJ9.signature"
        val mockRefreshToken = "rf_eyK1002.eyJzdWIiOiIxMj"

        sharedPrefs.edit().apply {
            putString("user_email", email)
            putString("user_name", fullName)
            putString("access_token", mockAccessToken)
            putString("refresh_token", mockRefreshToken)
            apply()
        }

        _authState.value = AuthState.Authenticated(
            UserSession(
                email = email,
                fullName = fullName,
                token = mockAccessToken,
                refreshToken = mockRefreshToken
            )
        )
    }

    fun resetPassword(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Please enter a valid registered email address.")
            return
        }
        // Simulation of token recovery link dispatched
        _authState.value = AuthState.Unauthenticated
    }

    fun logout() {
        sharedPrefs.edit().apply {
            remove("user_email")
            remove("user_name")
            remove("access_token")
            remove("refresh_token")
            apply()
        }
        _authState.value = AuthState.Unauthenticated
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}
