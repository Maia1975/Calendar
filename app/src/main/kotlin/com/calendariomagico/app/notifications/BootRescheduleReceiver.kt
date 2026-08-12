package com.calendariomagico.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.calendariomagico.app.data.local.DeviceProfileStore
import com.calendariomagico.app.data.remote.FirebaseAuthGateway
import com.calendariomagico.app.data.remote.FirestoreCalendarBackend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Alarms scheduled with AlarmManager are wiped on reboot. This receiver
 * re-reads whichever items are already cached (or re-fetches them) and
 * re-schedules their reminders so nothing silently stops firing.
 */
class BootRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(20_000) {
                    val profileStore = DeviceProfileStore(appContext)
                    val calendar = profileStore.activeCalendar.first() ?: return@withTimeoutOrNull
                    FirebaseAuthGateway().ensureSignedIn()
                    val items = FirestoreCalendarBackend().fetchItemsOnce(calendar.id)
                    ReminderScheduler(appContext).rescheduleAll(items)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
