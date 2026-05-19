package com.emonotion.app.utils

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.emonotion.app.R
import com.emonotion.app.domain.model.UserStats

/**
 * Единое отображение серии записей в дневнике (огонёк, рамки и цвета текста).
 */
object StreakUiHelper {

    fun isStreakActiveToday(stats: UserStats): Boolean = stats.hasEntryToday

    fun isStreakAtRisk(stats: UserStats): Boolean =
        stats.currentStreak > 0 && !stats.hasEntryToday

    fun shouldShowStreakBadge(stats: UserStats): Boolean =
        stats.currentStreak >= 2 || isStreakAtRisk(stats)

    fun applyHomeStreak(
        outerContainer: View?,
        flame: ImageView?,
        countText: TextView?,
        activeToday: Boolean
    ) {
        outerContainer?.setBackgroundResource(
            if (activeToday) R.drawable.streak_background else R.drawable.streak_background_inactive
        )
        applyFlameTint(flame, activeToday)
        countText?.setTextColor(
            ContextCompat.getColor(
                countText.context,
                if (activeToday) R.color.primary_foreground else R.color.muted_foreground
            )
        )
    }

    fun applyCalendarStreak(
        card: View?,
        iconContainer: View?,
        flame: ImageView?,
        labelText: TextView?,
        countText: TextView?,
        messageText: TextView?,
        activeToday: Boolean
    ) {
        card?.setBackgroundResource(
            if (activeToday) R.drawable.streak_card_background
            else R.drawable.streak_card_background_inactive
        )
        iconContainer?.setBackgroundResource(
            if (activeToday) R.drawable.streak_icon_background
            else R.drawable.streak_icon_background_inactive
        )
        applyFlameTint(flame, activeToday)

        val context = card?.context ?: return
        if (activeToday) {
            val white = ContextCompat.getColor(context, android.R.color.white)
            labelText?.setTextColor(white)
            labelText?.alpha = 0.8f
            countText?.setTextColor(white)
            messageText?.setTextColor(white)
            messageText?.alpha = 0.7f
        } else {
            labelText?.setTextColor(ContextCompat.getColor(context, R.color.muted_foreground))
            labelText?.alpha = 1f
            countText?.setTextColor(ContextCompat.getColor(context, R.color.foreground))
            messageText?.setTextColor(ContextCompat.getColor(context, R.color.muted_foreground))
            messageText?.alpha = 1f
        }
    }

    fun applyProfileStreak(
        iconContainer: View?,
        flame: ImageView?,
        activeToday: Boolean
    ) {
        iconContainer?.setBackgroundResource(
            if (activeToday) R.drawable.streak_icon_frame_active
            else R.drawable.streak_icon_frame_inactive
        )
        applyFlameTint(flame, activeToday)
    }

    fun applyFlameTint(imageView: ImageView?, activeToday: Boolean) {
        imageView ?: return
        val colorRes = if (activeToday) R.color.accent else R.color.muted_foreground
        imageView.setColorFilter(ContextCompat.getColor(imageView.context, colorRes))
    }

    fun dayWord(context: Context, streak: Int): String = when {
        streak % 10 == 1 && streak % 100 != 11 -> context.getString(R.string.day_singular)
        streak % 10 in 2..4 && streak % 100 !in 12..14 -> context.getString(R.string.day_plural_2_4)
        else -> context.getString(R.string.day_plural_5_20)
    }

    fun streakMessage(context: Context, streak: Int, atRisk: Boolean): String {
        if (atRisk) {
            return context.getString(R.string.streak_message_at_risk)
        }
        return when (streak) {
            in 2..4 -> context.getString(R.string.streak_message_start)
            in 5..7 -> context.getString(R.string.streak_message_good)
            in 8..14 -> context.getString(R.string.streak_message_great)
            in 15..29 -> context.getString(R.string.streak_message_amazing)
            else -> context.getString(R.string.streak_message_legend)
        }
    }
}
