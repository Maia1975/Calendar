package com.calendariomagico.app

import android.app.Application
import com.calendariomagico.app.data.local.DeviceProfileStore
import com.calendariomagico.app.data.remote.FirebaseAuthGateway
import com.calendariomagico.app.data.remote.FirestoreCalendarBackend
import com.calendariomagico.app.data.repository.CalendarRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CalendarMagicoApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var repository: CalendarRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = CalendarRepository(
            backend = FirestoreCalendarBackend(),
            authGateway = FirebaseAuthGateway(),
            profileStore = DeviceProfileStore(this),
            externalScope = appScope
        )
        appScope.launch { repository.start() }
    }
}
