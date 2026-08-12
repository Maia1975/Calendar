package com.calendariomagico.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getStringExtra(EXTRA_ITEM_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val emoji = intent.getStringExtra(EXTRA_EMOJI).orEmpty()
        if (title.isBlank()) return
        NotificationHelper.showReminder(
            context = context,
            notificationId = itemId.hashCode(),
            emoji = emoji,
            title = title,
            text = "Está quase na hora! 💛"
        )
    }

    companion object {
        const val EXTRA_ITEM_ID = "extra_item_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_EMOJI = "extra_emoji"
    }
}
