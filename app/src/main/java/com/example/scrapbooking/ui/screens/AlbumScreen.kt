package com.example.scrapbooking.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrapbooking.R
import com.example.scrapbooking.ui.components.DayContent
import com.example.scrapbooking.ui.components.DaysOfWeekTitle
import com.example.scrapbooking.ui.components.MonthHeader
import com.example.scrapbooking.viewmodel.AlbumViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    viewModel: AlbumViewModel = hiltViewModel(),
    onDayClick: (LocalDate) -> Unit = {}
) {
    var isMonthView by remember { mutableStateOf(true) }
    
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
    
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek) }
    
    val stampImages by viewModel.stampImages.collectAsStateWithLifecycle()
    // Group stampImages theo ngày (giả sử path hoặc lastModified có thể convert ra LocalDate)
    val photosByDate = remember(stampImages) {
        stampImages.groupBy {
            java.time.Instant.ofEpochMilli(it.lastModified)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    
    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )
    
    val weekCalendarState = rememberWeekCalendarState(
        startDate = startMonth.atDay(1),
        endDate = endMonth.atEndOfMonth(),
        firstVisibleWeekDate = currentMonth.atDay(1),
        firstDayOfWeek = firstDayOfWeek
    )

    // Lấy tháng đang hiển thị để làm tiêu đề
    val visibleMonth = calendarState.firstVisibleMonth.yearMonth
    val visibleWeekDate = weekCalendarState.firstVisibleWeek.days.firstOrNull()?.date ?: LocalDate.now()
    val visibleWeekMonth = YearMonth.from(visibleWeekDate)
    val currentYearMonth = if (isMonthView) visibleMonth else visibleWeekMonth
    
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        var tempYear by remember { mutableStateOf(currentYearMonth.year) }
        var tempMonth by remember { mutableStateOf(currentYearMonth.monthValue) }

        val months = listOf(
            "Thg 1", "Thg 2", "Thg 3",
            "Thg 4", "Thg 5", "Thg 6",
            "Thg 7", "Thg 8", "Thg 9",
            "Thg 10", "Thg 11", "Thg 12"
        )

        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { tempYear-- }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Năm trước")
                    }
                    Text(
                        text = tempYear.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { tempYear++ }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Năm sau")
                    }
                }
            },
            text = {
                Column {
                    for (row in 0..3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (col in 0..2) {
                                val monthIndex = row * 3 + col
                                val isSelected = tempMonth == monthIndex + 1
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                        .clickable { tempMonth = monthIndex + 1 }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = months[monthIndex],
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        val newYearMonth = YearMonth.of(tempYear, tempMonth)
                        if (isMonthView) {
                            calendarState.animateScrollToMonth(newYearMonth)
                        } else {
                            weekCalendarState.animateScrollToWeek(newYearMonth.atDay(1))
                        }
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Hủy") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_album_screen), fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            MonthHeader(
                currentYearMonth = currentYearMonth,
                onPreviousClick = {
                    coroutineScope.launch {
                        if (isMonthView) {
                            calendarState.animateScrollToMonth(calendarState.firstVisibleMonth.yearMonth.minusMonths(1))
                        } else {
                            val prevWeekFirstDay = weekCalendarState.firstVisibleWeek.days.first().date.minusWeeks(1)
                            weekCalendarState.animateScrollToWeek(prevWeekFirstDay)
                        }
                    }
                },
                onNextClick = {
                    coroutineScope.launch {
                        if (isMonthView) {
                            calendarState.animateScrollToMonth(calendarState.firstVisibleMonth.yearMonth.plusMonths(1))
                        } else {
                            val nextWeekFirstDay = weekCalendarState.firstVisibleWeek.days.first().date.plusWeeks(1)
                            weekCalendarState.animateScrollToWeek(nextWeekFirstDay)
                        }
                    }
                },
                onHeaderClick = {
                    showDatePicker = true
                }
            )
            
            DaysOfWeekTitle(daysOfWeek = daysOfWeek)
            
            Crossfade(targetState = isMonthView, label = "CalendarType") { monthView ->
                if (monthView) {
                    HorizontalCalendar(
                        state = calendarState,
                        dayContent = { day ->
                            DayContent(
                                date = day.date,
                                isCurrentMonth = day.position == DayPosition.MonthDate,
                                photos = photosByDate[day.date]?.map { it.path } ?: emptyList(),
                                onClick = { if (day.position == DayPosition.MonthDate) onDayClick(day.date) }
                            )
                        }
                    )
                } else {
                    WeekCalendar(
                        state = weekCalendarState,
                        dayContent = { day ->
                            DayContent(
                                date = day.date,
                                isCurrentMonth = true,
                                photos = photosByDate[day.date]?.map { it.path } ?: emptyList(),
                                onClick = { onDayClick(day.date) }
                            )
                        }
                    )
                }
            }
        }
    }
}

