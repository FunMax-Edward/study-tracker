package com.edward.studytracker.data

import android.util.Log
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.JsonParseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProjectRepository(private val database: AppDatabase) {
    private val projectDao = database.projectDao()
    private val unitDao = database.studyUnitDao()
    private val problemDao = database.problemDao()
    private val practiceRecordDao = database.practiceRecordDao()
    private val gson = Gson()

    companion object {
        private const val TAG = "ProjectRepository"
        private const val DATABASE_NAME = "study_tracker_database"
    }

    fun getAllProjects(): Flow<List<Project>> = projectDao.getAllProjects()

    suspend fun getAllProjectsSync(): List<Project> = withContext(Dispatchers.IO) {
        projectDao.getAllProjectsSync()
    }

    suspend fun createProject(name: String): Long {
        val project = Project(name = name)
        return projectDao.insertProject(project)
    }

    suspend fun deleteProject(projectId: Int) {
        projectDao.deleteProject(projectId)
    }

    fun getUnitsForProject(projectId: Int): Flow<List<StudyUnit>> =
        unitDao.getUnitsForProject(projectId)

    suspend fun createUnit(projectId: Int, name: String, problemCount: Int): Long {
        val unit = StudyUnit(
            projectId = projectId,
            name = name,
            problemCount = problemCount,
            sortOrder = 0
        )
        val unitId = unitDao.insertUnit(unit)

        val problems = (1..problemCount).map { index ->
            Problem(
                unitId = unitId.toInt(),
                problemIndex = index,
                proficiencyLevel = 0
            )
        }
        problemDao.insertProblems(problems)

        return unitId
    }

    suspend fun deleteUnit(unitId: Int) {
        unitDao.deleteUnit(unitId)
    }

    fun getProblemsForUnit(unitId: Int): Flow<List<Problem>> =
        problemDao.getProblemsForUnit(unitId)

    suspend fun recordProblemAttempt(problem: Problem, isCorrect: Boolean) {
        val currentLevel = problem.proficiencyLevel
        val newLevel = if (isCorrect) {
            when (currentLevel) {
                0 -> 5
                1 -> 5
                2 -> 1
                3 -> 2
                4 -> 3
                5 -> 6
                6 -> 6
                else -> currentLevel
            }
        } else {
            when (currentLevel) {
                0, 5, 6 -> 1
                1 -> 2
                2 -> 3
                3 -> 4
                4 -> 4
                else -> currentLevel
            }
        }

        val updatedProblem = problem.copy(proficiencyLevel = newLevel)
        problemDao.updateProblem(updatedProblem)

        val record = PracticeRecord(
            problemId = problem.id,
            isCorrect = isCorrect
        )
        practiceRecordDao.insertPracticeRecord(record)
    }

    fun getAllPracticeRecords(): Flow<List<PracticeRecord>> =
        practiceRecordDao.getAllPracticeRecords()

    suspend fun getTotalPracticeCount(): Int =
        practiceRecordDao.getTotalPracticeCount()

    suspend fun getCorrectPracticeCount(): Int =
        practiceRecordDao.getCorrectPracticeCount()

    suspend fun getWrongPracticeCount(): Int =
        practiceRecordDao.getWrongPracticeCount()

    suspend fun getPracticeRecordsBetween(startOfDay: Long, endOfDay: Long): List<PracticeRecord> =
        practiceRecordDao.getPracticeRecordsBetween(startOfDay, endOfDay)

    suspend fun exportData(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val exportData = ExportData(
                projects = projectDao.getAllProjectsSync(),
                studyUnits = unitDao.getAllUnitsSync(),
                problems = problemDao.getAllProblemsSync(),
                practiceRecords = practiceRecordDao.getAllPracticeRecordsSync()
            )
            gson.toJson(exportData)
        }.onFailure { error ->
            Log.e(TAG, "Export failed", error)
        }
    }

    suspend fun importData(json: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val importData = try {
                gson.fromJson(json, ExportData::class.java)
                    ?: throw IllegalArgumentException("Data is empty")
            } catch (e: JsonParseException) {
                throw IllegalArgumentException("Invalid JSON format", e)
            } catch (e: IllegalStateException) {
                throw IllegalArgumentException("Invalid JSON content", e)
            }

            if (importData.version <= 0) {
                throw IllegalArgumentException("Unsupported data version")
            }

            val projects = requireNotNull(importData.projects) { "Data is missing project list" }
            val studyUnits = requireNotNull(importData.studyUnits) { "Data is missing unit list" }
            val problems = requireNotNull(importData.problems) { "Data is missing problem list" }
            val practiceRecords = requireNotNull(importData.practiceRecords) { "Data is missing practice records" }

            database.withTransaction {
                practiceRecordDao.deleteAllPracticeRecords()
                problemDao.deleteAllProblems()
                unitDao.deleteAllUnits()
                projectDao.deleteAllProjects()

                if (projects.isNotEmpty()) {
                    projectDao.insertProjects(projects)
                }
                if (studyUnits.isNotEmpty()) {
                    unitDao.insertUnits(studyUnits)
                }
                if (problems.isNotEmpty()) {
                    problemDao.insertProblems(problems)
                }
                if (practiceRecords.isNotEmpty()) {
                    practiceRecordDao.insertPracticeRecords(practiceRecords)
                }
            }
        }.onFailure { error ->
            Log.e(TAG, "Import failed", error)
        }
    }
}
