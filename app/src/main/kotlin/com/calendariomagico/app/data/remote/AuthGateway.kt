package com.calendariomagico.app.data.remote

interface AuthGateway {
    val currentUid: String?
    suspend fun ensureSignedIn(): String
}
