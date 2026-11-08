package com.example.util

import com.example.data.model.MembershipWithStatus
import com.example.data.model.SkipRecord
import com.example.data.model.SkipStatus
import com.example.data.model.VipMembership
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

object SkipDateUtils {

    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
    fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-12
    fun getCurrentDay(): Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> "Month $month"
        }
    }

    fun getShortMonthName(month: Int): String {
        return when (month) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> "$month"
        }
    }

    fun formatCurrency(amount: Double, currency: String = "$"): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        val formatted = format.format(amount)
        return if (currency == "$") formatted else "$currency${String.format(Locale.US, "%.2f", amount)}"
    }

    fun calculateStatus(
        membership: VipMembership,
        record: SkipRecord?,
        viewYear: Int,
        viewMonth: Int
    ): MembershipWithStatus {
        val currentYear = getCurrentYear()
        val currentMonth = getCurrentMonth()
        val currentDay = getCurrentDay()

        if (!membership.isActive) {
            return MembershipWithStatus(
                membership = membership,
                record = record,
                status = SkipStatus.PAUSED,
                daysRemainingInWindow = 0,
                daysUntilWindowOpens = 0,
                isWindowCurrentlyOpen = false,
                displayStatusMessage = "Tracking paused"
            )
        }

        if (record != null) {
            if (record.status == "SKIPPED") {
                return MembershipWithStatus(
                    membership = membership,
                    record = record,
                    status = SkipStatus.SKIPPED,
                    daysRemainingInWindow = 0,
                    daysUntilWindowOpens = 0,
                    isWindowCurrentlyOpen = false,
                    displayStatusMessage = "Skipped & Safe! Saved ${formatCurrency(membership.monthlyCost, membership.currency)}"
                )
            } else if (record.status == "PAUSED") {
                return MembershipWithStatus(
                    membership = membership,
                    record = record,
                    status = SkipStatus.PAUSED,
                    daysRemainingInWindow = 0,
                    daysUntilWindowOpens = 0,
                    isWindowCurrentlyOpen = false,
                    displayStatusMessage = "Paused for ${getMonthName(viewMonth)}"
                )
            } else if (record.status == "BILLED") {
                return MembershipWithStatus(
                    membership = membership,
                    record = record,
                    status = SkipStatus.MISSED,
                    daysRemainingInWindow = 0,
                    daysUntilWindowOpens = 0,
                    isWindowCurrentlyOpen = false,
                    displayStatusMessage = "Marked as Billed (${formatCurrency(membership.monthlyCost, membership.currency)})"
                )
            }
        }

        // Compare view year/month with current year/month
        val viewTimeIndex = viewYear * 12 + viewMonth
        val currentTimeIndex = currentYear * 12 + currentMonth

        if (viewTimeIndex < currentTimeIndex) {
            // Past month with no record
            return MembershipWithStatus(
                membership = membership,
                record = null,
                status = SkipStatus.MISSED,
                daysRemainingInWindow = 0,
                daysUntilWindowOpens = 0,
                isWindowCurrentlyOpen = false,
                displayStatusMessage = "Missed window (${getMonthName(viewMonth)} ${membership.windowEndDay})"
            )
        } else if (viewTimeIndex > currentTimeIndex) {
            // Future month
            val monthsAhead = viewTimeIndex - currentTimeIndex
            return MembershipWithStatus(
                membership = membership,
                record = null,
                status = SkipStatus.UPCOMING,
                daysRemainingInWindow = 0,
                daysUntilWindowOpens = 30 * monthsAhead,
                isWindowCurrentlyOpen = false,
                displayStatusMessage = "Window opens ${getShortMonthName(viewMonth)} ${membership.windowStartDay}"
            )
        } else {
            // Current month!
            val startDay = membership.windowStartDay
            val endDay = membership.windowEndDay

            return when {
                currentDay < startDay -> {
                    val daysUntil = startDay - currentDay
                    MembershipWithStatus(
                        membership = membership,
                        record = null,
                        status = SkipStatus.UPCOMING,
                        daysRemainingInWindow = 0,
                        daysUntilWindowOpens = daysUntil,
                        isWindowCurrentlyOpen = false,
                        displayStatusMessage = "Opens in $daysUntil day${if (daysUntil > 1) "s" else ""} (${getShortMonthName(viewMonth)} $startDay)"
                    )
                }
                currentDay in startDay..endDay -> {
                    val daysLeft = endDay - currentDay
                    val isUrgent = daysLeft <= 1
                    val status = if (isUrgent) SkipStatus.URGENT_CLOSING_SOON else SkipStatus.WINDOW_OPEN
                    val msg = when (daysLeft) {
                        0 -> "⚠️ Closes TODAY! Avoid ${formatCurrency(membership.monthlyCost, membership.currency)} charge"
                        1 -> "⚠️ Closes TOMORROW! (${getShortMonthName(viewMonth)} $endDay)"
                        else -> "Skip window OPEN • Closes in ${daysLeft + 1} days"
                    }
                    MembershipWithStatus(
                        membership = membership,
                        record = null,
                        status = status,
                        daysRemainingInWindow = daysLeft,
                        daysUntilWindowOpens = 0,
                        isWindowCurrentlyOpen = true,
                        displayStatusMessage = msg
                    )
                }
                else -> {
                    // currentDay > endDay
                    MembershipWithStatus(
                        membership = membership,
                        record = null,
                        status = SkipStatus.MISSED,
                        daysRemainingInWindow = 0,
                        daysUntilWindowOpens = 0,
                        isWindowCurrentlyOpen = false,
                        displayStatusMessage = "Deadline passed on ${getShortMonthName(viewMonth)} $endDay"
                    )
                }
            }
        }
    }
}
