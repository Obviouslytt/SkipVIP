package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MembershipWithStatus
import com.example.data.model.SkipStatus
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberOnContainer
import com.example.ui.theme.AmberUrgent
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RoseOnContainer
import com.example.ui.theme.RoseMissed
import com.example.util.SkipDateUtils

@Composable
fun MembershipCard(
    item: MembershipWithStatus,
    isCurrentMonth: Boolean,
    onToggleSkip: () -> Unit,
    onOpenDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkBilled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val membership = item.membership
    val isSkipped = item.status == SkipStatus.SKIPPED
    var menuExpanded by remember { mutableStateOf(false) }

    val brandColor = Color(membership.colorHex)

    val cardBorderColor by animateColorAsState(
        targetValue = when (item.status) {
            SkipStatus.SKIPPED -> EmeraldSafe.copy(alpha = 0.6f)
            SkipStatus.URGENT_CLOSING_SOON -> AmberUrgent
            SkipStatus.WINDOW_OPEN -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            SkipStatus.MISSED -> RoseMissed.copy(alpha = 0.4f)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        },
        label = "cardBorderColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (item.status == SkipStatus.URGENT_CLOSING_SOON) 2.dp else 1.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onOpenDetails() }
            .testTag("membership_card_${membership.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSkipped) 1.dp else 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Brand Avatar, Title, Cost, and Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(brandColor.copy(alpha = 0.15f))
                        .border(1.dp, brandColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = membership.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = brandColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = membership.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = membership.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Cost Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = SkipDateUtils.formatCurrency(membership.monthlyCost, membership.currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "/month",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Overflow menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Skip Tips & Details") },
                            onClick = {
                                menuExpanded = false
                                onOpenDetails()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Info, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mark as Billed") },
                            onClick = {
                                menuExpanded = false
                                onMarkBilled()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Membership") },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Banner Chip
            val (statusBg, statusFg, statusIcon) = when (item.status) {
                SkipStatus.SKIPPED -> Triple(EmeraldContainer, EmeraldOnContainer, Icons.Default.CheckCircle)
                SkipStatus.URGENT_CLOSING_SOON -> Triple(AmberContainer, AmberOnContainer, Icons.Default.Warning)
                SkipStatus.WINDOW_OPEN -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Icons.Default.Info)
                SkipStatus.MISSED -> Triple(RoseContainer, RoseOnContainer, Icons.Default.Warning)
                SkipStatus.UPCOMING -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.Info)
                SkipStatus.PAUSED -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.Info)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusFg,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.displayStatusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = statusFg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Skip Window Timeline (1st to 5th)
            SkipWindowTimeline(
                startDay = membership.windowStartDay,
                endDay = membership.windowEndDay,
                status = item.status,
                isCurrentMonth = isCurrentMonth
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Actions: "Mark Skipped" and "Open Portal"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSkipped) {
                    OutlinedButton(
                        onClick = onToggleSkip,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("undo_skip_${membership.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = EmeraldSafe
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(EmeraldSafe)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Skipped ✓ (Undo)", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    ElevatedButton(
                        onClick = onToggleSkip,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mark_skipped_${membership.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = if (item.status == SkipStatus.URGENT_CLOSING_SOON) AmberUrgent else MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mark as Skipped",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Open Portal Website Button
                if (membership.portalUrl.isNotBlank()) {
                    FilledTonalButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(membership.portalUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("open_portal_${membership.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Open Skip Website",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
