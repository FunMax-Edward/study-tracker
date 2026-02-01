package com.edward.studytracker.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edward.studytracker.data.PracticeRecord
import com.edward.studytracker.data.ProjectRepository
import com.edward.studytracker.data.StudyUnit
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    projectName: String,
    units: List<StudyUnit>,
    projectId: Int,
    repository: ProjectRepository,
    onNavigateBack: () -> Unit
) {
    val totalProblems = units.sumOf { it.problemCount }
    val totalUnits = units.size
    
    // 获取所有练习记录
    val allRecords by repository.getAllPracticeRecords().collectAsState(initial = emptyList())
    
    // 计算真实的连续学习天数
    val streakDays = remember(allRecords) {
        calculateStreakDays(allRecords)
    }
    
    // 当前显示的月份
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "统计",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 项目概览卡片
            ProjectOverviewCard(
                projectName = projectName,
                totalUnits = totalUnits,
                totalProblems = totalProblems
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 学习热力图（按月展示，可切换）
            MonthlyHeatmapSection(
                title = "学习热力图",
                yearMonth = currentYearMonth,
                records = allRecords,
                onPreviousMonth = { currentYearMonth = currentYearMonth.minusMonths(1) },
                onNextMonth = { currentYearMonth = currentYearMonth.plusMonths(1) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 连续学习天数（真实数据）
            StreakCard(streakDays = streakDays)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 单元列表
            UnitsListSection(units = units)
        }
    }
}

@Composable
private fun ProjectOverviewCard(
    projectName: String,
    totalUnits: Int,
    totalProblems: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = projectName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = "$totalUnits",
                    label = "单元数"
                )
                StatItem(
                    value = "$totalProblems",
                    label = "总题数"
                )
                StatItem(
                    value = "${totalProblems / 10}",
                    label = "已做题数"
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MonthlyHeatmapSection(
    title: String,
    yearMonth: YearMonth,
    records: List<PracticeRecord>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Column {
        // 标题和月份导航
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            // 月份切换
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "上个月",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = yearMonth.format(DateTimeFormatter.ofPattern("yyyy年 M月", Locale.CHINESE)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "下个月",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 星期标签
        val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 日历网格
        val firstDayOfMonth = yearMonth.atDay(1)
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 周日=0, 周一=1, ...
        
        // 计算总格子数（包括空白格）
        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7 // 向上取整
        
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val dayOfMonth = cellIndex - firstDayOfWeek + 1
                        
                        if (cellIndex >= firstDayOfWeek && dayOfMonth <= daysInMonth) {
                            val date = yearMonth.atDay(dayOfMonth)
                            val count = getPracticeCountForDate(records, date)
                            val intensity = when {
                                count == 0 -> 0
                                count <= 5 -> 1
                                count <= 10 -> 2
                                count <= 20 -> 3
                                else -> 4
                            }
                            
                            MonthlyHeatmapCell(
                                day = dayOfMonth,
                                intensity = intensity,
                                count = count
                            )
                        } else {
                            // 空白格子
                            Box(modifier = Modifier.size(36.dp))
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 图例
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "少",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            // 图例方块
            (0..4).forEach { level ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(getHeatmapColor(level))
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "多",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MonthlyHeatmapCell(
    day: Int,
    intensity: Int,
    count: Int
) {
    var showTooltip by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(getHeatmapColor(intensity))
            .clickable { showTooltip = !showTooltip },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$day",
            style = MaterialTheme.typography.bodySmall,
            color = if (intensity > 2) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
        
        // 简单的计数提示
        if (showTooltip && count > 0) {
            androidx.compose.ui.window.Popup(
                alignment = Alignment.TopCenter,
                onDismissRequest = { showTooltip = false }
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "$count 题",
                        modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun getHeatmapColor(intensity: Int): Color {
    val baseColor = MaterialTheme.colorScheme.primary
    return when (intensity) {
        0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        1 -> baseColor.copy(alpha = 0.2f)
        2 -> baseColor.copy(alpha = 0.4f)
        3 -> baseColor.copy(alpha = 0.6f)
        4 -> baseColor.copy(alpha = 0.8f)
        else -> baseColor
    }
}

@Composable
private fun StreakCard(streakDays: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "连续学习 $streakDays 天",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "继续保持！",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UnitsListSection(units: List<StudyUnit>) {
    Column {
        Text(
            text = "单元详情",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        units.forEach { unit ->
            UnitStatItem(unit = unit)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun UnitStatItem(unit: StudyUnit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = unit.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${unit.problemCount} 题",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 进度指示
            val progress = (0..unit.problemCount).random() // 模拟进度
            val progressPercent = if (unit.problemCount > 0) {
                (progress.toFloat() / unit.problemCount * 100).toInt()
            } else 0
            
            Text(
                text = "$progressPercent%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// 计算连续学习天数
private fun calculateStreakDays(records: List<PracticeRecord>): Int {
    if (records.isEmpty()) return 0
    
    val practiceDates = records
        .map { LocalDate.ofEpochDay(it.timestamp / (1000 * 60 * 60 * 24)) }
        .distinct()
        .sortedDescending()
    
    if (practiceDates.isEmpty()) return 0
    
    val today = LocalDate.now()
    var streak = 0
    var checkDate = today
    
    // 如果今天没有学习，从昨天开始算
    if (!practiceDates.contains(today)) {
        checkDate = today.minusDays(1)
    }
    
    for (date in practiceDates) {
        if (date == checkDate || date == checkDate.minusDays(streak.toLong())) {
            streak++
        } else if (date.isBefore(checkDate.minusDays(streak.toLong()))) {
            break
        }
    }
    
    return streak
}

// 获取某一天的练习次数
private fun getPracticeCountForDate(records: List<PracticeRecord>, date: LocalDate): Int {
    val startOfDay = date.toEpochDay() * (1000 * 60 * 60 * 24)
    val endOfDay = startOfDay + (1000 * 60 * 60 * 24)
    
    return records.count { it.timestamp in startOfDay until endOfDay }
}

// 热力图数据类
data class HeatmapDay(
    val date: LocalDate,
    val intensity: Int, // 0-4 表示做题数量级别
    val count: Int // 实际做题数
)
