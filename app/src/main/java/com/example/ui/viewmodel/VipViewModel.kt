package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SkipVipApplication
import com.example.data.model.MembershipWithStatus
import com.example.data.model.MonthlySummary
import com.example.data.model.SkipRecord
import com.example.data.model.SkipStatus
import com.example.data.model.VipMembership
import com.example.data.preset.VipPreset
import com.example.notification.NotificationHelper
import com.example.util.SkipDateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MembershipFilter(val label: String) {
    ALL("All"),
    ACTION_NEEDED("Needs Action"),
    SKIPPED("Skipped"),
    UPCOMING("Upcoming")
}

data class UiCelebrationEvent(
    val message: String,
    val savedAmount: Double,
    val membershipName: String
)

class VipViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as SkipVipApplication).repository

    // Current selected month & year for inspection
    val currentCalendarYear = SkipDateUtils.getCurrentYear()
    val currentCalendarMonth = SkipDateUtils.getCurrentMonth()

    val selectedYear = MutableStateFlow(currentCalendarYear)
    val selectedMonth = MutableStateFlow(currentCalendarMonth)

    val currentFilter = MutableStateFlow(MembershipFilter.ALL)
    val celebrationEvent = MutableStateFlow<UiCelebrationEvent?>(null)

    // Sheets & Dialogs state
    val selectedMembershipForDetail = MutableStateFlow<VipMembership?>(null)
    val isAddEditSheetOpen = MutableStateFlow(false)
    val membershipToEdit = MutableStateFlow<VipMembership?>(null)
    val isPresetsSheetOpen = MutableStateFlow(false)

    // Stream records for currently selected month
    private val recordsForSelectedMonth = combine(selectedYear, selectedMonth) { year, month ->
        Pair(year, month)
    }.flatMapLatest { (year, month) ->
        repository.getRecordsForMonthFlow(year, month)
    }

    val allTimeSaved: StateFlow<Double> = repository.allTimeSavedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val allTimeSkipsCount: StateFlow<Int> = repository.allTimeSkipsCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allMemberships: StateFlow<List<VipMembership>> = repository.allMembershipsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecords: StateFlow<List<SkipRecord>> = repository.allRecordsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined list of memberships with computed status for current view month
    val membershipsWithStatus: StateFlow<List<MembershipWithStatus>> = combine(
        repository.allMembershipsFlow,
        recordsForSelectedMonth,
        selectedYear,
        selectedMonth
    ) { memberships, records, year, month ->
        val recordMap = records.associateBy { it.membershipId }
        memberships.map { membership ->
            SkipDateUtils.calculateStatus(
                membership = membership,
                record = recordMap[membership.id],
                viewYear = year,
                viewMonth = month
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monthly summary calculation
    val monthlySummary: StateFlow<MonthlySummary> = combine(
        membershipsWithStatus,
        allTimeSaved,
        allTimeSkipsCount,
        selectedYear,
        selectedMonth
    ) { items, lifetimeSaved, lifetimeSkips, year, month ->
        val activeItems = items.filter { it.membership.isActive }
        val skippedItems = items.filter { it.status == SkipStatus.SKIPPED }
        val openItems = items.filter { it.status == SkipStatus.WINDOW_OPEN || it.status == SkipStatus.URGENT_CLOSING_SOON }
        val missedItems = items.filter { it.status == SkipStatus.MISSED }

        val totalSavedThisMonth = skippedItems.sumOf { it.membership.monthlyCost }
        val totalAvoidableCost = activeItems.sumOf { it.membership.monthlyCost }

        MonthlySummary(
            year = year,
            month = month,
            totalSavedThisMonth = totalSavedThisMonth,
            totalAvoidableCost = totalAvoidableCost,
            activeMembershipsCount = activeItems.size,
            skippedCount = skippedItems.size,
            unskippedOpenCount = openItems.size,
            missedCount = missedItems.size,
            allTimeSaved = lifetimeSaved,
            allTimeSkipsCount = lifetimeSkips
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlySummary(
            year = currentCalendarYear,
            month = currentCalendarMonth,
            totalSavedThisMonth = 0.0,
            totalAvoidableCost = 0.0,
            activeMembershipsCount = 0,
            skippedCount = 0,
            unskippedOpenCount = 0,
            missedCount = 0,
            allTimeSaved = 0.0,
            allTimeSkipsCount = 0
        )
    )

    fun toggleSkip(membership: VipMembership, currentStatus: SkipStatus) {
        viewModelScope.launch {
            val year = selectedYear.value
            val month = selectedMonth.value
            if (currentStatus == SkipStatus.SKIPPED) {
                // Unmark
                repository.unmarkStatus(membership.id, year, month)
            } else {
                // Mark skipped!
                repository.markSkipped(membership, year, month)
                celebrationEvent.value = UiCelebrationEvent(
                    message = "Awesome! You skipped ${membership.name} and saved ${SkipDateUtils.formatCurrency(membership.monthlyCost)}! 🎉",
                    savedAmount = membership.monthlyCost,
                    membershipName = membership.name
                )
            }
        }
    }

    fun markAllOpenAsSkipped() {
        viewModelScope.launch {
            val year = selectedYear.value
            val month = selectedMonth.value
            val openItems = membershipsWithStatus.value.filter {
                it.status == SkipStatus.WINDOW_OPEN || it.status == SkipStatus.URGENT_CLOSING_SOON
            }
            var totalSaved = 0.0
            openItems.forEach { item ->
                repository.markSkipped(item.membership, year, month)
                totalSaved += item.membership.monthlyCost
            }
            if (openItems.isNotEmpty()) {
                celebrationEvent.value = UiCelebrationEvent(
                    message = "Boom! Skipped all ${openItems.size} memberships and saved ${SkipDateUtils.formatCurrency(totalSaved)}! 🛡️",
                    savedAmount = totalSaved,
                    membershipName = "All Open"
                )
            }
        }
    }

    fun markAsBilled(membership: VipMembership) {
        viewModelScope.launch {
            repository.markBilled(membership, selectedYear.value, selectedMonth.value)
        }
    }

    fun clearCelebration() {
        celebrationEvent.value = null
    }

    fun nextMonth() {
        if (selectedMonth.value == 12) {
            selectedMonth.value = 1
            selectedYear.value += 1
        } else {
            selectedMonth.value += 1
        }
    }

    fun prevMonth() {
        if (selectedMonth.value == 1) {
            selectedMonth.value = 12
            selectedYear.value -= 1
        } else {
            selectedMonth.value -= 1
        }
    }

    fun resetToCurrentMonth() {
        selectedYear.value = currentCalendarYear
        selectedMonth.value = currentCalendarMonth
    }

    fun addFromPreset(preset: VipPreset) {
        viewModelScope.launch {
            repository.addMembership(preset.toVipMembership())
            isPresetsSheetOpen.value = false
        }
    }

    fun saveMembership(membership: VipMembership) {
        viewModelScope.launch {
            if (membership.id == 0L) {
                repository.addMembership(membership)
            } else {
                repository.updateMembership(membership)
            }
            isAddEditSheetOpen.value = false
            membershipToEdit.value = null
        }
    }

    fun deleteMembership(membership: VipMembership) {
        viewModelScope.launch {
            repository.deleteMembership(membership)
            selectedMembershipForDetail.value = null
        }
    }

    fun openEditMembership(membership: VipMembership) {
        membershipToEdit.value = membership
        isAddEditSheetOpen.value = true
        selectedMembershipForDetail.value = null
    }

    fun openAddCustomMembership() {
        membershipToEdit.value = null
        isAddEditSheetOpen.value = true
    }

    val workInfoList: StateFlow<List<androidx.work.WorkInfo>> = com.example.work.VipWorkScheduler
        .getWorkInfoFlow(application)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun triggerWorkManagerCheck() {
        com.example.work.VipWorkScheduler.enqueueImmediateCheck(getApplication())
    }

    fun seedSampleHistory() {
        viewModelScope.launch {
            val memberships = allMemberships.value
            val currentYear = selectedYear.value
            val sampleMonths = listOf(
                Triple(currentYear, 8, "Skipped on Aug 2"),
                Triple(currentYear, 9, "Skipped on Sep 3"),
                Triple(currentYear, 10, "Skipped on Oct 4")
            )

            memberships.forEach { membership ->
                sampleMonths.forEach { (year, month, note) ->
                    repository.markSkipped(membership, year, month)
                }
            }
        }
    }

    fun triggerTestNotification() {
        NotificationHelper.sendTestNotification(getApplication())
    }
}
