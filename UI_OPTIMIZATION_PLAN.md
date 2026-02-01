# Study Tracker App - UI/UX 优化方案

## 一、设计灵感来源

### 1. 官方参考
- **Material Design 3**: https://m3.material.io/
- **Material 3 Compose**: https://developer.android.com/develop/ui/compose/designsystems/material3
- **Google Codelabs - Theming**: https://codelabs.developers.google.com/jetpack-compose-theming
- **Compose Samples**: https://github.com/android/compose-samples

### 2. 优秀Flashcard App设计
- **Dribbble Flashcards**: https://dribbble.com/tags/flashcards
- **Anki Mobile Design**: 简洁的卡片设计，清晰的进度反馈
- **Study App Template**: https://banani.co/templates/mobile/study

### 3. 动画与交互参考
- **Compose Animation Guide**: https://developer.android.com/develop/ui/compose/animation/quick-guide
- **Shimmer Effect**: 骨架屏加载效果
- **Material Motion**: 页面切换动画 (slide, fade, shared axis)

---

## 二、当前App UI问题分析

### ❌ 问题清单

#### 1. 主题配色
- [ ] 使用默认的Purple40配色，未自定义
- [ ] 缺乏品牌个性
- [ ] 未适配深色模式

#### 2. 卡片设计
- [ ] 固定圆角12dp，缺乏层次
- [ ] 阴影单一（仅2dp）
- [ ] 点击反馈不足（缺少ripple效果）

#### 3. 动画缺失
- [ ] 页面切换无过渡动画
- [ ] 列表项无进入动画
- [ ] 对话框弹出生硬
- [ ] FAB无缩放反馈

#### 4. 间距与排版
- [ ] 间距未统一（用了很多 magic numbers）
- [ ] 字体大小未使用Material 3 type scale
- [ ] 图例区域太小太拥挤

#### 5. 空状态
- [ ] 空项目/空单元提示太简单
- [ ] 缺少插图或视觉引导

#### 6. 交互反馈
- [ ] 删除操作无确认动画
- [ ] 项目切换无视觉反馈
- [ ] 题目状态变化无过渡

---

## 三、优化方案

### 1. 主题系统重构

#### 目标配色方案（清新、专业）
```kotlin
// 建议的Material 3配色
private val StudyTrackerColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),      // 品牌紫
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    
    secondary = Color(0xFF625B71),    // 柔和灰紫
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    
    tertiary = Color(0xFF7D5260),     // 强调色
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFF9DEDC),
    onError = Color.White,
    onErrorContainer = Color(0xFF410E0B),
    
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)
```

#### Typography优化
```kotlin
// 使用Material 3 type scale
val Typography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
)
```

---

### 2. 卡片增强设计

#### ProblemCell 优化
```kotlin
@Composable
fun ProblemCell(
    problem: Problem,
    onClick: () -> Unit
) {
    val bgColor = getProblemColor(problem.proficiencyLevel)
    val textColor = if (problem.proficiencyLevel == 0) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        Color.White
    }
    
    // 添加点击缩放动画
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.changes.first().pressState) {
                            is PressInteraction.Press -> pressed = true
                            is PressInteraction.Release -> pressed = false
                            is PressInteraction.Cancel -> pressed = false
                        }
                    }
                }
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),  // 增大圆角
        colors = CardDefaults.elevatedCardColors(
            containerColor = bgColor
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (pressed) 1.dp else 4.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${problem.problemIndex}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
```

---

### 3. 动画系统

#### 页面切换动画 (MainActivity.kt)
```kotlin
// 使用 AnimatedContent 或 NavHost transition
NavHost(
    navController = navController,
    startDestination = Screen.Home.route,
    enterTransition = {
        slideInHorizontally(
            initialOffsetX = { 300 },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300))
    },
    exitTransition = {
        slideOutHorizontally(
            targetOffsetX = { -300 },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
    },
    popEnterTransition = {
        slideInHorizontally(
            initialOffsetX = { -300 },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300))
    },
    popExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { 300 },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
    }
) { ... }
```

#### 列表项进入动画
```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(4),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    items(
        items = problems,
        key = { it.id }
    ) { problem ->
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            exit = fadeOut()
        ) {
            ProblemCell(problem = problem, onClick = { ... })
        }
    }
}
```

