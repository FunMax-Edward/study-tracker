package com.edward.studytracker.ui.theme

import androidx.compose.ui.graphics.Color

// 题目熟练度颜色 - 与 Material You 动态主题协调的柔和配色
object StatusColors {
    // 未做 - 使用 surfaceVariant 级别的中性色
    val Unsolved = Color(0xFFE7E0EC)
    
    // 红色系 - 从浅到深，柔和不刺眼
    val LightRed = Color(0xFFF5D6D6)      // 很浅的红色
    val MediumRed = Color(0xFFE8B4B4)     // 浅红色
    val DarkRed = Color(0xFFD48B8B)       // 中红色
    val DeepestRed = Color(0xFFC06262)    // 深红色
    
    // 绿色系 - 柔和的绿色
    val LightGreen = Color(0xFFC8E6C9)    // 浅绿色
    val DarkGreen = Color(0xFF81C784)     // 深绿色
}

fun getProblemColor(proficiencyLevel: Int): Color {
    return when (proficiencyLevel) {
        6 -> StatusColors.DarkGreen
        5 -> StatusColors.LightGreen
        4 -> StatusColors.DeepestRed
        3 -> StatusColors.DarkRed
        2 -> StatusColors.MediumRed
        1 -> StatusColors.LightRed
        else -> StatusColors.Unsolved
    }
}
