package com.example

import com.example.data.model.SkipStatus
import com.example.data.model.VipMembership
import com.example.util.SkipDateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCurrencyFormatting() {
        val formatted = SkipDateUtils.formatCurrency(69.95)
        assertTrue(formatted.contains("69.95"))
    }

    @Test
    fun testMonthNames() {
        assertEquals("November", SkipDateUtils.getMonthName(11))
        assertEquals("Dec", SkipDateUtils.getShortMonthName(12))
    }

    @Test
    fun testUpcomingFutureMonth() {
        val membership = VipMembership(
            id = 2L,
            name = "Savage X Fenty",
            monthlyCost = 49.95,
            windowStartDay = 1,
            windowEndDay = 5
        )

        val result = SkipDateUtils.calculateStatus(
            membership = membership,
            record = null,
            viewYear = 2030,
            viewMonth = 1
        )

        assertEquals(SkipStatus.UPCOMING, result.status)
    }
}
