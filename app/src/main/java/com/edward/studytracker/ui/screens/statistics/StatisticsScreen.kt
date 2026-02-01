package com.edward.studytracker.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.edward.studytracker.data.PracticeRecord
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    totalPracticeCount: Int,
    correctPracticeCount: Int,
    practiceRecords: List<PracticeRecord>,
    onNavigateBack: () -> Unit = {}
) {
    var currentYearMonth by remember { mutableStateOf(Calendar.getInstance()) }
    
    val totalCount = totalPracticeCount
    val correctRate = if (totalCount > 0) {
        (correctPracticeCount.toFloat() / totalCount * 100).toInt()
    } else 0
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Statistics Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "总练习",
                    value = "$totalCount",
                    unit = "次",
                    modifier = Modifier.weight(1f),
                    valueColor = Color(0xFF2196F3)
                )
                StatCard(
                    title = "正确率",
                    value = "$correctRate%",
                    unit = "平均",
                    modifier = Modifier.weight(1f),
                    valueColor = Color(0xFF4CAF50)
                )
            }
            
            // Calendar
            CalendarView(
                currentYearMonth = currentYearMonth,
                practiceRecords = practiceRecords,
                onPreviousMonth = {
                    currentYearMonth = (currentYearMonth.clone() as Calendar).apply {
                        add(Calendar.MONTH, -1)
                    }
                },
                onNextMonth = {
                    currentYearMonth = (currentYearMonth.clone() as Calendar).apply {
                        add(Calendar.MONTH, 1)
                    }
                }
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier.height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                    fontSize = 36.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun CalendarView(
    currentYearMonth: Calendar,
    practiceRecords: List<PracticeRecord>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val year = currentYearMonth.get(Calendar.YEAR)
    val month = currentYearMonth.get(Calendar.MONTH)
    
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    
    // Calculate records per day
    val recordsByDay = remember(practiceRecords, year, month) {
        val result = mutableMapOf<Int, Int>()
        practiceRecords.forEach { record ->
            val recordCal = Calendar.getInstance().apply { timeInMillis = record.timestamp }
            if (recordCal.get(Calendar.YEAR) == year && recordCal.get(Calendar.MONTH) == month) {
                val day = recordCal.get(Calendar.DAY_OF_MONTH)
                result[day] = (result[day] ?: 0) + 1
            }
        }
        result
    }
    
    val maxCount = recordsByDay.values.maxOrNull() ?: 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month"
                    )
                }
                Text(
                    text = "${year}年 ${month + 1}月",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNextMonth) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Weekday headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calendar grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(280.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Empty cells for days before the first day of month
                items((firstDayOfWeek - 1)) {
                    Box(modifier = Modifier.size(40.dp))
                }
                
                // Days of month
                items(daysInMonth) { index ->
                    val day = index + 1
                    val count = recordsByDay[day] ?: 0
                    val intensity = if (maxCount > 0) count.toFloat() / maxCount else 0f
                    
                    DayCell(
                        day = day,
                        count = count,
                        intensity = intensity
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "少",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                
                val legendColors = listOf(
                    Color(0xFFE0E0E0),
                    Color(0xFFC8E6C9),
                    Color(0xFF81C784),
                    Color(0xFF4CAF50),
                    Color(0xFF2E7D32)
                )
                
                legendColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "多",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    count: Int,
    intensity: Float
) {
    val backgroundColor = when {
        count == 0 -> Color(0xFFF5F5F5)
        intensity < 0.25f -> Color(0xFFC8E6C9)
        intensity < 0.5f -> Color(0xFF81C784)
        intensity < 0.75f -> Color(0xFF4CAF50)
        else -> Color(0xFF2E7D32)
    }
    
    val textColor = if (count == 0) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        Color.White
    }
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$day",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal
        )
    }
}