---

### 4. FAB增强

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUnitFab(
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    LargeFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.large,
        elevation = FloatingActionButtonDefaults.largeFloatingActionButtonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp
        )
    ) {
        AnimatedVisibility(visible = true) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add unit"
            )
        }
    }
}
```

---

### 5. 图例区域优化

```kotlin
@Composable
fun LegendSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "熟练度",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProficiencyLegendItem(level = 0, label = "未做")
                ProficiencyLegendItem(level = 1, label = "浅红")
                ProficiencyLegendItem(level = 2, label = "中红")
                ProficiencyLegendItem(level = 3, label = "深红")
                ProficiencyLegendItem(level = 4, label = "最深")
                ProficiencyLegendItem(level = 5, label = "浅绿")
                ProficiencyLegendItem(level = 6, label = "深绿")
            }
        }
    }
}

@Composable
private fun ProficiencyLegendItem(level: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(getProblemColor(level))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

---

### 6. 对话框美化

```kotlin
@Composable
fun ProblemActionDialog(
    problemNumber: Int,
    onDismiss: () -> Unit,
    onRecordCorrect: () -> Unit,
    onRecordWrong: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),  // 大圆角
        title = {
            Text(
                text = "第 $problemNumber 题",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = "这次的结果是：",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            Button(
                onClick = onRecordCorrect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("正确")
            }
        },
        dismissButton = {
            TextButton(onClick = onRecordWrong) {
                Text(
                    "错误",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}
```

---

### 7. 空状态设计

```kotlin
@Composable
fun EmptyUnitState(
    onAddUnit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 简洁的插图（可用图标或自定义绘图）
        Icon(
            imageVector = Icons.Default.Book,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "还没有单元",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击下方按钮添加第一个学习单元",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        FilledTonalButton(
            onClick = onAddUnit,
            modifier = Modifier.height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("添加单元")
        }
    }
}
```

---

### 8. 项目选择器增强

```kotlin
@Composable
fun ProjectSelectorSheet(
    projects: List<Project>,
    currentProject: Project,
    onProjectSelect: (Project) -> Unit,
    onCreateProject: () -> Unit,
    onProjectRename: (Project) -> Unit,
    onDeleteProject: (Project) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { /* dismiss */ },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "选择项目",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            projects.forEach { project ->
                ProjectListItem(
                    project = project,
                    isSelected = project.id == currentProject.id,
                    onClick = { onProjectSelect(project) },
                    onRename = { onProjectRename(project) },
                    onDelete = { onDeleteProject(project) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onCreateProject,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("新建项目")
            }
        }
    }
}

@Composable
private fun ProjectListItem(
    project: Project,
    isSelected: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            Row {
                IconButton(onClick = onRename) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "重命名",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
```

---

### 9. 深色模式支持

```kotlin
@Composable
fun StudyTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Purple80,
            onPrimary = Purple40,
            primaryContainer = Purple40,
            onPrimaryContainer = Purple80,
            // ... 其他深色配色
        )
    } else {
        LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

---

## 四、优先级实施计划

### 🔴 高优先级（P0）
1. ✅ 主题系统重构（自定义配色）
2. ✅ 卡片设计优化（圆角、阴影、点击反馈）
3. ✅ 图例区域美化
4. ✅ 对话框圆角和样式

### 🟡 中优先级（P1）
1. 页面切换动画
2. FAB增强
3. 深色模式支持
4. 空状态设计

### 🟢 低优先级（P2）
1. 列表项进入动画
2. 项目选择器BottomSheet
3. 骨架屏加载效果

---

## 五、参考资源汇总

### 官方文档
- Material 3: https://m3.material.io/
- Compose Animation: https://developer.android.com/develop/ui/compose/animation
- Compose Samples: https://github.com/android/compose-samples

### 设计灵感
- Dribbble Flashcards: https://dribbble.com/tags/flashcards
- Study App Template: https://banani.co/templates/mobile/study

### 教程
- Shimmer Effect: https://medium.com/@suwasto.anang/easy-shimmer-loading-for-jetpack-compose-kmm-9f0273db40d7
- Animation Best Practices: https://proandroiddev.com/top-3-most-common-animations-you-can-use-in-your-jetpack-compose-project-9bb92f5311a2
