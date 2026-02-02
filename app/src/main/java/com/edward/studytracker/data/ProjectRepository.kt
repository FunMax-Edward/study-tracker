package com.edward.studytracker.data

import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val database: AppDatabase) {
    private val projectDao = database.projectDao()
    private val unitDao = database.studyUnitDao()
    private val problemDao = database.problemDao()
    private val practiceRecordDao = database.practiceRecordDao()
    
    fun getAllProjects(): Flow<List<Project>> = projectDao.getAllProjects()
    
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
                0 -> 5      // 灰色 → 浅绿
                1 -> 5      // 浅红 → 浅绿（跳过灰色）
                2 -> 1      // 中红 → 浅红
                3 -> 2      // 深红 → 中红
                4 -> 3      // 最深红 → 深红
                5 -> 6      // 浅绿 → 深绿
                6 -> 6      // 深绿保持
                else -> currentLevel
            }
        } else {
            when (currentLevel) {
                0, 5, 6 -> 1  // 灰色/浅绿/深绿 → 浅红
                1 -> 2        // 浅红 → 中红
                2 -> 3        // 中红 → 深红
                3 -> 4        // 深红 → 最深红
                4 -> 4        // 最深红保持
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
}
