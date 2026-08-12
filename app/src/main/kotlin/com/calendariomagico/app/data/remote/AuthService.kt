package com.calendariomagico.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Every device signs in anonymously so Firestore security rules can attribute
 * writes to a stable uid, without asking a child or parent to create an account.
 */
class FirebaseAuthGateway(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthGateway {

    override val currentUid: String? get() = auth.currentUser?.uid

    override suspend fun ensureSignedIn(): String {
        auth.currentUser?.let { return it.uid }
        val result = auth.signInAnonymously().await()
        return result.user?.uid ?: error("Não foi possível iniciar sessão anónima.")
    }
}
