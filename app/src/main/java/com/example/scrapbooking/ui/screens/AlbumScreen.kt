package com.example.scrapbooking.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.scrapbooking.R
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    onDayClick: (LocalDate) -> Unit = {}
) {
    var isMonthView by remember { mutableStateOf(true) }
    
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
    
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek) }
    
    // Mock data: map of LocalDate to list of photo URLs
    val mockPhotos = remember {
        val today = LocalDate.now()
        mapOf(
            today to listOf("https://picsum.photos/seed/1/200/200", "https://picsum.photos/seed/2/200/200"),
            today.minusDays(2) to listOf("https://picsum.photos/seed/3/200/200"),
            today.minusDays(5) to listOf("https://picsum.photos/seed/4/200/200", "https://picsum.photos/seed/5/200/200", "https://picsum.photos/seed/6/200/200"),
            today.minusDays(10) to listOf("https://picsum.photos/seed/7/200/200")
        )
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
                                photos = mockPhotos[day.date] ?: emptyList(),
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
                                photos = mockPhotos[day.date] ?: emptyList(),
                                onClick = { onDayClick(day.date) }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MonthHeader(
    currentYearMonth: YearMonth,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onHeaderClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
        }
        Text(
            text = currentYearMonth.format(DateTimeFormatter.ofPattern("MM/yyyy")),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onHeaderClick() }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        IconButton(onClick = onNextClick) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next")
        }
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.name.take(3),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DayContent(
    date: LocalDate,
    isCurrentMonth: Boolean,
    photos: List<String>,
    onClick: () -> Unit
) {
    val isToday = date == LocalDate.now()
    
    Box(
        modifier = Modifier
            .aspectRatio(1f) // Đảm bảo cell là hình vuông
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isToday) MaterialTheme.colorScheme.primaryContainer 
                else if (isCurrentMonth) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else Color.Transparent
            )
            .clickable(enabled = isCurrentMonth) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Nếu không thuộc tháng đang xét thì làm mờ text, không hiển thị gì thêm
        if (!isCurrentMonth) {
            Text(
                text = date.dayOfMonth.toString(),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                fontSize = 14.sp
            )
            return@Box
        }

        // Hiển thị ảnh đầu tiên làm nền của cell
        if (photos.isNotEmpty()) {
            AsyncImage(
                model = photos.first(),
                contentDescription = "Day Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Nếu có nhiều ảnh, hiển thị badge số lượng ở góc trên bên trái
            if (photos.size > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(2.dp)
                        .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+${photos.size - 1}",
                        color = MaterialTheme.colorScheme.onError,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 3.dp).wrapContentHeight(Alignment.CenterVertically)
                    )
                }
            }
        } 
        
        // Văn bản ngày ở góc dưới bên phải
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(2.dp)
                .background(
                    if (photos.isNotEmpty()) Color.Black.copy(alpha = 0.5f) else Color.Transparent, 
                    CircleShape
                )
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = if (photos.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}