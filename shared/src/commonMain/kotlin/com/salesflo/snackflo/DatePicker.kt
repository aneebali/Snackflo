package com.salesflo.snackflo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDemo(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    if (!showDialog) return

    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val todayMillis = today.atTime(0, 0).toInstant(TimeZone.UTC).toEpochMilliseconds()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = todayMillis
    )

    DatePickerDialog(

        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    val selectedDate = selectedMillis?.let {
                        // Convert back from UTC
                        Instant.fromEpochMilliseconds(it)
                            .toLocalDateTime(TimeZone.UTC).date
                    } ?: today

                    onDateSelected(selectedDate)
                    onDismiss()
                }
            ) {
                Text("OK", color = Color(0xFFFF7F50), fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray , fontWeight = FontWeight.SemiBold)
            }
        }
    ) {
        Column(modifier = Modifier.background(Color.White)) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    headlineContentColor = Color.Black,
                    weekdayContentColor = Color.Black,
                    subheadContentColor = Color.Black,
                    dayContentColor = Color.Black,
                    disabledDayContentColor = Color.Gray,
                    selectedDayContainerColor = Color(0xFFFF7F50),
                    selectedDayContentColor = Color.White,
                    todayContentColor = Color.Black,
                    todayDateBorderColor = Color(0xFFFF7F50)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}