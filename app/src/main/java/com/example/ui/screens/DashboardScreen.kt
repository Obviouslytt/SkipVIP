package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SkipStatus
import com.example.ui.components.CelebrationSnackbar
import com.example.ui.components.HeroSavingsCard
import com.example.ui.components.MembershipCard
import com.example.ui.components.MonthSelector
import com.example.ui.components.QuickActionsFloatingMenu
import com.example.ui.sheets.AddEditMembershipSheet
import com.example.ui.sheets.MembershipDetailSheet
import com.example.ui.sheets.PresetsSheet
import com.example.ui.viewmodel.MembershipFilter
import com.example.ui.viewmodel.VipViewModel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timeline
import com.example.ui.theme.EmeraldSafe
import com.example.util.SkipDateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: VipViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToGuide: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val membershipsWithStatus by viewModel.membershipsWithStatus.collectAsState()
    val monthlySummary by viewModel.monthlySummary.collectAsState()
    val celebrationEvent by viewModel.celebrationEvent.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()
    val allMemberships by viewModel.allMemberships.collectAsState()
    val membershipMap = allMemberships.associateBy { it.id }

    val isAddEditSheetOpen by viewModel.isAddEditSheetOpen.collectAsState()
    val membershipToEdit by viewModel.membershipToEdit.collectAsState()
    val isPresetsSheetOpen by viewModel.isPresetsSheetOpen.collectAsState()
    val selectedMembershipForDetail by viewModel.selectedMembershipForDetail.collectAsState()

    val addEditSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val presetsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentYear = SkipDateUtils.getCurrentYear()
    val currentMonth = SkipDateUtils.getCurrentMonth()
    val isCurrentMonth = selectedYear == currentYear && selectedMonth == currentMonth

    // Filter memberships based on current chip
    val filteredList = membershipsWithStatus.filter { item ->
        when (currentFilter) {
            MembershipFilter.ALL -> true
            MembershipFilter.ACTION_NEEDED -> item.status == SkipStatus.WINDOW_OPEN || item.status == SkipStatus.URGENT_CLOSING_SOON
            MembershipFilter.SKIPPED -> item.status == SkipStatus.SKIPPED
            MembershipFilter.UPCOMING -> item.status == SkipStatus.UPCOMING
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SkipVIP",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Monthly Skip Reminder",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.isPresetsSheetOpen.value = true },
                        modifier = Modifier.testTag("action_presets")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "Popular Clubs Catalog"
                        )
                    }
                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.testTag("action_history")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Savings History"
                        )
                    }
                    IconButton(
                        onClick = onNavigateToGuide,
                        modifier = Modifier.testTag("action_guide")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Skip Guide & Tips"
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("action_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            QuickActionsFloatingMenu(
                memberships = membershipsWithStatus,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onToggleSkip = { membership, status ->
                    viewModel.toggleSkip(membership, status)
                },
                onSkipAllOpen = { viewModel.markAllOpenAsSkipped() },
                onAddClub = { viewModel.openAddCustomMembership() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Month Selector
                item {
                    MonthSelector(
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        onPreviousMonth = { viewModel.prevMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onResetToCurrentMonth = { viewModel.resetToCurrentMonth() }
                    )
                }

                // Hero Savings & Status Card
                item {
                    HeroSavingsCard(
                        summary = monthlySummary,
                        onSkipAllOpen = { viewModel.markAllOpenAsSkipped() }
                    )
                }

                // Filter Chips Row
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(MembershipFilter.values()) { filter ->
                            val count = when (filter) {
                                MembershipFilter.ALL -> membershipsWithStatus.size
                                MembershipFilter.ACTION_NEEDED -> membershipsWithStatus.count {
                                    it.status == SkipStatus.WINDOW_OPEN || it.status == SkipStatus.URGENT_CLOSING_SOON
                                }
                                MembershipFilter.SKIPPED -> membershipsWithStatus.count { it.status == SkipStatus.SKIPPED }
                                MembershipFilter.UPCOMING -> membershipsWithStatus.count { it.status == SkipStatus.UPCOMING }
                            }

                            ElevatedFilterChip(
                                selected = currentFilter == filter,
                                onClick = { viewModel.currentFilter.value = filter },
                                label = {
                                    Text("${filter.label} ($count)", fontWeight = FontWeight.SemiBold)
                                },
                                modifier = Modifier.testTag("filter_chip_${filter.name}")
                            )
                        }
                    }
                }

                // Membership Items
                if (filteredList.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No memberships in this view",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add popular VIP clubs like Fabletics or Savage X Fenty to begin tracking your monthly skip deadlines.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { viewModel.isPresetsSheetOpen.value = true },
                                    modifier = Modifier.testTag("empty_add_preset_button")
                                ) {
                                    Text("Pick from Popular Clubs")
                                }
                            }
                        }
                    }
                } else {
                    items(
                        items = filteredList,
                        key = { it.membership.id }
                    ) { item ->
                        MembershipCard(
                            item = item,
                            isCurrentMonth = isCurrentMonth,
                            onToggleSkip = { viewModel.toggleSkip(item.membership, item.status) },
                            onOpenDetails = { viewModel.selectedMembershipForDetail.value = item.membership },
                            onEdit = { viewModel.openEditMembership(item.membership) },
                            onDelete = { viewModel.deleteMembership(item.membership) },
                            onMarkBilled = { viewModel.markAsBilled(item.membership) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Recent Skip History & Savings Log Section
                if (allRecords.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clickable { onNavigateToHistory() }
                                .testTag("dashboard_history_preview_card"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Timeline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Recent Skip Log & Savings",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = "View All →",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                allRecords.take(3).forEach { record ->
                                    val clubName = membershipMap[record.membershipId]?.name ?: "VIP Club"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = EmeraldSafe,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "$clubName • ${SkipDateUtils.getShortMonthName(record.month)} ${record.year}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Text(
                                            text = "+${SkipDateUtils.formatCurrency(record.savedAmount)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldSafe
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Celebration Banner popup at bottom
            CelebrationSnackbar(
                event = celebrationEvent,
                onDismiss = { viewModel.clearCelebration() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 70.dp)
            )
        }
    }

    // Add / Edit Sheet
    if (isAddEditSheetOpen) {
        AddEditMembershipSheet(
            sheetState = addEditSheetState,
            membershipToEdit = membershipToEdit,
            onDismiss = { viewModel.isAddEditSheetOpen.value = false },
            onSave = { updated -> viewModel.saveMembership(updated) }
        )
    }

    // Popular Presets Sheet
    if (isPresetsSheetOpen) {
        PresetsSheet(
            sheetState = presetsSheetState,
            onDismiss = { viewModel.isPresetsSheetOpen.value = false },
            onSelectPreset = { preset -> viewModel.addFromPreset(preset) }
        )
    }

    // Membership Detail Sheet
    if (selectedMembershipForDetail != null) {
        MembershipDetailSheet(
            sheetState = detailSheetState,
            membership = selectedMembershipForDetail,
            onDismiss = { viewModel.selectedMembershipForDetail.value = null },
            onToggleSkip = { membership ->
                val currentStatus = membershipsWithStatus.find { it.membership.id == membership.id }?.status ?: SkipStatus.WINDOW_OPEN
                viewModel.toggleSkip(membership, currentStatus)
            },
            onEdit = { membership -> viewModel.openEditMembership(membership) },
            onDelete = { membership -> viewModel.deleteMembership(membership) },
            onMarkBilled = { membership -> viewModel.markAsBilled(membership) }
        )
    }
}
