package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SkipRecord
import com.example.data.model.SkipStatus
import com.example.data.model.VipMembership
import com.example.data.preset.VipPresetsCatalog
import com.example.util.SkipDateUtils
import com.example.work.VipWorkScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SkipVIP", appName)
    }

    @Test
    fun `test presets catalog has popular VIP clubs`() {
        val presets = VipPresetsCatalog.presets
        assertTrue(presets.isNotEmpty())
        assertTrue(presets.any { it.name == "Fabletics" })
        assertTrue(presets.any { it.name == "Savage X Fenty" })
        assertTrue(presets.any { it.name == "JustFab" })
    }

    @Test
    fun `test status resolution for skipped record`() {
        val membership = VipMembership(
            id = 1L,
            name = "Fabletics",
            monthlyCost = 69.95,
            windowStartDay = 1,
            windowEndDay = 5
        )
        val record = SkipRecord(
            membershipId = 1L,
            year = 2026,
            month = 11,
            status = "SKIPPED",
            savedAmount = 69.95
        )

        val result = SkipDateUtils.calculateStatus(
            membership = membership,
            record = record,
            viewYear = 2026,
            viewMonth = 11
        )

        assertEquals(SkipStatus.SKIPPED, result.status)
        assertTrue(result.displayStatusMessage.contains("Skipped"))
    }
}
