package com.calendariomagico.app.testutil

import com.calendariomagico.app.data.remote.AuthGateway

class FakeAuthGateway(private val uid: String = "uid-test") : AuthGateway {
    private var signedIn = false
    override val currentUid: String? get() = if (signedIn) uid else null

    override suspend fun ensureSignedIn(): String {
        signedIn = true
        return uid
    }
}
