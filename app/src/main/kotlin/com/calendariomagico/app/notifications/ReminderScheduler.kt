package com.calendariomagico.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.calendariomagico.app.data.model.CalendarItem

/**
 * Reminders are scheduled locally on each device from the data it already has
 * synced from Firestore. This avoids needing a push-notification server: as
 * long as the phone has received the item once, its own alarm will fire.
 */
class ReminderScheduler(private val context: Context) {
    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(item: CalendarItem) {
        val minutesBefore = item.reminderMinutesBefore
        if (minutesBefore == null || item.isDone) {
            cancel(item.id)
            return
        }
        val triggerAt = item.dateTimeMillis - minutesBefore * 60_000L
        if (triggerAt <= System.currentTimeMillis()) {
            cancel(item.id)
            return
        }
        val pendingIntent = buildPendingIntent(item.id, item.title, item.type.emoji)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }

    fun cancel(itemId: String) {
        alarmManager.cancel(buildPendingIntent(itemId, "", ""))
    }

    fun rescheduleAll(items: List<CalendarItem>) {
        items.forEach { schedule(it) }
    }

    private fun buildPendingIntent(itemId: String, title: String, emoji: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "$REMINDER_ACTION_PREFIX$itemId"
            putExtra(ReminderReceiver.EXTRA_ITEM_ID, itemId)
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_EMOJI, emoji)
        }
        return PendingIntent.getBroadcast(
            context,
            itemId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val REMINDER_ACTION_PREFIX = "com.calendariomagico.app.REMINDER_"
    }
}
