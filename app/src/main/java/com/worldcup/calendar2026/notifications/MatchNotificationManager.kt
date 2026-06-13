package com.worldcup.calendar2026.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.worldcup.calendar2026.R
import com.worldcup.calendar2026.domain.model.Match
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANNEL_ID = "match_updates"
private const val LIVE_NOTIFICATION_ID = 2601
private const val UPCOMING_NOTIFICATION_ID = 2602
private const val PREFS = "match_notifications"
private const val KEY_LAST_LIVE = "last_live"
private const val KEY_LAST_UPCOMING = "last_upcoming"

@Singleton
class MatchNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun notifyLiveMatches(liveMatches: List<Match>) {
        if (!canNotify() || liveMatches.isEmpty()) return
        createChannelIfNeeded()
        val fingerprint = liveMatches.joinToString("-") { "${it.id}:${it.homeGoals}:${it.awayGoals}:${it.elapsed}" }
        if (prefs.getString(KEY_LAST_LIVE, null) == fingerprint) return

        val first = liveMatches.first()
        val minute = first.elapsed?.let { "$it'" } ?: "LIVE"
        val score = "${first.home.name} ${first.homeGoals ?: 0} - ${first.awayGoals ?: 0} ${first.away.name}"
        val content = if (liveMatches.size > 1) {
            "$score • $minute (+${liveMatches.size - 1} more live match)"
        } else "$score • $minute"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_worldcup_icon)
            .setContentTitle("Live World Cup match")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(LIVE_NOTIFICATION_ID, notification)
        prefs.edit().putString(KEY_LAST_LIVE, fingerprint).apply()
    }

    fun notifyUpcomingMatches(matches: List<Match>) {
        if (!canNotify()) return
        val now = ZonedDateTime.now()
        val next = matches
            .filter { it.kickoff.isAfter(now) }
            .minByOrNull { it.kickoff.toInstant() }
            ?: return
        createChannelIfNeeded()
        val fingerprint = "${next.id}:${next.kickoff.toEpochSecond()}"
        if (prefs.getString(KEY_LAST_UPCOMING, null) == fingerprint) return

        val content = "${next.home.name} vs ${next.away.name} at ${next.kickoff.format(timeFormatter)}"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_worldcup_icon)
            .setContentTitle("Incoming World Cup match")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(UPCOMING_NOTIFICATION_ID, notification)
        prefs.edit().putString(KEY_LAST_UPCOMING, fingerprint).apply()
    }

    private fun canNotify(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Match updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Live and upcoming world cup match alerts"
                }
            )
        }
    }
}
