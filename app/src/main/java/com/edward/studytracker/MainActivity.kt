package com.edward.studytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.edward.studytracker.data.AppDatabase
import com.edward.studytracker.data.PreferencesManager
import com.edward.studytracker.data.Project
import com.edward.studytracker.data.ProjectRepository
import com.edward.studytracker.ui.screens.home.HomeScreen
import com.edward.studytracker.ui.screens.settings.SettingsScreen
import com.edward.studytracker.ui.screens.stats.StatsScreen
import com.edward.studytracker.ui.screens.unitdetail.UnitDetailScreen
import com.edward.studytracker.ui.theme.StudyTrackerTheme
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object UnitDetail : Screen("unit/{unitId}/{unitName}/{projectId}") {
        fun createRoute(unitId: Int, unitName: String, projectId: Int) =
            "unit/$unitId/${java.net.URLEncoder.encode(unitName, "UTF-8")}/$projectId"
    }
    data object Settings : Screen("settings")
    data object Stats : Screen("stats/{projectId}") {
        fun createRoute(projectId: Int) = "stats/$projectId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { ProjectRepository(database) }
    val prefs = remember { PreferencesManager(context) }

    val projects by repository.getAllProjects().collectAsState(initial = emptyList())
    var currentProject by remember { mutableStateOf<Project?>(null) }

    LaunchedEffect(projects) {
        val savedId = prefs.currentProjectId
        currentProject = if (savedId != -1) {
            projects.find { it.id == savedId }
        } else {
            projects.firstOrNull()
        }

        if (currentProject == null && projects.isNotEmpty()) {
            currentProject = projects.first()
            prefs.currentProjectId = currentProject!!.id
        }
    }

    val currentProjectId = currentProject?.id ?: -1
    val units by repository.getUnitsForProject(currentProjectId).collectAsState(initial = emptyList())

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
    ) {
        composable(Screen.Home.route) {
            if (projects.isEmpty()) {
                EmptyProjectScreen(
                    onCreateProject = { name ->
                        scope.launch {
                            val id = repository.createProject(name)
                            val newProject = projects.find { it.id == id.toInt() }
                            if (newProject != null) {
                                prefs.currentProjectId = newProject.id
                                currentProject = newProject
                            }
                        }
                    }
                )
            } else {
                currentProject?.let { project ->
                    HomeScreen(
                        currentProject = project,
                        projects = projects,
                        units = units,
                        onProjectClick = { },
                        onProjectRename = { p, name ->
                            scope.launch {
                                val updated = p.copy(name = name)
                                database.projectDao().updateProject(updated)
                            }
                        },
                        onCreateProject = { name ->
                            scope.launch {
                                val id = repository.createProject(name)
                                val newProject = projects.find { it.id == id.toInt() }
                                if (newProject != null) {
                                    prefs.currentProjectId = newProject.id
                                    currentProject = newProject
                                }
                            }
                        },
                        onDeleteProject = { id ->
                            scope.launch {
                                repository.deleteProject(id)
                                if (currentProject?.id == id) {
                                    val remaining = projects.filter { it.id != id }
                                    if (remaining.isNotEmpty()) {
                                        val next = remaining.first()
                                        prefs.currentProjectId = next.id
                                        currentProject = next
                                    } else {
                                        currentProject = null
                                        prefs.currentProjectId = -1
                                    }
                                }
                            }
                        },
                        onCreateUnit = { name, count ->
                            scope.launch {
                                repository.createUnit(currentProjectId, name, count)
                            }
                        },
                        onDeleteUnit = { unitId ->
                            scope.launch {
                                repository.deleteUnit(unitId)
                            }
                        },
                        onUnitClick = { unit ->
                            navController.navigate(Screen.UnitDetail.createRoute(unit.id, unit.name, currentProjectId))
                        },
                        onSwitchProject = { project ->
                            prefs.currentProjectId = project.id
                            currentProject = project
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route)
                        },
                        onNavigateToStats = {
                            navController.navigate(Screen.Stats.createRoute(currentProjectId))
                        }
                    )
                }
            }
        }

        composable(
            route = Screen.UnitDetail.route,
            arguments = listOf(
                navArgument("unitId") { type = NavType.IntType },
                navArgument("unitName") { type = NavType.StringType },
                navArgument("projectId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val unitId = backStackEntry.arguments?.getInt("unitId") ?: return@composable
            val unitName = try {
                java.net.URLDecoder.decode(backStackEntry.arguments?.getString("unitName") ?: "", "UTF-8")
            } catch (e: Exception) { "Unit" }

            val problems = repository.getProblemsForUnit(unitId).collectAsState(initial = emptyList()).value

            UnitDetailScreen(
                unitName = unitName,
                problems = problems,
                onNavigateBack = { navController.popBackStack() },
                onMarkProblem = { problem, isCorrect ->
                    scope.launch {
                        repository.recordProblemAttempt(problem, isCorrect)
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                repository = repository
            )
        }

        composable(
            route = Screen.Stats.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getInt("projectId") ?: return@composable
            val project = projects.find { it.id == projectId }
            val projectUnits = units.filter { it.projectId == projectId }

            StatsScreen(
                projectName = project?.name ?: "统计",
                units = projectUnits,
                projectId = projectId,
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EmptyProjectScreen(
    onCreateProject: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        CreateFirstProjectDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name ->
                onCreateProject(name)
            }
        )
    } else {
        EmptyStateContent(onCreateProject = { showDialog = true })
    }
}

@Composable
private fun EmptyStateContent(
    onCreateProject: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "还没有项目",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击下方按钮创建第一个学习项目",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            FilledTonalButton(
                onClick = onCreateProject,
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("创建项目")
            }
        }
    }
}

@Composable
private fun CreateFirstProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "创建第一个项目",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("项目名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
