package com.salesflo.snackflo

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDemo(onDismiss: () -> Unit) {

    var showDatePicker by remember { mutableStateOf(false) } // 🎯 Toggle dialog visibility
    var selectedDate by remember { mutableStateOf("") } // 📆 Store selected date
    var showError by remember { mutableStateOf(false) } // ⚠️ Show error for invalid dates


    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        DatePickerCard(
            title = "Future Only", // 📌 Card title
            description = "Select dates from today onward.", // ℹ️ Card description
            date = selectedDate, // 📅 Selected date
            buttonText = "Select Date", // 🖱️ Button text
            onClick = { showDatePicker = true } // 🔄 Show dialog on click
        )
    }


    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = Instant.fromEpochMilliseconds(utcTimeMillis)
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date
                    return validateDate(date, RestrictionType.NoPastDates) // ✅ Restrict past dates
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false }, // 🚪 Close on dismiss
            confirmButton = {

                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val date = Instant.fromEpochMilliseconds(selectedMillis)
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date
                            if (!validateDate(date, RestrictionType.NoPastDates)) {
                                showError = true
                            } else {
                                selectedDate = date.toString()
                                showDatePicker = false
                                showError = false
                            }
                        }
                    }
                ) {
                    Text("OK", color = Color(0xFF3F51B5))
                }
            },
            dismissButton = {

                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Color(0xFF3F51B5))
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = Color(0xFF3F51B5)
            )
        ) {

            Column {

                DatePicker(
                    state = datePickerState,
                    headline = {
                        Text(
                            text = "Future Only",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF3F51B5),
                            modifier = Modifier.padding(16.dp)
                        )
                    },
                    modifier = Modifier
                )

                if (showError) {
                    Text(
                        text = "Selected date is not allowed. Please choose a valid date.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun DatePickerCard(
    title: String,
    description: String,
    date: String,
    buttonText: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth() // 📏 Full width
            .clip(RoundedCornerShape(16.dp)) // 🔲 Rounded corners
            .border(
                1.dp,
                Color.Gray.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            ), // 🖌️ Light gray border
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // 🖼️ Surface background
    ) {
        Column(Modifier.padding(16.dp)) { // 🛑 Padding inside card
            // 📌 Step 15: Add Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), // 📏 Bold large text
                color = MaterialTheme.colorScheme.onSurface // 🎨 Surface text color
            )
            Spacer(Modifier.height(8.dp)) // 📐 Space
            // ℹ️ Step 16: Add Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium, // 📏 Medium text
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) // 🎨 Slightly transparent
            )
            Spacer(Modifier.height(12.dp)) // 📐 Space
            // 🖱️ Step 17: Add Button
            Button(
                onClick = onClick, // 🔄 Trigger onClick
                modifier = Modifier.align(Alignment.End), // 📍 Align right
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary, // 🎨 Primary color
                    contentColor = MaterialTheme.colorScheme.onPrimary // 🎨 On-primary text
                ),
                shape = RoundedCornerShape(12.dp) // 🔲 Rounded button
            ) {
                Text(buttonText, fontSize = 14.sp) // 📏 Button text
            }
            // 📅 Step 18: Show Selected Date
            if (date.isNotEmpty()) {
                Spacer(Modifier.height(8.dp)) // 📐 Space
                Text(
                    text = "Selected: $date", // 📅 Display date
                    style = MaterialTheme.typography.bodyLarge, // 📏 Large text
                    color = MaterialTheme.colorScheme.onSurface // 🎨 Surface text color
                )
            }
        }
    }
}

// 🔧 Step 19: Date Validation Logic 🔧
private fun validateDate(date: LocalDate, restrictionType: RestrictionType): Boolean {
    // ⏰ Step 20: Get Current Date
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    // ✅ Step 21: Validate Based on Restriction
    return when (restrictionType) {
        RestrictionType.NoPastDates -> date >= today // 🛑 No past dates
        else -> true // ✅ Allow all for None
    }
}

// 🎨 Step 22: Enum for Restriction Types 🎨
enum class RestrictionType {
    None, NoPastDates
}