package com.example.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.VipMembership
import com.example.ui.theme.ColorFabletics
import com.example.ui.theme.ColorSavageX
import com.example.ui.theme.ColorJustFab
import com.example.ui.theme.ColorShoeDazzle
import com.example.ui.theme.ColorAdoreMe
import com.example.ui.theme.ColorBookOfMonth
import com.example.ui.theme.ColorBespoke
import com.example.ui.theme.ColorBarkBox
import com.example.ui.theme.ColorCustom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMembershipSheet(
    sheetState: SheetState,
    membershipToEdit: VipMembership?,
    onDismiss: () -> Unit,
    onSave: (VipMembership) -> Unit
) {
    var name by remember(membershipToEdit) { mutableStateOf(membershipToEdit?.name ?: "") }
    var costText by remember(membershipToEdit) {
        mutableStateOf(membershipToEdit?.let { String.format("%.2f", it.monthlyCost) } ?: "69.95")
    }
    var startDayText by remember(membershipToEdit) {
        mutableStateOf(membershipToEdit?.windowStartDay?.toString() ?: "1")
    }
    var endDayText by remember(membershipToEdit) {
        mutableStateOf(membershipToEdit?.windowEndDay?.toString() ?: "5")
    }
    var category by remember(membershipToEdit) {
        mutableStateOf(membershipToEdit?.category ?: "Fashion")
    }
    var portalUrl by remember(membershipToEdit) {
        mutableStateOf(membershipToEdit?.portalUrl ?: "")
    }
    var instructions by remember(membershipToEdit) {
        mutableStateOf(membershipToEdit?.skipInstructions ?: "")
    }
    var colorHex by remember(membershipToEdit) {
        mutableLongStateOf(membershipToEdit?.colorHex ?: 0xFF6366F1)
    }

    var remindEve by remember(membershipToEdit) {
        mutableStateOf(membershipToEdit?.remindEve ?: true)
    }
    var remindDay1 by remember(membershipToEdit) {
        mutableStateOf(membershipToEdit?.remindDay1 ?: true)
    }
    var remindDayLast by remember(membershipToEdit) {
        mutableStateOf(membershipToEdit?.remindDayLast ?: true)
    }

    val availableColors = listOf(
        0xFFE11D48, // Fabletics Rose
        0xFF8B5CF6, // Savage X Purple
        0xFFEC4899, // JustFab Pink
        0xFFF97316, // ShoeDazzle Orange
        0xFF06B6D4, // AdoreMe Cyan
        0xFF0284C7, // BOTM Sky
        0xFF78716C, // Bespoke Gray
        0xFF14B8A6, // BarkBox Teal
        0xFF6366F1  // Indigo
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (membershipToEdit == null) "Add VIP Membership" else "Edit ${membershipToEdit.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Membership Name") },
                placeholder = { Text("e.g. Fabletics, Savage X, Book of the Month") },
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_membership_name"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cost & Currency
            OutlinedTextField(
                value = costText,
                onValueChange = { costText = it },
                label = { Text("Monthly Auto-Charge Fee ($)") },
                placeholder = { Text("69.95") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_monthly_cost"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Skip Window Days (Start Day to End Day)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = startDayText,
                    onValueChange = { startDayText = it },
                    label = { Text("Start Day") },
                    placeholder = { Text("1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_start_day"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = endDayText,
                    onValueChange = { endDayText = it },
                    label = { Text("Deadline Day") },
                    placeholder = { Text("5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_end_day"),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Portal Web URL
            OutlinedTextField(
                value = portalUrl,
                onValueChange = { portalUrl = it },
                label = { Text("Website Skip / Login Portal URL") },
                placeholder = { Text("https://www.brand.com/account") },
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_portal_url"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val categories = listOf("Fashion", "Activewear", "Lingerie", "Shoes", "Books", "Lifestyle", "Pets", "Other")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = category.equals(cat, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { category = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Brand Color Picker
            Text(
                text = "Accent Color",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                availableColors.forEach { colorVal ->
                    val isSelected = colorHex == colorVal
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(colorVal))
                            .clickable { colorHex = colorVal },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reminders
            Text(
                text = "Reminders",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { remindEve = !remindEve }
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(checked = remindEve, onCheckedChange = { remindEve = it })
                Text(
                    text = "Eve Reminder (Last day of month at 8:00 PM)",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { remindDay1 = !remindDay1 }
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(checked = remindDay1, onCheckedChange = { remindDay1 = it })
                Text(
                    text = "Opening Day Reminder (1st of month at 9:00 AM)",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { remindDayLast = !remindDayLast }
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(checked = remindDayLast, onCheckedChange = { remindDayLast = it })
                Text(
                    text = "Urgent Final Day Alerts (9:00 AM & 6:00 PM)",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    val parsedCost = costText.toDoubleOrNull() ?: 69.95
                    val parsedStart = (startDayText.toIntOrNull() ?: 1).coerceIn(1, 28)
                    val parsedEnd = (endDayText.toIntOrNull() ?: 5).coerceIn(parsedStart, 31)

                    val membership = VipMembership(
                        id = membershipToEdit?.id ?: 0L,
                        name = name.ifBlank { "VIP Club" },
                        monthlyCost = parsedCost,
                        currency = "$",
                        windowStartDay = parsedStart,
                        windowEndDay = parsedEnd,
                        category = category,
                        portalUrl = portalUrl,
                        skipInstructions = instructions.ifBlank {
                            "Log in between the ${parsedStart}st and ${parsedEnd}th to click 'Skip the Month'."
                        },
                        colorHex = colorHex,
                        remindEve = remindEve,
                        remindDay1 = remindDay1,
                        remindDayLast = remindDayLast
                    )
                    onSave(membership)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_membership_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (membershipToEdit == null) "Save VIP Membership" else "Update Changes",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
